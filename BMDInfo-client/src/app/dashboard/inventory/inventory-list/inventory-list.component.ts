import { Component, OnInit, ViewChild, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

import { InventoryService } from '../inventory.service';
import { AddItemDialogComponent } from '../add-item-dialog.component';
import { TransferStockDialogComponent } from '../transfer-stock-dialog.component'; 

@Component({
  selector: 'app-inventory-list',
  standalone: true,
    imports: [
    CommonModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatDialogModule,
    ReactiveFormsModule,
    MatCardModule,   
    MatIconModule     
  ],

  templateUrl: './inventory-list.component.html'
})
export class InventoryListComponent implements OnInit, AfterViewInit {
  dataSource = new MatTableDataSource<any>();
  displayedColumns: string[] = ['sku', 'name', 'location', 'quantity', 'unitPrice', 'totalValue', 'actions']; // ⬅️ add actions

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(private inventoryService: InventoryService, private dialog: MatDialog) {}

  ngOnInit(): void {
    this.loadData();
  }

  private loadData() {
    this.inventoryService.getInventoryItems().subscribe((data) => {
      const withTotals = data.map((d: any) => ({
        ...d,
        totalValue: Number(d.unitPrice) * Number(d.quantity)
      }));
      this.dataSource.data = withTotals;

      this.dataSource.filterPredicate = (row: any, filter: string) => {
        const f = filter.trim().toLowerCase();
        return (
          (row.sku ?? '').toString().toLowerCase().includes(f) ||
          (row.name ?? '').toString().toLowerCase().includes(f) ||
          (row.location ?? '').toString().toLowerCase().includes(f) ||
          (row.quantity ?? '').toString().toLowerCase().includes(f) ||
          (row.unitPrice ?? '').toString().toLowerCase().includes(f) ||
          (row.totalValue ?? '').toString().toLowerCase().includes(f)
        );
      };
    });
  }

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  applyFilter(value: string) {
    this.dataSource.filter = (value || '').trim().toLowerCase();
    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }

  openAddDialog() {
    const ref = this.dialog.open(AddItemDialogComponent, { width: '420px' });
    ref.afterClosed().subscribe((payload) => {
      if (!payload) return;
      this.inventoryService.addItem(payload).subscribe({
        next: (msg) => {
          alert(msg);
          this.loadData();
        },
        error: (err) => alert('Failed to add item: ' + (err?.error || err?.message || 'Unknown error'))
      });
    });
  }

  openTransferDialog(prefill?: { sku?: string; fromLocation?: string }) {
    const items = this.dataSource.data || [];
    const ref = this.dialog.open(TransferStockDialogComponent, {
      width: '440px',
      data: { items, prefill }
    });

    ref.afterClosed().subscribe((req) => {
      if (!req) return; 
      this.inventoryService.transferStock(req).subscribe({
        next: (msg) => {
          alert(typeof msg === 'string' ? msg : 'Stock transferred.');
          this.loadData();
        },
        error: (err) => alert('Transfer failed: ' + (err?.error || err?.message || 'Unknown error'))
      });
    });
  }
}

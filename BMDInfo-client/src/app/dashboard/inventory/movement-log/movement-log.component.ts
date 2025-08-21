import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

import { MatButtonModule } from '@angular/material/button';
import { InventoryService, StockMovementLog } from '../inventory.service';

type Row = StockMovementLog & {
  item: { sku?: string; name?: string } | null;
  fromLocation: string | null;
  toLocation: string | null;
  quantity: number | null;
  action: string | null;
  date: string | null;
};

@Component({
  selector: 'app-movement-log',
  standalone: true,
  imports: [
    CommonModule,
    DatePipe,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
     MatCardModule,   
    MatIconModule    
  ],
  templateUrl: './movement-log.component.html'
})
export class MovementLogComponent implements OnInit {
  dataSource = new MatTableDataSource<Row>([]);
  displayedColumns: string[] = ['date', 'sku', 'name', 'from', 'to', 'quantity', 'action'];

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(private inv: InventoryService) {}

  ngOnInit(): void {
    this.configureFilteringAndSorting();
    this.load();
  }

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  load(): void {
    this.inv.getMovements().subscribe
({
      next: (list) => {
        const normalized: Row[] = list.map((m: StockMovementLog) => ({
          ...m,
          item: (m as any).item ?? null,
          action:
            (m as any).action ??
            ((m.fromLocation && m.toLocation) ? 'TRANSFER' : 'ADD'),
          fromLocation:
            m.fromLocation && m.fromLocation !== 'N/A' ? m.fromLocation : '—',
          toLocation: m.toLocation ?? '—',
          quantity: (m.quantity ?? 0) as number,
          date: m.date ?? null
        }));
        this.dataSource.data = normalized;
      },
      error: (err) => {
        console.error('Failed to load movement logs', err);
        this.dataSource.data = [];
      }
    });
  }

  applyFilter(v: string): void {
    this.dataSource.filter = (v ?? '').trim().toLowerCase();
    this.paginator?.firstPage();
  }

  private configureFilteringAndSorting(): void {
    // filter: search by sku/name/from/to/action
    this.dataSource.filterPredicate = (row: Row, filter: string) => {
      const f = (filter || '').trim().toLowerCase();
      const sku = row.item?.sku ?? '';
      const name = row.item?.name ?? '';
      const from = row.fromLocation ?? '';
      const to = row.toLocation ?? '';
      const action = row.action ?? '';
      return (
        sku.toLowerCase().includes(f) ||
        name.toLowerCase().includes(f) ||
        from.toLowerCase().includes(f) ||
        to.toLowerCase().includes(f) ||
        action.toLowerCase().includes(f)
      );
    };

    this.dataSource.sortingDataAccessor = (row: Row, columnId: string): string | number => {
      switch (columnId) {
        case 'sku':      return row.item?.sku ?? '';
        case 'name':     return row.item?.name ?? '';
        case 'from':     return row.fromLocation ?? '';
        case 'to':       return row.toLocation ?? '';
        case 'action':   return row.action ?? '';
        case 'quantity': return Number(row.quantity ?? 0);
        case 'date':     return row.date ?? '';
        default:         return (row as any)[columnId];
      }
    };
  }
}

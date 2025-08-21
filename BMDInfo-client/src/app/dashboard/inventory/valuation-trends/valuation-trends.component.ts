import { Component, OnInit, AfterViewInit, ViewChild, ElementRef, Inject } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { PLATFORM_ID } from '@angular/core';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatCardModule } from '@angular/material/card';  
import { InventoryService, InventoryItem } from '../inventory.service';

type Method = 'FIFO' | 'LIFO' | 'Weighted';
type Row = {
  sku: string;
  name: string;
  location: string;
  method: string;
  value: number;
  lastUpdated: string;
};

@Component({
  selector: 'app-valuation-trends',
  standalone: true,
  imports: [
    CommonModule,
    MatFormFieldModule,
    MatSelectModule,
    MatButtonModule,
    MatTableModule,
    MatPaginatorModule,
    MatCardModule   
  ],
  templateUrl: './valuation-trends.component.html',
  styles: [`
    .filter-card {
      margin-bottom: 16px;
      padding: 16px;
      border-radius: 12px;
      box-shadow: 0px 2px 8px rgba(0,0,0,0.1);
    }
    .filter-container {
      display: flex;
      flex-wrap: wrap;
      gap: 16px;
      align-items: center;
    }
    mat-form-field {
      flex: 1 1 220px;
      min-width: 200px;
    }
    .button-group {
      display: flex;
      gap: 12px;
      align-items: center;
    }
    canvas {
      border: 1px solid #ccc;
      margin-top: 16px;
      width: 100%;
      max-width: 1000px;
    }
    h3 {
      margin-top: 24px;
    }
  `]
})
export class ValuationTrendsComponent implements OnInit, AfterViewInit {
  items: InventoryItem[] = [];
  locations: string[] = [];
  method: Method = 'FIFO';
  itemId: string | null = null;
  location: string | null = null;

  results = new MatTableDataSource<Row>([]);
  displayed: string[] = ['sku', 'name', 'location', 'method', 'value', 'lastUpdated']; 

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild('valuationCanvas', { static: true }) canvas!: ElementRef<HTMLCanvasElement>;
  private ctx: CanvasRenderingContext2D | null = null;
  private readonly isBrowser: boolean;

  constructor(private inv: InventoryService, @Inject(PLATFORM_ID) platformId: Object) {
    this.isBrowser = isPlatformBrowser(platformId);
  }

  ngOnInit(): void {
    this.inv.getInventoryItems().subscribe({
      next: items => {
        this.items = items;
        this.locations = Array.from(new Set(items.map(i => i.location)));
        this.loadPersistedValuations();
        this.results.paginator = this.paginator;
      },
      error: err => console.error('Failed to load items', err)
    });
  }

  ngAfterViewInit(): void {
    if (this.isBrowser) {
      this.ctx = this.canvas.nativeElement.getContext('2d');
    }
  }

  private loadPersistedValuations(): void {
    this.inv.getAllValuations().subscribe({
      next: rows => {
        const mapped: Row[] = rows.map((v: any) => ({
          sku: v.item?.sku ?? '',
          name: v.item?.name ?? '',
          location: v.location,
          method: v.valuationMethod,
          value: Number(v.totalValue),
          lastUpdated: v.lastUpdated 
        }));
        this.results.data = mapped;
        this.paginator?.firstPage();
      },
      error: err => console.error('Failed to load valuations', err)
    });
  }
  download(format: 'csv' | 'pdf') {
  let url = `http://localhost:8080/api/inventory/export-valuation?format=${format}`;

  if (this.itemId) {
    url += `&itemId=${this.itemId}`;
  }
  if (this.location) {
    url += `&location=${this.location}`;
  }

  fetch(url)
    .then(res => {
      if (!res.ok) throw new Error(`Download failed: ${res.statusText}`);
      return res.blob();
    })
    .then(blob => {
      const link = document.createElement('a');
      link.href = URL.createObjectURL(blob);

      const fileNameParts = ['valuation'];
      if (this.itemId) fileNameParts.push(this.itemId);
      if (this.location) fileNameParts.push(this.location);
      link.download = fileNameParts.join('_') + `.${format}`;

      link.click();
    })
    .catch(err => console.error('Download failed', err));
}



  run(): void {
    if (!this.itemId || !this.location) return;
    const obs =
      this.method === 'FIFO'
        ? this.inv.calculateFifo(this.itemId, this.location)
        : this.method === 'LIFO'
        ? this.inv.calculateLifo(this.itemId, this.location)
        : this.inv.calculateWeighted(this.itemId, this.location);

    obs.subscribe({
      next: () => {
        this.loadPersistedValuations();
        this.loadTrends();
      },
      error: err => alert('Valuation failed: ' + (err?.error || err?.message || err))
    });
  }

  loadTrends(): void {
    if (!this.itemId || !this.location) {
      alert('Please select Item and Location first'); return;
    }
    this.inv.getValuationHistory(this.itemId, this.location).subscribe({
      next: rows => {
        if (!rows || rows.length === 0) {
          alert('No historical valuations found for this item/location');
          this.drawChart([], []); return;
        }
        const fifo = rows.filter(r => r.valuationMethod === 'FIFO');
        const lifo = rows.filter(r => r.valuationMethod === 'LIFO');
        const wa   = rows.filter(r => r.valuationMethod === 'Weighted Average' || r.valuationMethod === 'Weighted');

        const allDates: string[] = [...fifo, ...lifo, ...wa]
          .map(r => r.lastUpdated)
          .filter((d): d is string => typeof d === 'string' && d.length > 0);

        const labels: string[] = Array.from(new Set(allDates)).sort();

        const datasets = [
          { label: 'FIFO', color: 'blue',  data: labels.map(d => Number(fifo.find(r => r.lastUpdated === d)?.totalValue ?? NaN)) },
          { label: 'LIFO', color: 'red',   data: labels.map(d => Number(lifo.find(r => r.lastUpdated === d)?.totalValue ?? NaN)) },
          { label: 'WA',   color: 'green', data: labels.map(d => Number(wa.find(r => r.lastUpdated === d)?.totalValue ?? NaN)) }
        ];

        this.drawChart(labels, datasets);
      },
      error: err => {
        console.error('History call failed', err);
        alert('Failed to load valuation history (check /valuation-history endpoint)');
      }
    });
  }

  private drawChart(
    labels: string[],
    datasets: { label: string; data: number[]; color: string }[]
  ): void {
    if (!this.isBrowser || !this.ctx) return;

    const ctx = this.ctx;
    const canvas = this.canvas.nativeElement;

    if (!canvas.width) canvas.width = 1000;
    if (!canvas.height) canvas.height = 320;

    ctx.clearRect(0, 0, canvas.width, canvas.height);

    const padding = 48;
    const width = canvas.width - padding * 2;
    const height = canvas.height - padding * 2;

    const numeric = datasets.flatMap(ds => ds.data).filter(v => Number.isFinite(v));
    if (numeric.length === 0 || labels.length === 0) {
      ctx.fillStyle = '#666';
      ctx.font = '12px Arial';
      ctx.fillText('No trend data', padding, padding + 12);
      return;
    }

    const maxVal = Math.max(...numeric) * 1.1;
    const minVal = Math.min(...numeric) * 0.9;
    const xStep = width / Math.max(1, labels.length - 1);
    const yScale = height / (maxVal - minVal || 1);

    ctx.strokeStyle = '#eee';
    ctx.lineWidth = 1;
    const gridLines = 5;
    for (let i = 0; i <= gridLines; i++) {
      const y = padding + (height / gridLines) * i;
      ctx.beginPath();
      ctx.moveTo(padding, y);
      ctx.lineTo(padding + width, y);
      ctx.stroke();
    }

    ctx.strokeStyle = '#000';
    ctx.lineWidth = 1.2;
    ctx.beginPath();
    ctx.moveTo(padding, padding);
    ctx.lineTo(padding, padding + height);
    ctx.lineTo(padding + width, padding + height);
    ctx.stroke();

    ctx.fillStyle = '#000';
    ctx.font = '10px Arial';
    for (let i = 0; i <= gridLines; i++) {
      const value = maxVal - ((maxVal - minVal) / gridLines) * i;
      const y = padding + (height / gridLines) * i;
      ctx.fillText(this.formatNumber(value), 4, y + 3);
    }

    // Lines + points
    datasets.forEach(ds => {
      ctx.strokeStyle = ds.color;
      ctx.lineWidth = 1.6;
      ctx.beginPath();
      let started = false;

      ds.data.forEach((val, i) => {
        if (!Number.isFinite(val)) return;
        const x = padding + i * xStep;
        const y = padding + height - (val - minVal) * yScale;

        if (!started) { ctx.moveTo(x, y); started = true; }
        else { ctx.lineTo(x, y); }
      });
      ctx.stroke();

      ds.data.forEach((val, i) => {
        if (!Number.isFinite(val)) return;
        const x = padding + i * xStep;
        const y = padding + height - (val - minVal) * yScale;
        ctx.fillStyle = ds.color;
        ctx.beginPath();
        ctx.arc(x, y, 2.5, 0, Math.PI * 2);
        ctx.fill();
      });
    });

    ctx.fillStyle = '#000';
    ctx.font = '10px Arial';
    labels.forEach((lbl, i) => {
      const x = padding + i * xStep;
      ctx.fillText(lbl, x - 12, padding + height + 14);
    });
  }

  private formatNumber(n: number): string {
    if (Math.abs(n) >= 1_000_000) return (n / 1_000_000).toFixed(1) + 'M';
    if (Math.abs(n) >= 1_000) return (n / 1_000).toFixed(1) + 'k';
    return n.toFixed(0);
  }
}

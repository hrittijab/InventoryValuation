import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

// Interfaces 
export interface InventoryItem {
  id: string;
  sku: string;
  name: string;
  category?: string;
  location: string;
  unitPrice: number;
  quantity: number;
  totalValue?: number; 
}

export interface StockMovementLog {
  id?: string;
  item: InventoryItem | string | null;
  fromLocation: string;
  toLocation: string;
  quantity: number;
  date: string; 
  action?: 'ADD' | 'TRANSFER' | 'REMOVE' | string;
}

export interface InventoryValuationRow {
  item?: { id?: string; sku?: string; name?: string };
  location: string;
 
  valuationMethod: 'FIFO' | 'LIFO' | 'Weighted' | 'Weighted Average' | string;
  totalValue: number | string;
  lastUpdated?: string;
}

@Injectable({ providedIn: 'root' })
export class InventoryService {
  private baseUrl = 'http://localhost:8080/api/inventory';

  constructor(private http: HttpClient) {}

  // Items / Movements 
  getInventoryItems(): Observable<InventoryItem[]> {
    return this.http.get<InventoryItem[]>(`${this.baseUrl}/items`);
  }

  getMovements(): Observable<StockMovementLog[]> {
    return this.http.get<StockMovementLog[]>(`${this.baseUrl}/movements`);
  }

  //  Stock operations 
  addStock(itemId: string, quantity: number, pricePerUnit: number, location: string): Observable<string> {
    let params = new HttpParams()
      .set('itemId', itemId)
      .set('quantity', String(quantity))
      .set('pricePerUnit', String(pricePerUnit))
      .set('location', location);
    return this.http.post(`${this.baseUrl}/add-stock`, null, { params, responseType: 'text' });
  }

  transferStock(req: { itemId: string; fromLocation: string; toLocation: string; quantity: number }): Observable<string> {
    return this.http.post(`${this.baseUrl}/transfer-stock`, req, { responseType: 'text' });
  }

  calculateFifo(itemId: string, location: string): Observable<number> {
    const params = new HttpParams().set('itemId', itemId).set('location', location);
    return this.http.get<number>(`${this.baseUrl}/calculate-fifo`, { params });
  }

  calculateLifo(itemId: string, location: string): Observable<number> {
    const params = new HttpParams().set('itemId', itemId).set('location', location);
    return this.http.get<number>(`${this.baseUrl}/calculate-lifo`, { params });
  }

  calculateWeighted(itemId: string, location: string): Observable<number> {
    const params = new HttpParams().set('itemId', itemId).set('location', location);
    return this.http.get<number>(`${this.baseUrl}/calculate-weighted`, { params });
  }

  // Export persisted valuations 
  exportValuation(format: 'csv' | 'pdf'): Observable<Blob> {
    const params = new HttpParams().set('format', format);
    return this.http.get(`${this.baseUrl}/export-valuation`, { params, responseType: 'blob' });
  }

  getAllValuations(): Observable<InventoryValuationRow[]> {
    return this.http.get<InventoryValuationRow[]>(`${this.baseUrl}/valuations`);
  }

  addItem(req: Omit<InventoryItem, 'id' | 'totalValue'>): Observable<string> {
    return this.http.post(`${this.baseUrl}/add-item`, req, { responseType: 'text' });
  }

  // Historicl valuation snapshots for charts/timeline 
  getValuationHistory(
    itemId: string,
    location?: string,
    limit?: number
  ): Observable<InventoryValuationRow[]> {
    let params = new HttpParams().set('itemId', itemId);
    if (location) params = params.set('location', location);
    if (limit != null) params = params.set('limit', String(limit));
    return this.http.get<InventoryValuationRow[]>(
      `${this.baseUrl}/valuation-history`,
      { params }
    );
  }
}

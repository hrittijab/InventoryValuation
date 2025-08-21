import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { InventoryService, InventoryItem } from '../inventory.service';

describe('InventoryService', () => {
  let service: InventoryService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [InventoryService],
    });

    service = TestBed.inject(InventoryService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch inventory items', () => {
    const mockItems: InventoryItem[] = [
      { id: '1', sku: 'SKU1', name: 'Laptop', location: 'Dhaka', unitPrice: 1000, quantity: 10 }
    ];

    service.getInventoryItems().subscribe((items) => {
      expect(items.length).toBe(1);
      expect(items[0].name).toBe('Laptop');
    });

    const req = httpMock.expectOne('http://localhost:8080/api/inventory/items');
    expect(req.request.method).toBe('GET');
    req.flush(mockItems);
  });

  it('should handle movement log safely', () => {
    const mockMovements = [
      {
        id: '1',
        item: { sku: 'SKU1', name: 'Laptop', location: 'Dhaka', unitPrice: 1000, quantity: 5 },
        fromLocation: 'Dhaka',
        toLocation: 'Chittagong',
        quantity: 5,
        date: '2025-08-21'
      }
    ];

    service.getMovements().subscribe((movements) => {
      expect(movements.length).toBe(1);

      const first = movements[0];
      expect(first.fromLocation).toBe('Dhaka');
      expect(first.toLocation).toBe('Chittagong');

      if (first.item && typeof first.item !== 'string') {
        expect(first.item.name).toBe('Laptop');
      }
    });

    const req = httpMock.expectOne('http://localhost:8080/api/inventory/movements');
    expect(req.request.method).toBe('GET');
    req.flush(mockMovements);
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ValuationTrendsComponent } from './valuation-trends.component';
import { InventoryService } from '../inventory.service';
import { of } from 'rxjs';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

describe('ValuationTrendsComponent', () => {
  let component: ValuationTrendsComponent;
  let fixture: ComponentFixture<ValuationTrendsComponent>;
  let mockService: any;

  beforeEach(async () => {
    mockService = {
      getInventoryItems: jasmine.createSpy('getInventoryItems').and.returnValue(of([])),
      getAllValuations: jasmine.createSpy('getAllValuations').and.returnValue(of([])),
      calculateFifo: jasmine.createSpy('calculateFifo').and.returnValue(of(5000)),
      calculateLifo: jasmine.createSpy('calculateLifo').and.returnValue(of(4800)),
      calculateWeighted: jasmine.createSpy('calculateWeighted').and.returnValue(of(4900)),
      getValuationHistory: jasmine.createSpy('getValuationHistory').and.returnValue(of([]))
    };

    await TestBed.configureTestingModule({
      imports: [
        ValuationTrendsComponent, 
        BrowserAnimationsModule     
      ],
      providers: [{ provide: InventoryService, useValue: mockService }]
    }).compileComponents();

    fixture = TestBed.createComponent(ValuationTrendsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should call FIFO method when run() with method=FIFO', () => {
    component.itemId = '1';
    component.location = 'Dhaka';
    component.method = 'FIFO';
    component.run();
    expect(mockService.calculateFifo).toHaveBeenCalledWith('1', 'Dhaka');
  });

  it('should call LIFO method when run() with method=LIFO', () => {
    component.itemId = '1';
    component.location = 'Dhaka';
    component.method = 'LIFO';
    component.run();
    expect(mockService.calculateLifo).toHaveBeenCalledWith('1', 'Dhaka');
  });

  it('should call Weighted method when run() with method=Weighted', () => {
    component.itemId = '1';
    component.location = 'Dhaka';
    component.method = 'Weighted';
    component.run();
    expect(mockService.calculateWeighted).toHaveBeenCalledWith('1', 'Dhaka');
  });

  it('should call valuation history API when loadTrends() is called', () => {
    component.itemId = '1';
    component.location = 'Dhaka';
    component.loadTrends();
    expect(mockService.getValuationHistory).toHaveBeenCalledWith('1', 'Dhaka');
  });
});

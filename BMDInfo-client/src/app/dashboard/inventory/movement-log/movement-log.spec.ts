import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MovementLogComponent } from './movement-log.component';
import { InventoryService } from '../inventory.service';
import { of } from 'rxjs';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

describe('MovementLogComponent', () => {
  let component: MovementLogComponent;
  let fixture: ComponentFixture<MovementLogComponent>;
  let mockService: any;

  beforeEach(async () => {
    mockService = {
      getMovements: jasmine.createSpy('getMovements').and.returnValue(of([
        {
          id: '1',
          item: { name: 'Laptop' },
          fromLocation: 'Dhaka',
          toLocation: 'Chittagong',
          quantity: 5,
          date: '2025-08-21',
          action: 'TRANSFER'
        }
      ]))
    };

    await TestBed.configureTestingModule({
      imports: [
        MovementLogComponent,  
        BrowserAnimationsModule 
      ],
      providers: [{ provide: InventoryService, useValue: mockService }]
    }).compileComponents();

    fixture = TestBed.createComponent(MovementLogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load movement logs from service', () => {
    expect(mockService.getMovements).toHaveBeenCalled();
    const first = component.dataSource?.data[0];
    expect(first).toBeTruthy();
    if (first && first.item && typeof first.item !== 'string') {
      expect(first.item.name).toBe('Laptop');
    }
  });

  it('should render movement log in the template', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    const tableText = compiled.querySelector('table')?.textContent || '';
    expect(tableText).toContain('Laptop');
    expect(tableText).toContain('Dhaka');
    expect(tableText).toContain('Chittagong');
  });
});

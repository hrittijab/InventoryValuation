import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BidTrackerDetailsComponentComponent } from './bid-tracker-details-component.component';

describe('BidTrackerDetailsComponentComponent', () => {
  let component: BidTrackerDetailsComponentComponent;
  let fixture: ComponentFixture<BidTrackerDetailsComponentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BidTrackerDetailsComponentComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(BidTrackerDetailsComponentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

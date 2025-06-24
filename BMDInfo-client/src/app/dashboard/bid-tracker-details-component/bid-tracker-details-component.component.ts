import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { BidTracker } from '../../interface/bid-tracker';

@Component({
  selector: 'app-bid-tracker-details-component',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './bid-tracker-details-component.component.html',
  styleUrl: './bid-tracker-details-component.component.scss'
})
export class BidTrackerDetailsComponentComponent {
 @Input() data!: BidTracker;
}

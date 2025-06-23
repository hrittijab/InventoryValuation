import { Component } from '@angular/core';
import { DataServiceService } from '../../data-service.service';
import { Router } from '@angular/router';
import { NgFor } from '@angular/common';

@Component({
  selector: 'app-dashboard-ui',
  standalone: true,
  imports: [NgFor],
  templateUrl: './dashboard-ui.component.html',
  styleUrl: './dashboard-ui.component.scss'
})
export class DashboardUiComponent {
  
 allSpradSheetData: any;

 constructor(
   private router: Router,
   private dataService: DataServiceService,
 ) {}


  ngOnInit() {
     this.dataService.getBidTrackerData().subscribe({
      next: (r) => {
       this.allSpradSheetData = r;
      },
      error: (err) => console.error(err),
    });
    }

   


  }

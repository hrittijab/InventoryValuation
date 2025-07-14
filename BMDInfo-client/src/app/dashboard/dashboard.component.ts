import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { DataServiceService } from '../data-service.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
              CommonModule,
              FormsModule,
              RouterModule,
            ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent {

  lastFetchDataAt: any;
  isLoaderVisible = false;

  constructor(
    private router: Router,
    private dataService: DataServiceService,
  ) {}

  storedValue!: any;
  ngOnInit(): void {
    // Check if the user is logged in as admin
    if(!sessionStorage.getItem('admin')) {
      this.router.navigate(['/login']);
      return;
    }
    this.storedValue = sessionStorage.getItem('admin');

    this.dataService.getLastFetchedDataAt().subscribe({
      next: (data) => {
        this.lastFetchDataAt = data;
      },
      error: (err) => {
        console.error('Error fetching last fetched data at:', err);
        this.lastFetchDataAt = 'N/A';
      }
    });

  }

  searchProducts(data: NgForm){

  }

  logOut(){
    sessionStorage.removeItem('admin');
    this.router.navigate(['/login']);

  }

  fetchData() {
    this.isLoaderVisible = true; // Show the loader
      this.dataService.getSpradeSheetData().subscribe({
      next: (r) => {
      
      },
      error: (err) => {
        if (err.status === 200) {
            alert('Data fetched successfully');
            this.isLoaderVisible = false; // Hide the loader
            window.location.reload();
        }else {
        alert('Error fetching data: ' + err.message);
        this.isLoaderVisible = false; // Hide the loader
        console.error(err);
        }
      } 
    });
  }

}

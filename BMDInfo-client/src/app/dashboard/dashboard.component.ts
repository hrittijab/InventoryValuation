import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Component, Inject, PLATFORM_ID } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { DataServiceService } from '../data-service.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss'
})
export class DashboardComponent {
  lastFetchDataAt: any;
  isLoaderVisible = false;
  storedValue!: any;

  constructor(
    private router: Router,
    private dataService: DataServiceService,
    @Inject(PLATFORM_ID) private platformId: Object
  ) {}

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      const admin = sessionStorage.getItem('admin');

      // if (!admin) {
      //   this.router.navigate(['/login']);
      //   return;
      // }

      this.storedValue = admin;
    }

    this.dataService.getLastFetchedDataAt().subscribe({
      next: (data) => (this.lastFetchDataAt = data),
      error: () => (this.lastFetchDataAt = 'N/A')
    });
  }

  searchProducts(data: NgForm) {}

  logOut() {
    if (isPlatformBrowser(this.platformId)) {
      sessionStorage.removeItem('admin');
    }
    this.router.navigate(['/login']);
  }

  fetchData() {
    this.isLoaderVisible = true;
    this.dataService.getSpradeSheetData().subscribe({
      next: () => {},
      error: (err) => {
        if (err.status === 200) {
          alert('Data fetched successfully');
          this.isLoaderVisible = false;
          if (typeof window !== 'undefined') window.location.reload();
        } else {
          alert('Error fetching data: ' + err.message);
          this.isLoaderVisible = false;
          console.error(err);
        }
      }
    });
  }
}

import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { DataServiceService } from '../data-service.service';
import { log } from 'console';

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

  
  constructor(
    private router: Router,
    private dataService: DataServiceService,
  ) {}

  storedValue!: any;
  ngOnInit(): void {
    this.storedValue = sessionStorage.getItem('admin');
  }

  searchProducts(data: NgForm){

  }

  logOut(){
    sessionStorage.removeItem('admin');
    this.router.navigate(['/login']);

  }

  refreshData() {
      this.dataService.getSpradeSheetData().subscribe({
      next: (r) => {
       console.log(r);
       window.location.reload();
      },
      error: (err) => console.error(err),
    });
  }

}

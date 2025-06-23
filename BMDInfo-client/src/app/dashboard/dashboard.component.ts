import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';

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
    private router: Router
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

}

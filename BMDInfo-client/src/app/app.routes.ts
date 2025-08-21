import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { DashboardUiComponent } from './dashboard/dashboard-ui/dashboard-ui.component';
import { InventoryListComponent } from './dashboard/inventory/inventory-list/inventory-list.component';
import { MovementLogComponent } from './dashboard/inventory/movement-log/movement-log.component';
import { ValuationTrendsComponent } from './dashboard/inventory/valuation-trends/valuation-trends.component';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard/inventory-list', pathMatch: 'full' },

  { path: 'login', title: 'Login Page', component: LoginComponent },

  {
    path: 'dashboard',
    title: 'Dashboard',
    component: DashboardComponent,
    children: [
      { path: '', title: 'Dashboard Home', component: DashboardUiComponent },
      { path: 'bid-tracker', title: 'Bid Tracker', component: DashboardUiComponent },
      { path: 'inventory-list', title: 'Inventory List', component: InventoryListComponent },
      { path: 'movements', title: 'Stock Logs', component: MovementLogComponent },
      { path: 'valuation', title: 'Valuation Report', component: ValuationTrendsComponent },
    ]
  },

  { path: '**', redirectTo: 'dashboard/inventory-list' }
];

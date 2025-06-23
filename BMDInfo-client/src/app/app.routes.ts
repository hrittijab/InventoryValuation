import { Routes } from '@angular/router';
import { LoginComponent } from './login/login.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { DashboardUiComponent } from './dashboard/dashboard-ui/dashboard-ui.component';

export const routes: Routes = [
  { path: '',  title: 'Login Page', component: LoginComponent},
  { path: 'login',  title: 'Login Page', component:  LoginComponent},

  { 
    path: 'dashboard',  
    title: 'Dashboard', 
    component:  DashboardComponent,
    
    children: [
      {
        path: 'dashboard',
        title: 'Dashboard',
        component: DashboardUiComponent
      },

    ]

},
];

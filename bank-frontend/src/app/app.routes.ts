import { Routes } from '@angular/router';

import { Login } from './Auth/login/login';
import {Register} from './Auth/register/register';
import{ Dashboard} from "./dashboard/dashboard";
import {authGuard} from './shared/auth-guard';


export const routes: Routes = [

  { path: 'login', component: Login},
  { path: '', redirectTo: 'login' , pathMatch: 'full'},
  { path: 'register', component: Register},
  { path: 'dashboard', component: Dashboard, canActivate: [authGuard]}

];

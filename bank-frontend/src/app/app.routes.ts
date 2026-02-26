import { Routes } from '@angular/router';

import { Login } from './Auth/login/login';
import {Register} from './Auth/register/register';
import{ Dashboard} from "./dashboard/dashboard";
import {authGuard} from './shared/auth-guard';
import { Payment } from './payments/payment/payment';
import { CreateAccount } from './accounts/create-account/create-account';

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'register', component: Register },
  { path: 'dashboard', component: Dashboard, canActivate: [authGuard] },
  { path: 'payment', component: Payment, canActivate: [authGuard] },
  { path: 'create-account', component: CreateAccount, canActivate: [authGuard] },
];

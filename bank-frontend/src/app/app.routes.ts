import { Routes } from '@angular/router';

import { Login } from './Auth/login/login';
import {Register} from './Auth/register/register';
import{ Dashboard} from "./dashboard/dashboard";
import {authGuard} from './shared/auth-guard';
import { Payment } from './payments/payment/payment';
import { CreateAccount } from './accounts/create-account/create-account';
import { AccountDetail } from './accounts/account-detail/account-detail';
import { Profile } from './customers/profile/profile';
import { NotFound } from './shared/not-found/not-found';

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'register', component: Register },
  { path: 'dashboard', component: Dashboard, canActivate: [authGuard] },
  { path: 'payment', component: Payment, canActivate: [authGuard] },
  { path: 'create-account', component: CreateAccount, canActivate: [authGuard] },
  { path: 'account/:id', component: AccountDetail, canActivate: [authGuard] },
  { path: 'profile', component: Profile, canActivate: [authGuard] },
  { path: '**', component: NotFound },
];

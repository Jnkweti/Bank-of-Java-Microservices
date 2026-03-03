import { Component, ChangeDetectorRef, OnInit, OnDestroy } from '@angular/core';
import { Account } from '../accounts/account';
import { Customer } from '../customers/customer';
import { AuthService } from '../Auth/auth.service';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { switchMap, Subscription } from 'rxjs';
import { AccountModel } from '../shared/models';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, RouterLink, MatCardModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard implements OnInit, OnDestroy {
  accounts: AccountModel[] = [];
  loading = true;
  private sub?: Subscription;

  constructor(
    private accountService: Account,
    private customerService: Customer,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    const email = this.authService.getEmailFromToken();
    if (!email) {
      this.loading = false;
      return;
    }

    this.sub = this.customerService.getCustomerByEmail(email).pipe(
      switchMap(customer => this.accountService.getAccountsByCustomerId(customer.id))
    ).subscribe({
      next: (accounts) => {
        this.accounts = accounts;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  ngOnDestroy() {
    this.sub?.unsubscribe();
  }
}
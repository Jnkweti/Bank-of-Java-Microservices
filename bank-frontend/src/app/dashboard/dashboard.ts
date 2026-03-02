import { Component, ChangeDetectorRef, OnInit } from '@angular/core';
import { Account } from '../accounts/account';
import { Customer } from '../customers/customer';
import { AuthService } from '../Auth/auth.service';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { switchMap } from 'rxjs';

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, MatCardModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard implements OnInit {
  accounts: any[] = [];
  loading = true;

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

    this.customerService.getCustomerByEmail(email).pipe(
      switchMap((customer: any) => this.accountService.getAccountsByCustomerId(customer.id))
    ).subscribe({
      next: (accounts: any) => {
        this.accounts = accounts;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (err: any) => {
        console.log('Failed to load accounts', err);
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }
}

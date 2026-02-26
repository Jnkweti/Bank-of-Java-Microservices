import { Component, OnInit } from '@angular/core';
import { Account } from '../accounts/account';
import { Customer } from '../customers/customer';
import { AuthService } from '../Auth/auth.service';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, MatCardModule, MatButtonModule, MatIconModule],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard implements OnInit {
  accounts: any[] = [];

  constructor(
    private accountService: Account,
    private customerService: Customer,
    private authService: AuthService
  ) {}

  ngOnInit() {
    const email = this.authService.getEmailFromToken();
    if (!email) return;

    this.customerService.getCustomerByEmail(email).subscribe({
      next: (customer: any) => {
        this.accountService.getAccountsByCustomerId(customer.id).subscribe({
          next: (res: any) => { this.accounts = res },
          error: (err: any) => { console.log('Failed to get accounts', err) }
        });
      },
      error: (err: any) => { console.log('Failed to get customer', err) }
    });
  }
}

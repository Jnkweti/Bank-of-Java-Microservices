import {Component, OnInit} from '@angular/core';
import { Account } from '../accounts/account';
import { Customer } from '../customers/customer';
import { AuthService } from '../Auth/auth.service';


@Component({
  selector: 'app-dashboard',
  imports: [],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})

export class Dashboard  implements OnInit {
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

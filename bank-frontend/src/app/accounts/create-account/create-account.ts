import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { Account } from '../account';
import { Customer } from '../../customers/customer';
import { AuthService } from '../../Auth/auth.service';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';

@Component({
  selector: 'app-create-account',
  imports: [FormsModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatSelectModule],
  templateUrl: './create-account.html',
  styleUrl: './create-account.scss',
})
export class CreateAccount {
  accName = '';
  type = 'CHECKING';
  errorMessage = '';

  constructor(
    private accountService: Account,
    private customerService: Customer,
    private authService: AuthService,
    private router: Router
  ) {}

  onSubmit() {
    this.errorMessage = '';

    const email = this.authService.getEmailFromToken();
    if (!email) return;

    this.customerService.getCustomerByEmail(email).subscribe({
      next: (customer: any) => {
        this.accountService.createAccount({
          accName: this.accName,
          customerId: customer.id,
          type: this.type,
          status: 'ACTIVE',
          balance: '0.00'
        }).subscribe({
          next: () => { this.router.navigate(['/dashboard']); },
          error: () => { this.errorMessage = 'Failed to create account. Please try again.'; }
        });
      },
      error: () => { this.errorMessage = 'Could not find your customer profile.'; }
    });
  }
}

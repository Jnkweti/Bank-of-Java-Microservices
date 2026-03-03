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
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { switchMap } from 'rxjs';

@Component({
  selector: 'app-create-account',
  imports: [FormsModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatSelectModule, MatProgressSpinnerModule],
  templateUrl: './create-account.html',
  styleUrl: './create-account.scss',
})
export class CreateAccount {
  accName = '';
  type = 'CHECKING';
  errorMessage = '';
  loading = false;

  constructor(
    private accountService: Account,
    private customerService: Customer,
    private authService: AuthService,
    private router: Router
  ) {}

  onSubmit() {
    this.errorMessage = '';
    this.loading = true;

    const email = this.authService.getEmailFromToken();
    if (!email) {
      this.loading = false;
      return;
    }

    this.customerService.getCustomerByEmail(email).pipe(
      switchMap(customer => this.accountService.createAccount({
        accName: this.accName,
        customerId: customer.id,
        type: this.type as 'CHECKING' | 'SAVINGS',
        status: 'ACTIVE',
        balance: '0.00'
      }))
    ).subscribe({
      next: () => { this.router.navigate(['/dashboard']); },
      error: () => {
        this.errorMessage = 'Failed to create account. Please try again.';
        this.loading = false;
      }
    });
  }
}

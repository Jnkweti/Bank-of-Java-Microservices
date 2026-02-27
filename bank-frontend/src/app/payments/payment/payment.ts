import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PaymentService } from '../PaymentService';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-payment',
  imports: [FormsModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatSelectModule, MatProgressSpinnerModule],
  templateUrl: './payment.html',
  styleUrl: './payment.scss',
})
export class Payment {
  fromAccountId = '';
  toAccountId = '';
  amount = '';
  description = '';
  type = 'TRANSFER';
  successMessage = '';
  errorMessage = '';
  loading = false;

  constructor(private paymentService: PaymentService) {}

  onSubmit() {
    this.successMessage = '';
    this.errorMessage = '';
    this.loading = true;

    this.paymentService.processPayment({
      fromAccountId: this.fromAccountId,
      toAccountId: this.toAccountId,
      amount: this.amount,
      type: this.type,
      description: this.description
    }).subscribe({
      next: () => {
        this.successMessage = 'Payment processed successfully';
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Payment failed';
        this.loading = false;
      }
    });
  }
}

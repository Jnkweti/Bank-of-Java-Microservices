import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { PaymentService } from '../PaymentService';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';


@Component({
  selector: 'app-payment',
  imports: [FormsModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatSelectModule],
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

  constructor(private paymentService:PaymentService){}

  onSubmit(){
    this.successMessage = '';
    this.errorMessage = '';

    this.paymentService.processPayment({
      fromAccountId: this.fromAccountId,
      toAccountId: this.toAccountId,
      amount: this.amount,
      type: this.type,
      description: this.description
    }).subscribe({
      next: (res) => { this.successMessage = 'Payment processed successfully'; },
      error: (err) => { this.errorMessage = 'Payment failed'; }
    });
  }
}

import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../auth.service';
import { Customer } from '../../customers/customer';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { switchMap } from 'rxjs';

@Component({
  selector: 'app-register',
  imports: [FormsModule, RouterLink, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatProgressSpinnerModule],
  templateUrl: './register.html',
  styleUrl: './register.scss',
})
export class Register {
  email = '';
  password = '';
  firstName = '';
  lastName = '';
  address = '';
  birthDate = '';
  errorMessage = '';
  loading = false;

  constructor(
    private authService: AuthService,
    private customerService: Customer,
    private router: Router
  ) {}

  onRegister() {
    this.errorMessage = '';
    this.loading = true;

    this.authService.register(this.email, this.password).pipe(
      switchMap(res => {
        localStorage.setItem('token', res.accessToken);
        return this.customerService.createCustomer({
          firstName: this.firstName,
          lastName: this.lastName,
          address: this.address,
          email: this.email,
          birthDate: this.birthDate
        });
      })
    ).subscribe({
      next: () => { this.router.navigate(['/dashboard']); },
      error: (err) => {
        if (err.status === 401 || err.status === 409) {
          this.errorMessage = 'Registration failed. Email may already be in use.';
        } else {
          this.errorMessage = 'Account created but customer profile failed. Contact support.';
        }
        this.loading = false;
      }
    });
  }
}

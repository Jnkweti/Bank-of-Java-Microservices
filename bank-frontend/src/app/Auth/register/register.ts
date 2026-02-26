import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../auth.service';
import { Customer } from '../../customers/customer';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-register',
  imports: [FormsModule, RouterLink, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule],
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

  constructor(
    private authService: AuthService,
    private customerService: Customer,
    private router: Router
  ) {}

  onRegister() {
    this.errorMessage = '';

    this.authService.register(this.email, this.password).subscribe({
      next: (res: any) => {
        localStorage.setItem('token', res.accessToken);
        this.customerService.createCustomer({
          firstName: this.firstName,
          lastName: this.lastName,
          address: this.address,
          email: this.email,
          birthDate: this.birthDate
        }).subscribe({
          next: () => { this.router.navigate(['/dashboard']); },
          error: () => { this.errorMessage = 'Account created but customer profile failed. Contact support.'; }
        });
      },
      error: () => { this.errorMessage = 'Registration failed. Email may already be in use.'; }
    });
  }
}

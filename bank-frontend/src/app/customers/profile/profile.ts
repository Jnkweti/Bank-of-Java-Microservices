import { Component, ChangeDetectorRef, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Customer } from '../customer';
import { AuthService } from '../../Auth/auth.service';
import { NotificationService } from '../../shared/notification.service';
import { switchMap } from 'rxjs';

@Component({
  selector: 'app-profile',
  imports: [CommonModule, FormsModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './profile.html',
  styleUrl: './profile.scss',
})
export class Profile implements OnInit {
  customer: any = null;
  loading = true;
  saving = false;
  editing = false;

  editForm = {
    firstName: '',
    lastName: '',
    address: '',
  };

  constructor(
    private customerService: Customer,
    private authService: AuthService,
    private notify: NotificationService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    const email = this.authService.getEmailFromToken();
    if (!email) {
      this.loading = false;
      return;
    }

    this.customerService.getCustomerByEmail(email).pipe(
      switchMap((res: any) => this.customerService.getCustomerById(res.id))
    ).subscribe({
      next: (customer: any) => {
        this.customer = customer;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  startEditing() {
    this.editForm.firstName = this.customer.firstName;
    this.editForm.lastName = this.customer.lastName;
    this.editForm.address = this.customer.address;
    this.editing = true;
  }

  cancelEditing() {
    this.editing = false;
  }

  saveProfile() {
    this.saving = true;
    this.customerService.updateCustomer(this.customer.id, {
      firstName: this.editForm.firstName,
      lastName: this.editForm.lastName,
      email: this.customer.email,
      address: this.editForm.address,
      birthDate: this.customer.birthDate,
    }).subscribe({
      next: (updated: any) => {
        this.customer = updated;
        this.editing = false;
        this.saving = false;
        this.notify.success('Profile updated successfully.');
        this.cdr.detectChanges();
      },
      error: () => {
        this.saving = false;
        this.notify.error('Failed to update profile.');
        this.cdr.detectChanges();
      },
    });
  }
}
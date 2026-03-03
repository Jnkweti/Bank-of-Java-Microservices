import { Component, ChangeDetectorRef, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { Account } from '../account';
import { PaymentService } from '../../payments/PaymentService';
import { forkJoin, catchError, of, Subscription } from 'rxjs';
import { AccountModel, PaymentModel } from '../../shared/models';

@Component({
  selector: 'app-account-detail',
  imports: [CommonModule, RouterLink, MatCardModule, MatButtonModule, MatIconModule, MatProgressSpinnerModule],
  templateUrl: './account-detail.html',
  styleUrl: './account-detail.scss',
})
export class AccountDetail implements OnInit, OnDestroy {
  account: AccountModel | null = null;
  payments: PaymentModel[] = [];
  loading = true;
  private sub?: Subscription;

  constructor(
    private route: ActivatedRoute,
    private accountService: Account,
    private paymentService: PaymentService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.loading = false;
      return;
    }

    this.sub = forkJoin({
      account: this.accountService.getAccountById(id),
      payments: this.paymentService.getPaymentsByAccount(id).pipe(
        catchError(() => of([] as PaymentModel[]))
      ),
    }).subscribe({
      next: (result) => {
        this.account = result.account;
        this.payments = result.payments;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  ngOnDestroy() {
    this.sub?.unsubscribe();
  }
}
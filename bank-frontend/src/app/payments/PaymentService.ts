import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { PaymentModel } from '../shared/models';

@Injectable({
  providedIn: 'root',
})
export class PaymentService {
  private apiUrl = `${environment.apiUrl}/api/payments`;

  constructor(private http: HttpClient) {}

  processPayment(payment: Partial<PaymentModel>): Observable<PaymentModel> {
    return this.http.post<PaymentModel>(this.apiUrl, payment);
  }

  getPaymentsByAccount(accountId: string): Observable<PaymentModel[]> {
    return this.http.get<PaymentModel[]>(`${this.apiUrl}/account/${accountId}`);
  }
}
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class PaymentService {
  private apiUrl = 'http://localhost:8080/api/payments';

  constructor(private http: HttpClient) {}

  processPayment(payment: any): Observable<any> {
    return this.http.post(this.apiUrl, payment);
  }

  getPaymentsByAccount(accountId: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/account/${accountId}`);
  }
}

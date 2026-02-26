import { Injectable } from '@angular/core';
import { HttpClient} from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class Account {
  private apiUrl = 'http://localhost:8080/api/accounts';

  constructor(private http: HttpClient) { }

  getAccountsByCustomerId(customerId: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/customer/${customerId}`);
  }

  createAccount(account: any): Observable<any> {
    return this.http.post(this.apiUrl, account);
  }
}


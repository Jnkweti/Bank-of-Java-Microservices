import { Injectable } from '@angular/core';
import { HttpClient} from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class Account {
  private apiUrl = `${environment.apiUrl}/api/accounts`;

  constructor(private http: HttpClient) { }

  getAccountsByCustomerId(customerId: string): Observable<any> {
    return this.http.get(`${this.apiUrl}/customer/${customerId}`);
  }

  createAccount(account: any): Observable<any> {
    return this.http.post(this.apiUrl, account);
  }
}


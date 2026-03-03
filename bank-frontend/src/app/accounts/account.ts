import { Injectable } from '@angular/core';
import { HttpClient} from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AccountModel } from '../shared/models';

@Injectable({
  providedIn: 'root',
})
export class Account {
  private apiUrl = `${environment.apiUrl}/api/accounts`;

  constructor(private http: HttpClient) { }

  getAccountById(id: string): Observable<AccountModel> {
    return this.http.get<AccountModel>(`${this.apiUrl}/${id}`);
  }

  getAccountsByCustomerId(customerId: string): Observable<AccountModel[]> {
    return this.http.get<AccountModel[]>(`${this.apiUrl}/customer/${customerId}`);
  }

  createAccount(account: Partial<AccountModel>): Observable<AccountModel> {
    return this.http.post<AccountModel>(this.apiUrl, account);
  }
}
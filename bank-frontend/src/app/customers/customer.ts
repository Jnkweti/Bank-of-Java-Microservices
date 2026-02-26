import { Injectable } from '@angular/core';
import { HttpClient} from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class Customer {
  private apiUrl = `${environment.apiUrl}/api/customers`;

  constructor(private http: HttpClient) { }

  getCustomerByEmail(email: string) {
    return this.http.get(`${this.apiUrl}/email/${email}`);
  }

  createCustomer(customer: any): Observable<any> {
    return this.http.post(this.apiUrl, customer);
  }
}

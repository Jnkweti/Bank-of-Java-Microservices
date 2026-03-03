import { Injectable } from '@angular/core';
import { HttpClient} from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { CustomerModel, CustomerIdResponse } from '../shared/models';

@Injectable({
  providedIn: 'root',
})
export class Customer {
  private apiUrl = `${environment.apiUrl}/api/customers`;

  constructor(private http: HttpClient) { }

  getCustomerById(id: string): Observable<CustomerModel> {
    return this.http.get<CustomerModel>(`${this.apiUrl}/${id}`);
  }

  getCustomerByEmail(email: string): Observable<CustomerIdResponse> {
    return this.http.get<CustomerIdResponse>(`${this.apiUrl}/email/${email}`);
  }

  createCustomer(customer: Partial<CustomerModel>): Observable<CustomerModel> {
    return this.http.post<CustomerModel>(this.apiUrl, customer);
  }

  updateCustomer(id: string, customer: Partial<CustomerModel>): Observable<CustomerModel> {
    return this.http.put<CustomerModel>(`${this.apiUrl}/${id}`, customer);
  }
}
import { Injectable } from '@angular/core';
import { HttpClient} from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { AuthResponse } from '../shared/models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = `${environment.apiUrl}/auth`;

  constructor(private http:HttpClient) { }

  login(email:string, password:string): Observable<AuthResponse>{
    return this.http.post<AuthResponse>(`${this.apiUrl}/login`, {email:email, password:password});
  }

  register(email:string, password:string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/register`, {email:email, password:password});
  }

  getEmailFromToken():string | null {
    let token : any = localStorage.getItem('token');
    if(!token) return null;
    token = token.split('.');
    token = JSON.parse(atob(token[1])).email;
    return token;
  }
  isLoggedIn(): boolean{
    return !!localStorage.getItem('token');
  }
}
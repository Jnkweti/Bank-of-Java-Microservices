import { Injectable } from '@angular/core';
import { HttpClient} from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/auth';

  constructor(private http:HttpClient) { }

  login(email:string, password:string): Observable<any>{
    return this.http.post(`${this.apiUrl}/login`, {email:email, password:password});
  }

  register(email:string, password:string): Observable<any> {
    return this.http.post(`${this.apiUrl}/register`, {email:email, password:password});
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

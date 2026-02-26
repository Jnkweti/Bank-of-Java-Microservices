import { Component } from '@angular/core';
import { FormsModule} from '@angular/forms';
import {Router} from '@angular/router';
import {AuthService} from '../auth.service';
import {Observable} from 'rxjs';

@Component({
  selector: 'app-register',
  imports: [FormsModule],
  templateUrl: './register.html',
  styleUrl: './register.scss',
})
export class Register {
  email ='';
  password ='';
  errorMessage = '';

  constructor(private authService: AuthService, private router: Router) { }

  onRegister(){
    this.authService.register(this.email, this.password)
     .subscribe({next: (res) => {this.router.navigate(['/login'])},
       error: (err) => { this.errorMessage = 'Registration Failed'; }});
  }
}

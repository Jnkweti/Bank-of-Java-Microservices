import { Component } from '@angular/core';
import { FormsModule} from '@angular/forms';
import {Router} from '@angular/router';
import {AuthService} from '../auth.service';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.scss',

})
export class Login {
  email ='';
  password ='';
  errorMessage = '';

  constructor(private router:Router, private authService:AuthService) { }

  onLogin(){
    this.authService.login(this.email, this.password)
      .subscribe({next: (res) => {localStorage.setItem('token', res.accessToken);
          this.router.navigate(['/dashboard'])},
        error: (err) =>{ this.errorMessage = 'Invalid login credentials'}});

  }

}

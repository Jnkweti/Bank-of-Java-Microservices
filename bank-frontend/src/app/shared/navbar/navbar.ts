import { Component } from '@angular/core';
import { Router, RouterLink} from '@angular/router';
import { AuthService} from '../../Auth/auth.service';

import { MatToolbar } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, MatToolbar, MatButtonModule],
  templateUrl: './navbar.html',
  styleUrl: './navbar.scss',
})
export class Navbar {

  constructor(private authService: AuthService, private router: Router) { }

  isLoggedIn(): boolean{
    return this.authService.isLoggedIn();
  }

  logout() {
    localStorage.removeItem('token');
    this.router.navigate(['/login']);

  }
}

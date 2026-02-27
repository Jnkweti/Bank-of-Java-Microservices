import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { NotificationService } from './notification.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const notify = inject(NotificationService);
  const token = localStorage.getItem('token');

  if (!token) {
    return next(req);
  }

  return next(req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        localStorage.removeItem('token');
        router.navigate(['/login']);
        notify.error('Session expired. Please log in again.');
      } else if (error.status === 0) {
        notify.error('Unable to reach the server. Check your connection.');
      } else if (error.status >= 500) {
        notify.error('Something went wrong on our end. Try again later.');
      }
      return throwError(() => error);
    })
  );
};

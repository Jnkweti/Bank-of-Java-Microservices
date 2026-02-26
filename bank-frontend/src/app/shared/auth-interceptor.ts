import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  let token = localStorage.getItem('token');

  if (!token) {
    return next(req);
  }
  return next(req.clone({setHeaders: {Authorization: `Bearer ${token}`}}));

};

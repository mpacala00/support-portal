import { Injectable } from '@angular/core';
import {
   HttpRequest,
   HttpHandler,
   HttpEvent,
   HttpInterceptor
} from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthenticationService } from '../service/authentication.service';

//interceptor interceps requests and modifies them before they are sent to the server
@Injectable()
export class AuthInterceptor implements HttpInterceptor {

   constructor(private authService: AuthenticationService) { }

   intercept(httpRequest: HttpRequest<any>, httpHandler: HttpHandler): Observable<HttpEvent<any>> {
      if (httpRequest.url.includes(`${this.authService.host}/user/login`)) {
         return httpHandler.handle(httpRequest); //let the request continue its course
      }
      if (httpRequest.url.includes(`${this.authService.host}/user/register`)) {
         return httpHandler.handle(httpRequest); //let the request continue its course
      }

      //get the token from cookies and set it in authService
      this.authService.loadToken();
      const token = this.authService.getToken();
      const requestClone = httpRequest.clone({ setHeaders: { Authorization: `Bearer ${token}` } });

      return httpHandler.handle(requestClone);
   }
}

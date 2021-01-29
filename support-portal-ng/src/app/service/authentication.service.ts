import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { User } from '../model/user';
import { CookieService } from 'ngx-cookie-service';
import { JwtHelperService } from '@auth0/angular-jwt';

//providedIn: 'root' makes it unnecessary to put this service in providers array
@Injectable({
   providedIn: 'root'
})
export class AuthenticationService {
   public host = environment.baseAPIUrl;
   private token: string;
   private loggedUser: string;
   private jwtHelper = new JwtHelperService();

   constructor(private http: HttpClient, private cookies: CookieService) { }

   public login(user: User): Observable<HttpResponse<any> | HttpErrorResponse> {

      //post function args: (url, object (body), meta-data (like headers))
      //observe: 'response' will return the entire response with headers etc., without it we would only get body
      return this.http
         .post<HttpResponse<any> | HttpErrorResponse>(`${this.host}/user/login`, user, { observe: 'response' });
   }

   public register(user: User): Observable<HttpResponse<any> | HttpErrorResponse> {
      return this.http
         .post<HttpResponse<any> | HttpErrorResponse>(`${this.host}/user/register`, user, { observe: 'response' });
   }

   //using cookies for storage of JWT is safer than localStorage
   public logout(): void {
      this.token = null;
      this.loggedUser = null;
      this.cookies.deleteAll();
   }

   public isLoggedIn(): boolean {
      this.loadToken();

      //check if not empty
      if (this.token != null && this.token != '') {
         let sub = this.jwtHelper.decodeToken(this.token).sub;
         //check if sub not empty
         if (sub != null && sub != '') {
            //check if not expired
            if (!this.jwtHelper.isTokenExpired(this.token)) {
               //subject of the token is username, as was specified in the api
               this.loggedUser = this.jwtHelper.decodeToken(this.token).sub;
               return true;
            }
         }
      } else {
         this.logout();
         return false;
      }
   }

   public saveToken(token: string): void {
      this.token = token;

      //this.cookies.set(name, value, {expires?, path?, domain?, secure?, sameSite?})
      this.cookies.set('token', token);
   }

   public getToken(): string {
      return this.token;
   }

   public loadToken(): void {
      this.token = this.cookies.get('token');
   }

   public addUserToLocalCache(user: User): void {
      this.cookies.set('user', JSON.stringify(user));
   }

   public getUserFromLocalCache(): User {
      return JSON.parse(this.cookies.get('user'));
   }

}

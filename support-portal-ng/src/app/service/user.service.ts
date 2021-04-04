import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpEvent, HttpResponse } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { User } from '../model/user';
import { CookieService } from 'ngx-cookie-service';
import { JwtHelperService } from '@auth0/angular-jwt';
import { CustomHttpResponse } from '../model/custom-http-response';

@Injectable({
   providedIn: 'root'
})
export class UserService {
   private host = environment.baseAPIUrl;

   constructor(private http: HttpClient, private cookies: CookieService) { }

   public getUsers(): Observable<User[] | HttpErrorResponse> {
      return this.http.get<User[]>(`${this.host}/user/all`);
   }

   public addUser(formData: FormData): Observable<User | HttpErrorResponse> {
      return this.http.post<User>(`${this.host}/user/add`, formData);
   }

   public updateUser(formData: FormData): Observable<User | HttpErrorResponse> {
      return this.http.post<User>(`${this.host}/user/update`, formData);
   }

   //CustomHttpResponse is mapping for custom http response in the back-end
   public resetPassword(email: string): Observable<CustomHttpResponse | HttpErrorResponse> {
      return this.http.get<CustomHttpResponse>(`${this.host}/user/reset-password/${email}`);
   }

   public updateProfileImage(formData: FormData): Observable<HttpEvent<User> | HttpErrorResponse> {
      return this.http.post<User>(`${this.host}/user/update-profile-image`, formData,
         { //indication that the image is being processed
            reportProgress: true,
            observe: 'events'
         });
   }

   public deleteUser(userId: number): Observable<CustomHttpResponse | HttpErrorResponse> {
      return this.http.delete<CustomHttpResponse>(`${this.host}/user/delete/${userId}`);
   }

   public addUsersToCache(users: User[]): void {
      this.cookies.set('users', JSON.stringify(users));
   }

   public getUsersFromCache(): User[] {
      if (this.cookies.get('users')) {
         return JSON.parse(this.cookies.get('users'));
      }
      return null;
   }

   public createUserData(loggedInUsername: string, user: User, profileImage: File): FormData {
      const formData = new FormData();
      formData.append('currentUsername', loggedInUsername);
      formData.append('firstName', user.firstName);
      formData.append('lastName', user.lastName);
      formData.append('username', user.username);
      formData.append('email', user.email);
      formData.append('profileImage', profileImage);
      formData.append('isActive', JSON.stringify(user.active));
      formData.append('isNotLocked', JSON.stringify(user.notLocked));
      return formData;
   }
}

import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { NotificationType } from 'src/app/enum/notification-type.enum';
import { HeaderType } from 'src/app/enum/header-type.enum';
import { User } from 'src/app/model/user';
import { AuthenticationService } from 'src/app/service/authentication.service';
import { NotificationService } from 'src/app/service/notification.service';
import { FormControl, FormGroup } from '@angular/forms';

@Component({
   selector: 'app-login',
   templateUrl: './login.component.html',
   styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit, OnDestroy {
   public showLoading: boolean;

   private subscriptions: Subscription[] = [];

   constructor(private router: Router, private authService: AuthenticationService,
      private notificationService: NotificationService) { }

   form: FormGroup;

   ngOnInit(): void {
      if (this.authService.isUserLoggedIn()) {
         this.router.navigateByUrl('/user/management');
      } else {
         this.router.navigateByUrl('/login');
      }

      this.form = new FormGroup({
         username: new FormControl(''),
         password: new FormControl('')
      })
   }

   ngOnDestroy(): void {
      //unsubscribe to avoid memory leaks
      this.subscriptions.forEach(sub => sub.unsubscribe());
   }

   public onLogin(user: User): void {
      //user is created by putting form.value as an agument in the template
      //this function takes values from all inputs and makes a json outta them
      this.showLoading = true;
      this.subscriptions.push(
         this.authService.login(user).subscribe(
            (response: HttpResponse<User>) => { //if call is sucessful execute code in curly braces
               const token = response.headers.get(HeaderType.JWT_TOKEN);
               this.authService.saveToken(token);
               this.authService.addUserToLocalCache(response.body); //body will be of user type

               this.router.navigateByUrl('/user/management');
               this.showLoading = false;
            },
            (errorResponse: HttpErrorResponse) => {
               this.sendErrorNotification(NotificationType.ERROR, errorResponse.error.message);
               this.showLoading = false;
            }
         )
      );
   }

   private sendErrorNotification(type: NotificationType, message: string) {
      if (message) {
         this.notificationService.notify(type, message);
      } else {
         this.notificationService.notify(type, 'An unknown error occured.');
      }
   }

}

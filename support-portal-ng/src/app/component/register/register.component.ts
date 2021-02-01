import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { NotificationType } from 'src/app/enum/notification-type.enum';
import { User } from 'src/app/model/user';
import { AuthenticationService } from 'src/app/service/authentication.service';
import { NotificationService } from 'src/app/service/notification.service';

@Component({
   selector: 'app-register',
   templateUrl: './register.component.html',
   styleUrls: ['./register.component.scss']
})
export class RegisterComponent implements OnInit, OnDestroy {
   public showLoading: boolean;

   private subscriptions: Subscription[] = [];

   form: FormGroup;

   constructor(private router: Router, private authService: AuthenticationService,
      private notificationService: NotificationService) {

      if (this.authService.isUserLoggedIn()) { this.router.navigateByUrl('/user/management'); }

      this.form = new FormGroup({
         firstName: new FormControl(''),
         lastName: new FormControl(''),
         username: new FormControl(''),
         password: new FormControl(''),
         email: new FormControl('')
      })
   }

   ngOnDestroy(): void {
      this.subscriptions.forEach(sub => sub.unsubscribe());
   }

   ngOnInit(): void {
   }

   public onRegister(user: User): void {
      this.showLoading = true;
      this.subscriptions.push(
         this.authService.register(user).subscribe(
            (response: User) => {
               this.sendNotification(NotificationType.SUCCESS,
                  `Account for username ${response.username} created successfuly.`)
               this.showLoading = false;
               this.router.navigateByUrl('/login');
            },
            (errorResponse: HttpErrorResponse) => {
               console.log(errorResponse);
               this.sendNotification(NotificationType.ERROR, errorResponse.error.message);
               this.showLoading = false;
            }
         )
      );
   }

   private sendNotification(type: NotificationType, message: string) {
      if (message) {
         this.notificationService.notify(type, message);
      } else {
         this.notificationService.notify(type, 'An unknown error occured.');
      }
   }

}

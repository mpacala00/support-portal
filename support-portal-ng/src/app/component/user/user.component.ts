import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { BehaviorSubject, Subscription } from 'rxjs';
import { NotificationType } from 'src/app/enum/notification-type.enum';
import { User } from 'src/app/model/user';
import { NotificationService } from 'src/app/service/notification.service';
import { UserService } from 'src/app/service/user.service';

@Component({
   selector: 'app-user',
   templateUrl: './user.component.html',
   styleUrls: ['./user.component.scss']
})
export class UserComponent implements OnInit {

   public refreshing: boolean = false;
   public selectedUser: User;
   private subscriptions: Subscription[] = [];

   private titleSubject = new BehaviorSubject<string>('Users');
   //anytime titleSubject changes
   public titleAction$ = this.titleSubject.asObservable();

   upperForm: FormGroup;

   //displayed users
   public users: User[] = [];

   constructor(private userService: UserService, private notificationService: NotificationService) { }

   ngOnInit(): void {
      this.upperForm = new FormGroup({
         search: new FormControl('')
      })

      this.getUsers(true);
   }

   public getUsers(showNotification: boolean): void {
      this.refreshing = true;

      //using subs list to make it easy to destroy them
      this.subscriptions.push(
         this.userService.getUsers().subscribe(
            (res: User[]) => {
               this.userService.addUsersToCache(res);
               this.users = res;
               this.refreshing = false;
               if (showNotification) {
                  this.sendNotification(NotificationType.SUCCESS, `${res.length} user(s) loaded successfuly`);
               }
            },
            (err: HttpErrorResponse) => {
               this.sendNotification(NotificationType.ERROR, err.error.message);
               this.refreshing = false;
            }
         )
      );
   }

   public changeTitle(title: string): void {
      this.titleSubject.next(title); //.next() -> change the value
   }

   //open modal on click in the user table
   public onSelectUser(selectedUser: User): void {
      this.selectedUser = selectedUser;
      document.getElementById('openUserInfo').click(); //click because target is a button
   }

   private sendNotification(type: NotificationType, message: string) {
      if (message) {
         this.notificationService.notify(type, message);
      } else {
         this.notificationService.notify(type, 'An unknown error occured.');
      }
   }

}

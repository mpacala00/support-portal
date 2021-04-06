import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { BehaviorSubject, Observable, Subscription } from 'rxjs';
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

   private subscriptions: Subscription[] = [];

   public refreshing: boolean = false;
   public selectedUser: User;
   public profileImageFileName: string;
   public profileImage: File; //for updating profile pic

   private titleSubject = new BehaviorSubject<string>('Users');
   //anytime titleSubject changes
   public titleAction$ = this.titleSubject.asObservable();

   upperForm: FormGroup;
   newUserForm: FormGroup;

   //displayed users
   public users: User[] = [];

   constructor(private userService: UserService, private notificationService: NotificationService) { }

   ngOnInit(): void {
      this.upperForm = new FormGroup({
         search: new FormControl('')
      })

      this.newUserForm = new FormGroup({
         firstName: new FormControl(''),
         lastName: new FormControl(''),
         username: new FormControl(''),
         email: new FormControl(''),
         role: new FormControl('ROLE_USER'),
         //profileImage is directly put as an arg to a function
         isNotLocked: new FormControl(true),
         isActive: new FormControl(false)
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

   //new user post
   public onAddNewUser(): void {
      const formData = this.userService.createUserData(null, this.newUserForm.value, this.profileImage);
      this.subscriptions.push(this.userService.addUser(formData).subscribe(
         (res: User) => {
            this.clickButton('new-user-close'); //close modal
            this.getUsers(false); //refresh users without showing notification
            this.profileImage = null;
            this.profileImageFileName = null;
            this.newUserForm.reset(); //clear the form
            this.sendNotification(NotificationType.SUCCESS, `${res.firstName} ${res.lastName} created successfuly.`);
            console.log(res);
         },
         (err: HttpErrorResponse) => {
            this.sendNotification(NotificationType.ERROR, err.error.message);
            this.profileImage = null;
            this.profileImageFileName = null;
         }
      ));

   }

   public saveNewUser(): void {
      this.clickButton('new-user-save');
   }

   public changeTitle(title: string): void {
      this.titleSubject.next(title); //.next() -> change the value
   }

   //open modal on click in the user table
   public onSelectUser(selectedUser: User): void {
      this.selectedUser = selectedUser;
      this.clickButton('openUserInfo'); //click because target is a button
   }

   public onProfileImageChange(fileName: string, file: File): void {
      this.profileImageFileName = fileName;
      this.profileImage = file;
      console.log(fileName, file);
   }

   private sendNotification(type: NotificationType, message: string) {
      if (message) {
         this.notificationService.notify(type, message);
      } else {
         this.notificationService.notify(type, 'An unknown error occured.');
      }
   }

   private clickButton(buttonId: string): void {
      document.getElementById(buttonId).click();
   }

}

import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { BehaviorSubject, Observable, Subscription } from 'rxjs';
import { NotificationType } from 'src/app/enum/notification-type.enum';
import { CustomHttpResponse } from 'src/app/model/custom-http-response';
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

   newUserForm: FormGroup;
   editUserForm: FormGroup;

   //displayed users
   public users: User[] = [];

   public editUser = new User();
   private currentUsername: string;

   constructor(private userService: UserService, private notificationService: NotificationService) { }

   ngOnInit(): void {
      // simple input is enough for search functionality
      // this.upperForm = new FormGroup({
      //    search: new FormControl('')
      // })

      this.newUserForm = new FormGroup({
         firstName: new FormControl(''),
         lastName: new FormControl(''),
         username: new FormControl(''),
         email: new FormControl(''),
         role: new FormControl('ROLE_USER'),
         //profileImage is directly put as an arg to a function
         isActive: new FormControl(false),
         isNotLocked: new FormControl(true),
      })

      //values updated on edit user modal open
      this.editUserForm = new FormGroup({
         firstName: new FormControl(''),
         lastName: new FormControl(''),
         username: new FormControl(''),
         email: new FormControl(''),
         role: new FormControl(''),
         isNotLocked: new FormControl(false),
         isActive: new FormControl(false)
      });

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

   public test(test) {
      console.log(test);
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

   //my own implementation of editUserForm using reactive forms since author of the course
   //used ngModel
   public onUpdateUser(): void {
      const formData = this.userService.createUserData(this.currentUsername, this.editUserForm.value, this.profileImage);
      this.subscriptions.push(this.userService.updateUser(formData).subscribe(
         (res: User) => {
            this.clickButton('closeEditUserModalButton'); //close modal
            this.getUsers(false); //refresh users without showing notification
            this.profileImage = null;
            this.profileImageFileName = null;
            this.sendNotification(NotificationType.SUCCESS, `${res.firstName} ${res.lastName} updated successfuly.`);
         },
         (err: HttpErrorResponse) => {
            this.sendNotification(NotificationType.ERROR, err.error.message);
            this.profileImage = null;
            this.profileImageFileName = null;
         }
      ));
   }

   public onDeleteUser(id: number): void {
      this.subscriptions.push(this.userService.deleteUser(id).subscribe(
         (res: CustomHttpResponse) => {
            this.sendNotification(NotificationType.INFO, res.message);
            this.getUsers(true);
         },
         (err: HttpErrorResponse) => {
            this.sendNotification(NotificationType.ERROR, err.error.message);
         }
      ))
   }

   public onEditUser(user: User): void {
      this.editUser = user;
      this.currentUsername = user.username;
      //patch editUserForm values since it starts as a blank form
      this.editUserForm.patchValue(this.editUser);

      this.clickButton('openUserEdit');
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

   public searchUsers(searchTerm: string): void {
      const results: User[] = [];
      console.log(searchTerm);
      for (let user of this.userService.getUsersFromCache()) {
         //if indexOf returns anything but -1 it found searchTerm in the string
         if (user.firstName.toLowerCase().indexOf(searchTerm.toLowerCase()) !== -1 ||
            user.lastName.toLowerCase().indexOf(searchTerm.toLowerCase()) !== -1 ||
            user.username.toLowerCase().indexOf(searchTerm.toLowerCase()) !== -1 ||
            user.userId.toLowerCase().indexOf(searchTerm.toLowerCase()) !== -1) {
            results.push(user);
         }
      }
      this.users = results;
      if (results.length == 0 || !searchTerm) {
         this.users = this.userService.getUsersFromCache();
      }
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

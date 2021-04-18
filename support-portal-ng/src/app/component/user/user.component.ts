import { HttpErrorResponse, HttpEvent, HttpEventType, HttpResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, Subscription } from 'rxjs';
import { NotificationType } from 'src/app/enum/notification-type.enum';
import { CustomHttpResponse } from 'src/app/model/custom-http-response';
import { User } from 'src/app/model/user';
import { FileUploadStatus } from 'src/app/model/file-upload.status';
import { AuthenticationService } from 'src/app/service/authentication.service';
import { NotificationService } from 'src/app/service/notification.service';
import { UserService } from 'src/app/service/user.service';
import { Role } from 'src/app/enum/role.enum';
import { SubSink } from 'subsink';

@Component({
   selector: 'app-user',
   templateUrl: './user.component.html',
   styleUrls: ['./user.component.scss']
})
export class UserComponent implements OnInit, OnDestroy {

   // private subscriptions: Subscription[] = [];
   private subs = new SubSink();

   public refreshing: boolean = false;
   public selectedUser: User;
   public profileImageFileName: string;
   public profileImage: File; //for updating profile pic
   public fileStatus = new FileUploadStatus();

   private titleSubject = new BehaviorSubject<string>('Users');
   //anytime titleSubject changes
   public titleAction$ = this.titleSubject.asObservable();

   newUserForm: FormGroup;
   editUserForm: FormGroup;
   emailForm: FormGroup;
   profileUserForm: FormGroup;

   //displayed users
   public users: User[] = [];

   public loggedInUser: User;

   public editUser = new User();
   private currentUsername: string;

   constructor(private userService: UserService, private notificationService: NotificationService,
      private authService: AuthenticationService, private router: Router) { }

   ngOnInit(): void {
      // simple input is enough for search functionality
      // this.upperForm = new FormGroup({
      //    search: new FormControl('')
      // })
      this.loggedInUser = this.authService.getUserFromLocalCache();

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
         firstName: new FormControl({ value: '', disabled: !this.isManager() }),
         lastName: new FormControl({ value: '', disabled: !this.isManager() }),
         username: new FormControl({ value: '', disabled: !this.isManager() }),
         email: new FormControl({ value: '', disabled: !this.isManager() }),
         role: new FormControl({ value: '', disabled: !this.isAdmin() }),
         isNotLocked: new FormControl({ value: false, disabled: !this.isManager() }),
         isActive: new FormControl({ value: false, disabled: !this.isManager() })
      });

      this.emailForm = new FormGroup({
         email: new FormControl('')
      })

      this.profileUserForm = new FormGroup({
         firstName: new FormControl(''),
         lastName: new FormControl(''),
         username: new FormControl(''),
         email: new FormControl(''),
         role: new FormControl(''),
         isNotLocked: new FormControl(false),
         isActive: new FormControl(false)
      })

      this.getUsers(true);
   }

   ngOnDestroy(): void {
      //unsubscribe to avoid memory leaks
      // this.subscriptions.forEach(sub => sub.unsubscribe());
      //unsubscribe using SubSink library
      this.subs.unsubscribe();
   }

   public getUsers(showNotification: boolean): void {
      this.refreshing = true;

      //using subs list to make it easy to destroy them
      this.subs.add(
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

   public updateProfileImage(): void {
      this.clickButton('profile-image-input');
   }

   public onUpdateProfileImage(): void {
      const formData = new FormData();
      formData.append('username', this.loggedInUser.username);
      formData.append('profileImage', this.profileImage);

      this.subs.add(this.userService.updateProfileImage(formData).subscribe(
         (event: HttpEvent<any>) => {
            this.reportUploadProgress(event);
         },
         (err: HttpErrorResponse) => {
            this.sendNotification(NotificationType.ERROR, err.error.message);
            this.profileImage = null;
            this.profileImageFileName = null;
         }
      ));
   }

   private reportUploadProgress(event: HttpEvent<any>): void {
      switch (event.type) {
         case HttpEventType.UploadProgress:
            this.fileStatus.percentage = Math.round((100 * event.loaded / event.total));
            this.fileStatus.status = 'progress';
            break;

         case HttpEventType.Response:
            if (event.status === 200) {
               this.loggedInUser.profileImageUrl = `${event.body.profileImageUrl}?time=${new Date().getTime()}`;
               this.sendNotification(NotificationType.SUCCESS, `Your profile image has been updated`);
               this.fileStatus.status = 'done';
               break;
            } else {
               this.sendNotification(NotificationType.ERROR, `Unable to upload image. Please try again`);
               break;
            }
         default:
            'Finished all processes';
      }
   }

   public onLogOut(): void {
      this.authService.logout();
      this.router.navigate(['/login']);
      this.sendNotification(NotificationType.SUCCESS, `You've been successfully logged out`);
   }

   //new user post
   public onAddNewUser(): void {
      const formData = this.userService.createUserData(null, this.newUserForm.value, this.profileImage);
      this.subs.add(this.userService.addUser(formData).subscribe(
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
      this.subs.add(this.userService.updateUser(formData).subscribe(
         (res: User) => {
            this.clickButton('closeEditUserModalButton'); //close modal
            this.getUsers(false); //refresh users without showing notification
            this.profileImage = null;
            this.profileImageFileName = null;
            this.sendNotification(NotificationType.SUCCESS, `${res.firstName} ${res.lastName} updated successfuly.`);
         },
         (err: HttpErrorResponse) => {
            this.sendNotification(NotificationType.ERROR, err.error.message);
            this.fileStatus.status = 'done';
            this.profileImage = null;
            this.profileImageFileName = null;
         }
      ));
   }

   public onDeleteUser(username: string): void {
      this.subs.add(this.userService.deleteUser(username).subscribe(
         (res: CustomHttpResponse) => {
            this.sendNotification(NotificationType.INFO, res.message);
            this.getUsers(false);
         },
         (err: HttpErrorResponse) => {
            this.sendNotification(NotificationType.ERROR, err.error.message);
         }
      ))
   }

   public onUpdateCurrentUser(user: User): void {
      this.refreshing = true;
      this.currentUsername = this.authService.getUserFromLocalCache().username;

      const formData = this.userService.createUserData(this.currentUsername, user, this.profileImage);

      this.subs.add(this.userService.updateUser(formData).subscribe(
         (res: User) => {
            //update user in the cache
            this.authService.addUserToLocalCache(res);
            this.getUsers(false);
            this.profileImage = null;
            this.profileImageFileName = null;
            this.sendNotification(NotificationType.SUCCESS, `${res.firstName} ${res.lastName} updated successfuly.`);
            this.loggedInUser = res;
            this.refreshing = false;
         },
         (err: HttpErrorResponse) => {
            this.sendNotification(NotificationType.ERROR, err.error.message);
            this.profileImage = null;
            this.profileImageFileName = null;
            this.refreshing = false;
         }
      ));
   }

   public onResetPassword(emailForm: FormGroup): void {
      this.refreshing = true;
      const emailAddress = emailForm.value['email'];

      this.subs.add(
         this.userService.resetPassword(emailAddress).subscribe(
            (res: CustomHttpResponse) => {
               this.refreshing = false;
               this.sendNotification(NotificationType.SUCCESS, res.message);
            },
            (err: HttpErrorResponse) => {
               this.refreshing = false;
               this.sendNotification(NotificationType.WARNING, err.error.message);
            },
            // exceture on complete: whether or not the call was successful
            () => emailForm.reset()
         )
      )
   }

   public onEditUser(user: User): void {
      this.editUser = user;
      this.currentUsername = user.username;
      //patch editUserForm values since it starts as a blank form
      this.editUserForm.patchValue(this.editUser);

      this.clickButton('openUserEdit');
   }

   public onProfileEnter(): void {
      this.changeTitle('Profile');
      this.profileUserForm.patchValue(this.loggedInUser);
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

   public isAdmin(): boolean {
      return this.getUserRole() === Role.ADMIN || this.getUserRole() === Role.SUPER_ADMIN;
   }

   public isManager(): boolean {
      return this.isAdmin() || this.getUserRole() === Role.MANAGER;
   }

   private getUserRole(): string {
      return this.authService.getUserFromLocalCache().role;
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

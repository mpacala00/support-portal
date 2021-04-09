import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree, Router } from '@angular/router';
import { NotificationType } from '../enum/notification-type.enum';

import { AuthenticationService } from '../service/authentication.service';
import { NotificationService } from '../service/notification.service';

@Injectable({
   providedIn: 'root'
})
export class AuthenticationGuard implements CanActivate {

   constructor(private authService: AuthenticationService, private router: Router,
      private notificationService: NotificationService) { }

   canActivate(
      next: ActivatedRouteSnapshot,
      state: RouterStateSnapshot): boolean {
      return this.isUserLoggedIn();
   }


   private isUserLoggedIn(): boolean {
      console.log("isUserLoggedIn() running...");
      if (this.authService.isUserLoggedIn()) { return true; }
      this.router.navigateByUrl('login');
      this.notificationService.notify(NotificationType.ERROR, "You need to log in to access this page.");
      return false;
   }
}

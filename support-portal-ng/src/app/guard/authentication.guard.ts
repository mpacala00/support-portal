import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, UrlTree, Router } from '@angular/router';
import { Observable } from 'rxjs';

import { AuthenticationService } from '../service/authentication.service';

@Injectable({
   providedIn: 'root'
})
export class AuthenticationGuard implements CanActivate {

   constructor(private authService: AuthenticationService, private router: Router) { }

   canActivate(
      next: ActivatedRouteSnapshot,
      state: RouterStateSnapshot): boolean {
      return this.isUserLoggedIn();
   }


   private isUserLoggedIn(): boolean {
      if (this.authService.isUserLoggedIn) { return true; }
      this.router.navigate['/login'];
      //send some notification
      return false;
   }
}

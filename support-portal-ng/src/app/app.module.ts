import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { NotificationModule } from './notification.module';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { CookieService } from 'ngx-cookie-service';

import { NotificationService } from './service/notification.service';
import { AuthenticationService } from './service/authentication.service';
import { UserService } from './service/user.service';
import { AuthInterceptor } from './interceptor/auth.interceptor';
import { AuthenticationGuard } from './guard/authentication.guard';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { LoginComponent } from './component/login/login.component';
import { RegisterComponent } from './component/register/register.component';
import { UserComponent } from './component/user/user.component';


@NgModule({
   declarations: [
      AppComponent,
      LoginComponent,
      RegisterComponent,
      UserComponent
   ],
   imports: [
      BrowserModule,
      AppRoutingModule,
      ReactiveFormsModule,
      HttpClientModule,
      NotificationModule

   ],
   providers: [NotificationService, CookieService, AuthenticationService, UserService, AuthenticationGuard,
      { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true }], //multi - many instances across many files
   bootstrap: [AppComponent]
})
export class AppModule { }

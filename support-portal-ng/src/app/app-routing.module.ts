import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { LoginComponent } from './component/login/login.component';
import { RegisterComponent } from './component/register/register.component';
import { UserComponent } from './component/user/user.component';
import { AuthenticationGuard } from './guard/authentication.guard';

const routes: Routes = [
   { path: 'login', component: LoginComponent },
   { path: 'register', component: RegisterComponent },
   //canActivate : array of objects implementing CanActivate that implement a canActivate() function
   { path: 'user/management', component: UserComponent, canActivate: [AuthenticationGuard] },
   //order is important, this has to be last one
   { path: '', redirectTo: '/login', pathMatch: 'full' }
];

@NgModule({
   imports: [RouterModule.forRoot(routes)],
   exports: [RouterModule]
})
export class AppRoutingModule { }

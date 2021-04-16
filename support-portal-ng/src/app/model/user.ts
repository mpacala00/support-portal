export class User {
   userId: string;
   firstName: string;
   lastName: string;
   username: string;
   email: string;
   loginDateDisplay: Date;
   joinDate: Date;
   profileImageUrl: string;
   isActive: boolean;
   isNotLocked: boolean;
   role: string;
   authorities: [];

   constructor() {
      this.firstName = '';
      this.lastName = '';
      this.username = '';
      this.email = '';
      this.isActive = false;
      this.isNotLocked = false;
      this.role = '';
      this.authorities = [];
   }
}
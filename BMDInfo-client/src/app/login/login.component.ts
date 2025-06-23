import { Component } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {

  
  constructor(
    private router: Router
  ) {}


  allUser = [
    {"userName": "admin"},
    {"password": "admin"},
  ];

  ngOnInit(): void {
    // this.service.getAllUsers().subscribe({
    //   next: (r) => {
    //     this.allUser = r;
    //   },
    //   error: (err) => {
    //     alert(err);
    //   },
    // });
  }

  message:string = "";

  formSubmit(data: NgForm) {
    console.log(data.value.name);
    console.log(data.value.password);

    console.log(this.allUser.at(0)?.userName);
    console.log(this.allUser.at(1)?.password);
    console.log();
    
    
    
    // console.log(this.allUser);


    if (this.allUser.at(0)?.userName === data.value.name && this.allUser.at(1)?.password === data.value.password) {

      sessionStorage.removeItem('admin');
      sessionStorage.setItem('admin', data.value);

      this.message = "Login Successfull! "
      this.showMessage();

      this.router.navigate(['/dashboard']);

    } else {
      this.message = 'User Name Or Password Is InCorrect..'
      alert( 'User Name Or Password Is InCorrect..')
    }
  }

  
  showMessageFlag: boolean = false;


public  closeMessage() {
    this.showMessageFlag = false;
  }
 public showMessage() {
    this.showMessageFlag = true;
    setTimeout(() => {
      this.showMessageFlag = false;
    }, 3000);
  }


}

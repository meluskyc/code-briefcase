import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AppRoutes } from './app-routes';
import { RouterModule } from '@angular/router';

@NgModule({
  imports: [
    CommonModule,
    RouterModule.forRoot(AppRoutes)
  ],
  declarations: [],
  exports: [RouterModule]
})
export class AppRoutingModule { }

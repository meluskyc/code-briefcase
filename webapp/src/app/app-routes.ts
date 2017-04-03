import { Routes } from '@angular/router';
import { ListComponent } from './list/list.component';
import { DetailComponent } from './detail/detail.component';
import { Config } from './app-config';

export const AppRoutes: Routes = [
  { path: '',  component: ListComponent, data: {id: Config.Views.List} },
  { path: 'add', component: DetailComponent, data: {id: Config.Views.Add} },
  { path: 'detail/:id',  component: DetailComponent, data: {id: Config.Views.Detail} }
];

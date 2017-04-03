import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';

import { AppComponent } from './app.component';
import { MaterialModule } from '@angular/material';
import { AppRoutingModule } from './app-routing.module';
import { ListComponent } from './list/list.component';
import { NavigationComponent } from './navigation/navigation.component';
import { environment } from '../environments/environment';
import { InMemoryWebApiModule } from 'angular-in-memory-web-api';
import { MockDataOverrideService } from './mock-data.service';
import { DetailComponent } from './detail/detail.component';
import { CommonModule } from '@angular/common';
import { AceEditorModule } from 'ng2-ace-editor';
import { ConfirmDenyDialogComponent } from './detail/confirm-deny-dialog.component';

declare const ace;

@NgModule({
  declarations: [
    AppComponent,
    ListComponent,
    NavigationComponent,
    DetailComponent,
    ConfirmDenyDialogComponent
  ],
  entryComponents: [ConfirmDenyDialogComponent],
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule,
    MaterialModule,
    AppRoutingModule,
    CommonModule,
    AceEditorModule,
    environment.production ? [] : InMemoryWebApiModule.forRoot(MockDataOverrideService)
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {
}

ace.config.set('modePath', 'ace-modes');

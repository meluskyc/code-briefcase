import { Component, OnInit } from '@angular/core';
import { MdDialogRef } from '@angular/material';

@Component({
  selector: 'app-confirm-deny-dialog',
  templateUrl: './confirm-deny-dialog.component.html',
  styleUrls: ['./confirm-deny-dialog.component.css']
})
export class ConfirmDenyDialogComponent implements OnInit {
  static readonly confirmed = 1;
  static readonly denied = 2;
  title: string;
  content: string;
  confirmText: string;
  denyText: string;

  constructor(public dialogRef: MdDialogRef<ConfirmDenyDialogComponent>) { }

  ngOnInit() {
    this.title = this.dialogRef.config.data.title;
    this.content = this.dialogRef.config.data.content;
    this.confirmText = this.dialogRef.config.data.confirmText;
    this.denyText = this.dialogRef.config.data.denyText;
  }

  confirm(): number {
    return ConfirmDenyDialogComponent.confirmed;
  }

  deny(): number {
    return ConfirmDenyDialogComponent.denied;
  }

}

import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ConfirmDenyDialogComponent } from './confirm-deny-dialog.component';

describe('ConfirmDenyDialogComponent', () => {
  let component: ConfirmDenyDialogComponent;
  let fixture: ComponentFixture<ConfirmDenyDialogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ ConfirmDenyDialogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConfirmDenyDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

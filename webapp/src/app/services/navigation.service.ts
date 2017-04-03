import { Injectable } from '@angular/core';
import { Subject } from 'rxjs/Subject';
import { Location } from '@angular/common';

@Injectable()
export class NavigationService {
  private actionSource = new Subject<number>();

  action$ = this.actionSource.asObservable();

  constructor(private location: Location) { }

  performAction(action: number) {
    this.actionSource.next(action);
  }

  goBack(): void {
    this.location.back();
  }

}

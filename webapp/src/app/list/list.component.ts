import { Component, OnDestroy, OnInit } from '@angular/core';
import { ItemService } from '../services/item.service';
import { Item } from '../models/item';
import { NavigationService } from '../services/navigation.service';
import { Control } from '../models/control';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { MdSnackBar } from '@angular/material';
import * as Moment from 'moment';
import { NavigationComponent } from '../navigation/navigation.component';

enum Actions { GoToAdd }

@Component({
  selector: 'app-list',
  templateUrl: './list.component.html',
  styleUrls: ['./list.component.css']
})
export class ListComponent implements OnInit, OnDestroy {
  static readonly backEnabled = false;
  static readonly heading = 'Code Briefcase';
  static readonly controls: Control[] = [];
  static readonly fab: Control = {icon: 'add', action: Actions.GoToAdd};
  filter: string;
  filteredItems: Item[];
  items: Item[];
  private actionsSub: any;

  constructor(private itemService: ItemService,
              private navigationService: NavigationService,
              private router: Router,
              private snackbar: MdSnackBar,
              private activatedRoute: ActivatedRoute) {}

  ngOnInit() {
    this.itemService.getItems()
      .subscribe(
        items => {
          this.items = items;
          this.filteredItems = this.items;
        },
        err => {
          this.snackbar.open('Unable to star. ' +
            'Check connection and try again.', '', {duration: 500});
        },
        () => this.filterItems()
      );

    this.actionsSub = this.navigationService.action$
      .subscribe(
        (action: number) => {
          switch (action) {
            case Actions.GoToAdd:
              this.goToAdd();
              break;
            default:
              console.log(`Invalid action: ${action}`);
          }
        }
      );
  }

  ngOnDestroy(): void {
    this.actionsSub.unsubscribe();
  }

  goToAdd(): void {
    this.router.navigate(['/add']);
  }

  star(item: Item): void {
    const next = item.starred !== 1 ? 1 : 0;
    item.starred = next;
    this.itemService
      .update(item)
      .subscribe(
        next => {},
        err => {
          item.starred = next !== 1 ? 1 : 0;
          this.snackbar.open('Unable to star. ' +
            'Check connection and try again.', '', {duration: 500});
        }
      );
  }

  convertTime(time: number): string {
    return Moment(time, 'x').fromNow();
  }

  private filterItems(): void {
    this.activatedRoute.queryParams.subscribe( (params: Params) => {
      const filter = params['filter'];
      this.filter = filter;
      if (this.filter === NavigationComponent.Starred) {
        this.filteredItems = this.items.filter(
          (item) => item.starred === 1
        );
      } else {
        this.filteredItems = this.items.filter(
          (item) => filter === undefined
          || item.tag_primary === filter
        );
      }
    });
  }
}

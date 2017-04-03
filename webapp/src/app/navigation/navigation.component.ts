import { Component, OnInit } from '@angular/core';
import { NavigationService } from '../services/navigation.service';
import { Control } from '../models/control';
import { ItemService } from '../services/item.service';
import { ActivatedRoute, NavigationEnd, Params, Router } from '@angular/router';
import { ListComponent } from '../list/list.component';
import { Config } from '../app-config';
import { DetailComponent } from '../detail/detail.component';
import { Item } from '../models/item';
import 'rxjs/add/operator/mergeMap';

@Component({
  selector: 'app-navigation',
  templateUrl: './navigation.component.html',
  styleUrls: ['./navigation.component.css'],
  providers: [NavigationService, ItemService]
})
export class NavigationComponent implements OnInit {
  static readonly Starred = 'Starred';
  heading: string;
  backEnabled: boolean;
  controls: Control[];
  fab: Control;
  filter: string;
  itemTags: Item[];

  constructor(private navigationService: NavigationService,
              private itemService: ItemService,
              private router: Router,
              private activatedRoute: ActivatedRoute) {
    this.activatedRoute.queryParams.subscribe( (params: Params) => {
      const filter = params['filter'];
      this.filter = filter;
    });
  }

  ngOnInit(): void {
    this.router.events
      .filter(event => event instanceof NavigationEnd)
      .map(() => this.activatedRoute)
      .map(route => {
        while (route.firstChild) {
          route = route.firstChild;
        }
        return route;
      })
      .filter(route => route.outlet === 'primary')
      .mergeMap(route => route.data)
      .subscribe((event) => {
        this.setupView(event['id']);
        if (event['id'] === Config.Views.List) {
          this.updateTags();
        }
      }
    );


  }

  private setupView(id: any): void {
    switch (id) {
      case Config.Views.List:
        this.heading = ListComponent.heading;
        this.controls = ListComponent.controls;
        this.backEnabled = ListComponent.backEnabled;
        this.fab = ListComponent.fab;
        break;
      case Config.Views.Add:
        this.heading = DetailComponent.addHeading;
        this.controls = DetailComponent.addControls;
        this.backEnabled = DetailComponent.backEnabled;
        this.fab = DetailComponent.fab;
        break;
      case Config.Views.Detail:
        this.heading = DetailComponent.detailsHeading;
        this.controls = DetailComponent.detailsControls;
        this.backEnabled = DetailComponent.backEnabled;
        this.fab = DetailComponent.fab;
        break;
      default:
        console.log(`Invalid route ID: ${id}`);
    }
  }

  goBack(): void {
    this.navigationService.goBack();
  }

  performAction(action: number): void {
    this.navigationService.performAction(action);
  }

  updateTags(): void {
    this.itemService.getItemTagsDistinct()
      .subscribe(
        tags => this.itemTags = tags,
        err => console.log(err)
      );
  }

}

import { AfterViewInit, Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { ItemService } from '../services/item.service';
import { Item } from '../models/item';
import { NavigationService } from '../services/navigation.service';
import { Control } from '../models/control';
import { Tag } from '../models/tag';
import { TagService } from '../services/tag.service';
import { MdDialog, MdDialogConfig, MdSnackBar } from '@angular/material';
import { AceEditorComponent } from 'ng2-ace-editor';
import { ConfirmDenyDialogComponent } from './confirm-deny-dialog.component';

enum Actions { Create, Update, Delete }

@Component({
  selector: 'app-detail',
  templateUrl: './detail.component.html',
  styleUrls: ['./detail.component.css'],
  providers: [TagService]
})
export class DetailComponent implements OnInit, OnDestroy, AfterViewInit {
  static readonly backEnabled = true;
  static readonly fab = undefined;
  static readonly detailsHeading = 'Details';
  static readonly addHeading = 'Add';
  static readonly detailsControls: Control[] =
    [{icon: 'done', action: Actions.Update},
      {icon: 'delete_forever', action: Actions.Delete}];
  static readonly addControls: Control[] =
    [{icon: 'done', action: Actions.Create}];

  @ViewChild('editor') editor: AceEditorComponent;
  item = new Item();
  tags: Tag[] = [];
  private actionsSub: any;

  constructor(private activatedRoute: ActivatedRoute,
              private itemService: ItemService,
              private tagService: TagService,
              private navigationService: NavigationService,
              private router: Router,
              private dialog: MdDialog,
              private snackbar: MdSnackBar) {}

  ngOnInit() {
    this.activatedRoute.params.subscribe( (params: Params) => {
      const id = params['id'];
      if (id) {
        this.itemService.getItem(id).subscribe(
          item => this.item = item,
          err => console.log(err),
          () => this.editor.setMode(this.item.ace_mode)
        );
      }
    });

    this.tagService.getTags().subscribe(
      tags => this.tags = tags,
      err => console.log(err)
    );

    this.actionsSub = this.navigationService.action$
      .subscribe(
        (action: number) => {
          switch (action) {
            case Actions.Create:
              this.createItem();
              break;
            case Actions.Update:
              this.updateItem();
              break;
            case Actions.Delete:
              this.deleteItem();
              break;
            default:
              console.log(`Invalid action: ${action}`);
          }
        },
        err => console.log(err)
      );
  }

  ngOnDestroy(): void {
    this.actionsSub.unsubscribe();
  }

  ngAfterViewInit(): void {
    this.editor.setTheme('eclipse');
    this.editor.getEditor().$blockScrolling = Infinity;
    this.editor.setOptions({
      fontSize : '16px',
      showGutter : true,
      wrap : true
    });
  }

  updateItem(): void {
    this.itemService
      .update(this.item)
      .subscribe(
        next => {},
        err => {
          this.snackbar.open('Unable to update. ' +
            'Check connection and try again.', '', {duration: 500});
        },
        () => {
          this.router.navigate(['/']);
          this.snackbar.open('Updated!', '', {duration: 1000});
        }
      );
  }

  createItem(): void {
    this.itemService
      .create(this.item)
      .subscribe(
        next => {},
        err => {
          this.snackbar.open('Unable to add. ' +
            'Check connection and try again.', '', {duration: 500});
        },
        () => {
          this.router.navigate(['/']);
          this.snackbar.open('Created!', '', {duration: 1000});
        }
      );
  }

  deleteItem(): void {
    const config = new MdDialogConfig();
    config.data = {
      title: 'Delete',
      content: `Delete item ${this.item.id}?`,
      confirmText: 'Yes',
      denyText: 'No'
    };
    const confirmDialog = this.dialog.open(ConfirmDenyDialogComponent, config);

    confirmDialog.afterClosed().subscribe(result => {
      switch (result) {
        case ConfirmDenyDialogComponent.confirmed:
          this.itemService.delete(this.item.id).subscribe(
            next => {},
            err => {
              this.snackbar.open('Unable to delete. ' +
                'Check connection and try again.', '', {duration: 500});
            },
            () => {
              this.router.navigate(['/']);
              this.snackbar.open('Delete successful', '', {duration: 500});
            }
          );
          break;
        default:
          break;
      }
    });
  }

}

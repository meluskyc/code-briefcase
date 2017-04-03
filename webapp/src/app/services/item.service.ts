import { Injectable } from '@angular/core';
import { Headers, Http, Response } from '@angular/http';
import { Item } from '../models/item';
import { Observable } from 'rxjs/Observable';
import { Tag } from '../models/tag';
import 'rxjs/add/operator/toPromise';
import { Config } from '../app-config';

@Injectable()
export class ItemService {
  private itemsUri  = `${Config.apiUri}/items`;
  private jsonHeaders = new Headers({'Content-Type': 'application/json'});

  constructor(private http: Http) { }

  public getItems(): Observable<Item[]> {
    return this.http
      .get(this.itemsUri)
      .map((res: Response) => res.json() as Observable<Item[]>)
      .catch((error: any) => Observable.throw(error.json().error || 'Server error'));
  }

  public getItem(id: number): Observable<Item> {
    const url = `${this.itemsUri}/${id}`;
    return this.http
      .get(url)
      .map((res: Response) => res.json() as Observable<Item[]>)
      .catch((error: any) => Observable.throw(error.json().error || 'Server error'));
  }

  public getItemTagsDistinct(): Observable<Item[]> {
    return this.http
      .get(`${this.itemsUri}/tags`)
      .map((res: Response) => res.json() as Observable<Tag[]>)
      .catch((error: any) => Observable.throw(error.json().error || 'Server error'));
  }

  public create(item: Item): Observable<any> {
    return this.http
      .post(this.itemsUri, JSON.stringify(item), {headers: this.jsonHeaders})
      .catch((error: any) => Observable.throw(error.json().error || 'Server error'));
  }

  public update(item: Item): Observable<any> {
    return this.http
      .put(`${this.itemsUri}/${item.id}`, JSON.stringify(item), {headers: this.jsonHeaders})
      .catch((error: any) => Observable.throw(error.json().error || 'Server error'));
  }

  public delete(id: number): Observable<any> {
    return this.http
      .delete(`${this.itemsUri}/${id}`, {headers: this.jsonHeaders})
      .catch((error: any) => Observable.throw(error.json().error || 'Server error'));
  }
}

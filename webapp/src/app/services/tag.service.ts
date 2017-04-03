import { Injectable } from '@angular/core';
import { Http, Response } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { Tag } from '../models/tag';
import { Config } from '../app-config';

@Injectable()
export class TagService {
  private tagsUri  = `${Config.apiUri}/tags`;

  constructor(private http: Http) { }

  getTags(): Observable<Tag[]> {
    return this.http
      .get(this.tagsUri)
      .map((res: Response) => res.json() as Observable<Tag[]>)
      .catch((error: any) => Observable.throw(error.json().error || 'Server error'));
  }

}

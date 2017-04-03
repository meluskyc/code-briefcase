import { Injectable } from '@angular/core';
import {
  createErrorResponse,
  emitResponse,
  HttpMethodInterceptorArgs,
  InMemoryDbService,
  ParsedUrl,
  RequestInfo,
  STATUS
} from 'angular-in-memory-web-api';
import { mockData } from './mock-data';
import { Item } from './models/item';
import { Tag } from './models/tag';
import { RequestMethod, Response, ResponseOptions, URLSearchParams } from '@angular/http';
import { Observable } from 'rxjs/Observable';
import { Observer } from 'rxjs/Observer';
import * as Moment from 'moment';

@Injectable()
export class MockDataService implements InMemoryDbService {
  createDb(): {} {
    console.log('setting up');
    const items: Item[] = mockData.items;
    const tags: Tag[] = mockData.tags;

    return {items, tags};
  }

  constructor() { }

}

@Injectable()
export class MockDataOverrideService extends MockDataService {
  // parseUrl override
  parseUrl(url: string): ParsedUrl {
    try {
      const loc = this.getLocation(url);
      console.log('url = ' + loc);
      let drop = 0;
      let urlRoot = '';
      if (loc.host !== undefined) {
        // url for a server on a different host!
        // assume it's collection is actually here too.
        drop = 1; // the leading slash
        urlRoot = loc.protocol + '//' + loc.host + '/';
      }
      const path = loc.pathname.substring(drop);
      let [base, collectionName, id] = path.split('/');
      const resourceUrl = urlRoot + base + '/' + collectionName + '/';
      [collectionName] = collectionName.split('.'); // ignore anything after the '.', e.g., '.json'
      const query = loc.search && new URLSearchParams(loc.search.substr(1));

      const result = { base, collectionName, id, query, resourceUrl };
      console.log('override parseUrl:');
      console.log(result);
      return result;
    } catch (err) {
      const msg = `unable to parse url '${url}'; original error: ${err.message}`;
      throw new Error(msg);
    }
  }

  // intercept response from the default HTTP method handlers
  responseInterceptor(response: ResponseOptions, reqInfo: RequestInfo) {
    const method = RequestMethod[reqInfo.req.method].toUpperCase();
    const body = JSON.stringify(response.body);
    console.log(`responseInterceptor: ${method} ${reqInfo.req.url}: \n${body}`);
    return response;
  }

  // HTTP GET interceptor
  protected get(interceptorArgs: HttpMethodInterceptorArgs) {
    // Returns a "cold" observable that won't be executed until something subscribes.
    return new Observable<Response>((responseObserver: Observer<Response>) => {
      console.log('HTTP GET override');
      let resOptions: ResponseOptions;

      const {id, query, collection, collectionName, headers, req} = interceptorArgs.requestInfo;
      let data = collection;

      if (id) {
        data = this.findById(collection, id);
      } else if (query) {
        data = this.applyQuery(collection, query);
      }

      if (data) {
        resOptions = new ResponseOptions({
          body: this.clone(data),
          headers: headers,
          status: STATUS.OK
        });
      } else {
        resOptions = createErrorResponse(req, STATUS.NOT_FOUND,
          `'${collectionName}' with id='${id}' not found`);
      }

      emitResponse(responseObserver, req, resOptions);
      return () => { }; // unsubscribe function
    });
  }

  // HTTP PUT interceptor
  protected put(interceptorArgs: HttpMethodInterceptorArgs) {
    // Returns a "cold" observable that won't be executed until something subscribes.
    return new Observable<Response>((responseObserver: Observer<Response>) => {
      console.log('HTTP PUT override');
      let resOptions: ResponseOptions;

      const {id, query, collection, collectionName, headers, req} = interceptorArgs.requestInfo;
      const obj = JSON.parse(req.text());
      let index: number = -1;

      // tslint:disable-next-line:triple-equals
      if (obj.id == undefined || !id) {
        resOptions = createErrorResponse(req, STATUS.BAD_REQUEST,
          'id is required for put');
      } else if (obj.id !== id) {
        resOptions = createErrorResponse(req, STATUS.BAD_REQUEST,
          `"${collectionName}" id does not match item.id`);
      } else {
        index = collection.findIndex((item: any) => item.id === id);
      }

      if (index > -1) {
        if (collectionName === 'items') {
          obj.date_updated = +Moment();
        }
        collection[index] = obj;
        resOptions = new ResponseOptions({
          body: this.clone(obj),
          headers: headers,
          status: STATUS.OK
        });
      } else {
        resOptions = createErrorResponse(req, STATUS.NOT_FOUND,
          `'${collectionName}' with id='${id}' not found`);
      }

      emitResponse(responseObserver, req, resOptions);
      return () => { }; // unsubscribe function
    });
  }

  // HTTP POST interceptor
  protected post(interceptorArgs: HttpMethodInterceptorArgs) {
    // Returns a "cold" observable that won't be executed until something subscribes.
    return new Observable<Response>((responseObserver: Observer<Response>) => {
      console.log('HTTP POST override');
      let resOptions: ResponseOptions;

      const {id, query, collection, collectionName, headers, req} = interceptorArgs.requestInfo;
      const obj = JSON.parse(req.text());

      // tslint:disable-next-line:triple-equals
      if (obj.id != undefined || id) {
        resOptions = createErrorResponse(req, STATUS.BAD_REQUEST,
          'id should be empty for post');
      } else {
        if (collectionName === 'items') {
          obj.id = 1 + collection.reduce( (a, b) => {
            return (a.id > b.id) ? a : b;
          }).id;
          obj.date_updated = +Moment();
          obj.color = '#FFCA28';
          obj.tag_primary = obj.tag_primary || 'Java';
        }
        collection.push(obj);
        resOptions = new ResponseOptions({
          body: this.clone(obj),
          headers: headers,
          status: STATUS.OK
        });
      }

      emitResponse(responseObserver, req, resOptions);
      return () => { }; // unsubscribe function
    });
  }

  /////////// private ///////////////
  private applyQuery(collection: any[], query: URLSearchParams) {
    // extract filtering conditions - {propertyName, RegExps) - from query/search parameters
    const conditions: {name: string, rx: RegExp}[] = [];
    const caseSensitive = 'i';
    query.paramsMap.forEach((value: string[], name: string) => {
      value.forEach(v => conditions.push({name, rx: new RegExp(decodeURI(v), caseSensitive)}));
    });

    const len = conditions.length;
    if (!len) { return collection; }

    // AND the RegExp conditions
    return collection.filter(row => {
      let ok = true;
      let i = len;
      while (ok && i) {
        i -= 1;
        const cond = conditions[i];
        ok = cond.rx.test(row[cond.name]);
      }
      return ok;
    });
  }

  private clone(data: any) {
    return JSON.parse(JSON.stringify(data));
  }

  private findById(collection: any[], id: number | string) {
    if (id === 'tags') {
      const distinct: string[] = [];
      collection.forEach(
        (item: Item) => {
          if (distinct.indexOf(item.tag_primary) === -1) {
            distinct.push(item.tag_primary);
          }
        });
      return distinct.map(
        str => {return {tag_primary: str}; }
      );
    } else {
      return collection.find((item: any) => item.id === id);
    }
  }

  private getLocation(href: string) {
    const l = document.createElement('a');
    l.href = href;
    return l;
  };
}

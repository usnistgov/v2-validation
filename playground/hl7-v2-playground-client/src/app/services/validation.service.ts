import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ResourceType } from './data.service';

export interface IValidationQuery {
  profile: string;
  constraints: string;
  vsLib: string;
  vsSpec: string;
  coConstraints: string;
  slicing: string;
  configuration: string;
  id: string;
  message: string;
}

export interface IEntry {
  classification: string;
  category: string;
  description: string;
  line: number;
  column: number;
  path: string;
}

export interface ICheckResourceResult {
  status: boolean;
  issues: IEntry[];
}

export interface IParseQuery {
  profile: string;
  message: string;
  id: string;
}

export interface IValidationResult {
  profile: ICheckResourceResult;
  constraints: ICheckResourceResult;
  vsLib: ICheckResourceResult;
  vsSpec: ICheckResourceResult;
  coConstraints: ICheckResourceResult;
  slicing: ICheckResourceResult;
  configuration: ICheckResourceResult;
  message: IEntry[];
}


@Injectable({
  providedIn: 'root'
})
export class ValidationService {

  constructor(private http: HttpClient) { }

  loadVxuExample(): Observable<IValidationQuery> {
    return this.http.get<IValidationQuery>('api/loadVxuExample');
  }

  validate(query: IValidationQuery): Observable<IValidationResult> {
    return this.http.post<IValidationResult>('api/validate', query);
  }

  parse(query: IParseQuery): Observable<string> {
    return this.http.post<string>('api/parse', query);
  }

  checkResource(content: string, rType: ResourceType): Observable<ICheckResourceResult> {
    return this.http.post<ICheckResourceResult>('api/checkResource', { content, rType });
  }
}

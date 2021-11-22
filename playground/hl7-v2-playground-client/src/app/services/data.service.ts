import { Injectable } from '@angular/core';
import { IEntry } from './validation.service';
import * as beautify from 'vkbeautify';

export enum Status {
  EMPTY, VALUED, VALID, INVALID
}

export enum ResourceType {
  PROFILE = 'Profile',
  VALUESET = 'VsLib',
  VALUESETSPEC = 'ValueSetSpec',
  CONFIGURATION = 'Configuration',
  CONSTRAINTS = 'Constraints',
  COCONSTRAINTS = 'CoConstraints',
  SLICING = 'Slicing',
  MESSAGE = 'Message',
}

export interface IDetectionByClass {
  class: string;
  size: number;
  categories: IDetectionByCategory[];
}

export interface IDetectionByCategory {
  category: string;
  size: number;
  entries: IEntry[];
}

export interface IContentValue {
  text: string;
  status: Status;
  issues: IDetectionByClass[];
  issuesVisible?: boolean;
}

export interface IPlayGroundContent {
  [resource: string]: IContentValue;
}

@Injectable({
  providedIn: 'root'
})
export class DataService {

  state: IPlayGroundContent = {
    [ResourceType.PROFILE]: {
      text: '',
      status: Status.EMPTY,
      issues: [],
    },
    [ResourceType.VALUESET]: {
      text: '',
      status: Status.EMPTY,
      issues: [],
    },
    [ResourceType.CONSTRAINTS]: {
      text: '',
      status: Status.EMPTY,
      issues: [],
    },
    [ResourceType.COCONSTRAINTS]: {
      text: '',
      status: Status.EMPTY,
      issues: [],
    },
    [ResourceType.SLICING]: {
      text: '',
      status: Status.EMPTY,
      issues: [],
    },
    [ResourceType.CONFIGURATION]: {
      text: '',
      status: Status.EMPTY,
      issues: [],
    },
    [ResourceType.VALUESETSPEC]: {
      text: '',
      status: Status.EMPTY,
      issues: [],
    },
    [ResourceType.MESSAGE]: {
      text: '',
      status: Status.EMPTY,
      issues: [],
    },
  };

  constructor() { }

  getState(): IPlayGroundContent {
    return this.state;
  }

  beautify(container: { text: string }): void {
    container.text = this.beautifyXML(container.text);
  }

  beautifyXML(text: string): string {
    return beautify.xml(text, 2);
  }

  format(type: ResourceType): void {
    switch (type) {
      case ResourceType.CONSTRAINTS:
      case ResourceType.PROFILE:
      case ResourceType.VALUESET:
      case ResourceType.VALUESETSPEC:
      case ResourceType.SLICING:
      case ResourceType.COCONSTRAINTS:
        this.state[type].text = this.beautifyXML(this.state[type].text);
        break;
    }
  }

  putXML(type: ResourceType, value: string): void {
    this.state[type].text = this.beautifyXML(value);
  }

  putValue(type: ResourceType, value: string): void {
    switch (type) {
      case ResourceType.CONSTRAINTS:
      case ResourceType.PROFILE:
      case ResourceType.VALUESET:
      case ResourceType.VALUESETSPEC:
      case ResourceType.COCONSTRAINTS:
      case ResourceType.SLICING:
        this.putXML(type, value);
        break;
      default:
        this.state[type].text = value;
    }
    this.setStatus(type, Status.VALUED);
  }

  setStatus(type: ResourceType, status: Status): void {
    this.state[type].status = status;
    if (status === Status.INVALID) {
      this.state[type].issuesVisible = true;
    }
  }

  get(type: ResourceType): IContentValue {
    return this.state[type];
  }

  putIssues(type: ResourceType, issues: IEntry[]): void {
    const status = issues.length > 0 ? Status.INVALID : Status.VALID;
    this.setStatus(type, status);
    this.state[type].issues = this.formatIssues(issues);
  }

  formatIssues(issues: IEntry[]): IDetectionByClass[] {
    const detections: IDetectionByClass[] = [];

    const classifications = issues.reduce((acc: Record<string, IEntry[]>, current: IEntry) => {
      (acc[current.classification] = acc[current.classification] || []).push(current);
      return acc;
    }, {});

    Object.keys(classifications).forEach((classification) => {
      const categories = classifications[classification].reduce((acc: Record<string, IEntry[]>, current: IEntry) => {
        (acc[current.category] = acc[current.category] || []).push(current);
        return acc;
      }, {});
      detections.push({
        class: classification,
        size: classifications[classification].length,
        categories: Object.keys(categories).map((category) => {
          return {
            category,
            size: categories[category].length,
            entries: categories[category],
          };
        })
      });
    });

    return detections;
  }

}

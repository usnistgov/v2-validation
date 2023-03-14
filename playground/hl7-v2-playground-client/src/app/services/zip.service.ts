import { Injectable } from '@angular/core';
import * as JSZip from 'jszip';
import { combineLatest, Observable, ReplaySubject, of, from } from 'rxjs';
import { tap } from 'rxjs/operators';

export interface IBundle {
  slicings?: string;
  coConstraints?: string;
  vsBindings: string;
  vsLib: string;
  constraints: string;
  profile: string;
}

@Injectable({
  providedIn: 'root'
})
export class ZipService {
  readonly PROFILE = 'Profile.xml';
  readonly CONSTRAINTS = 'Constraints.xml';
  readonly SLICING = 'Slicing.xml';
  readonly COCONSTRAINTS = 'CoConstraints.xml';
  readonly VSLIB = 'ValueSets.xml';
  readonly BINDINGS = 'Bindings.xml';

  constructor() {
  }

  getZipEntryContent(zip: JSZip, name: string): Observable<string> {
    const file = zip.file(name);
    return file ? from(file.async('string')) : of('');
  }

  getZipContent(file: File): Observable<IBundle> {
    const subject = new ReplaySubject<IBundle>(1);
    const reader = new FileReader();
    const jzip = new JSZip();

    reader.onload = (ev) => {
      jzip.loadAsync(reader.result as any).then((zip) => {
        combineLatest([
          this.getZipEntryContent(zip, this.PROFILE),
          this.getZipEntryContent(zip, this.CONSTRAINTS),
          this.getZipEntryContent(zip, this.SLICING),
          this.getZipEntryContent(zip, this.COCONSTRAINTS),
          this.getZipEntryContent(zip, this.VSLIB),
          this.getZipEntryContent(zip, this.BINDINGS),
        ]).pipe(
          tap(([profile, constraints, slicings, coConstraints, vsLib, vsBindings]) => {
            subject.next({
              profile,
              constraints,
              slicings,
              coConstraints,
              vsLib,
              vsBindings
            });
            subject.complete();
          })
        ).subscribe();
      });
    }
    reader.readAsArrayBuffer(file);
    return subject.asObservable();
  }

}

import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';

declare const zip: any;

export interface ZipEntry {
  version: number;
  bitFlag: number;
  compressionMethod: number;
  lastModDateRaw: number;
  lastModDate: string;
  crc32: number;
  compressedSize: number;
  uncompressedSize: number;
  filenameLength: number;
  extraFieldLength: number;
  commentLength: number;
  directory: boolean;
  offset: 0;
  filename: string;
  comment: string;
}

export interface ZipTaskProgress {
  active: boolean;
  current?: number;
  total?: number;
}

export interface ZipTask {
  progress: Observable<ZipTaskProgress | undefined>;
  data: Observable<Blob>;
}

@Injectable({
  providedIn: 'root'
})
export class ZipService {

  constructor() {
    zip.workerScriptsPath = 'scripts/';
  }

  getEntries(file: File): Observable<Array<ZipEntry>> {
    return new Observable(subscriber => {
      const reader = new zip.BlobReader(file);
      zip.createReader(reader, (zipReader: any) => {
        zipReader.getEntries((entries: Array<ZipEntry>) => {
          subscriber.next(entries);
          subscriber.complete();
        });
      }, (message: any) => {
        subscriber.error({ message });
      });
    });
  }

  getData(entry: ZipEntry): ZipTask {
    const progress = new Subject<ZipTaskProgress | undefined>();
    const data = new Observable<Blob>(subscriber => {
      const writer = new zip.BlobWriter();

      // Using `as any` because we don't want to expose this
      // method in the interface
      (entry as any).getData(writer, (blob: Blob) => {
        subscriber.next(blob);
        subscriber.complete();
        progress.next(undefined);
      }, (current: number, total: number) => {
        progress.next({ active: true, current, total });
      });
    });
    return { progress, data };
  }

}

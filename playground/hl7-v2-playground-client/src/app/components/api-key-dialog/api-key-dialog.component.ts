import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { SelectItem } from 'primeng/api';

@Component({
  selector: 'app-api-key-dialog',
  templateUrl: './api-key-dialog.component.html',
  styleUrls: ['./api-key-dialog.component.scss']
})
export class ApiKeyDialogComponent implements OnInit {

  keys: Record<string, string> = {};
  valuesets: Record<string, string> = {};
  vsNoKey: SelectItem<{
    bindingIdentifier: string,
    url: string,
  }>[] = [];
  vsList: SelectItem<{
    bindingIdentifier: string,
    url: string,
  }>[] = [];
  keysGroups: {
    key: string,
    options: SelectItem<{
      bindingIdentifier: string,
      url: string,
    }>[];
    vsList: {
      bindingIdentifier: string,
      url: string,
    }[]
  }[] = [];

  constructor(
    public dialogRef: MatDialogRef<ApiKeyDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any) {
    this.keys = data.keys;
    this.valuesets = data.valuesets;
    this.updateVsNoKey();
    this.updateKeyGroups();
    this.vsList = this.vsNoKey.map(v => ({ ...v }));
    console.log(this.keysGroups);
    console.log(this.vsNoKey);
  }

  onSelected(i: number, change: {
    bindingIdentifier: string,
    url: string,
  }[]) {
    this.updateVsNoKey();
    console.log(change);
    const selected = change.map((c) => c.bindingIdentifier);
    this.keysGroups.forEach((kg, j) => {
      if (i !== j) {
        kg.options.forEach((op) => {
          if (op.label && selected.includes(op.label)) {
            op.disabled = true;
          } else {
            op.disabled = false;
          }
        })
      }
    });
    this.keysGroups = [
      ...this.keysGroups,
    ];
    console.log(this.keysGroups);
  }

  done() {
    const keys: Record<string, string> = {};
    this.keysGroups.forEach((kg) => {
      kg.vsList.forEach((vs) => {
        keys[vs.bindingIdentifier] = kg.key;
      })
    });
    this.dialogRef.close(keys);
  }

  add() {
    this.keysGroups.push({
      key: '',
      options: this.vsList.map(v => ({ ...v })),
      vsList: [],
    })
  }

  updateVsNoKey() {
    const vsNoKey: {
      label: string,
      value: {
        bindingIdentifier: string,
        url: string,
      }
    }[] = [];
    const selected = Array.from(this.keysGroups.reduce((acc, group) => {
      group.vsList.forEach((vs) => acc.add(vs.bindingIdentifier));
      return acc;
    }, new Set()))
    Object.keys(this.valuesets).forEach((vsBi) => {
      if (!this.keys[vsBi] && !selected.includes(vsBi)) {
        vsNoKey.push({
          label: vsBi,
          value: {
            bindingIdentifier: vsBi,
            url: this.valuesets[vsBi]
          }
        })
      }
    });
    this.vsNoKey = vsNoKey;
  }

  updateKeyGroups() {
    const keysGroups = Object.keys(this.keys).reduce((acc, bi) => {
      if (!acc[this.keys[bi]]) {
        acc[this.keys[bi]] = []
      }
      acc[this.keys[bi]] = [
        ...acc[this.keys[bi]],
        {
          bindingIdentifier: bi,
          url: this.valuesets[bi]
        }
      ];
      return acc;
    }, {} as Record<string, {
      bindingIdentifier: string,
      url: string,
    }[]>);
    this.keysGroups = Object.keys(keysGroups).map((key) => ({
      key,
      options: this.vsList.map(v => ({ ...v })),
      vsList: keysGroups[key]
    }));
  }

  ngOnInit(): void { }

}

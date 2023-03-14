import { Component, Inject, OnInit } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { json } from 'vkbeautify';

@Component({
  selector: 'app-parse-dialog',
  templateUrl: './parse-dialog.component.html',
  styleUrls: ['./parse-dialog.component.scss']
})
export class ParseDialogComponent implements OnInit {

  message: string;
  jsonEditorOptions = {
    theme: 'monokai',
    mode: 'application/ld+json',
    lineNumbers: true,
    foldGutter: true,
    styleActiveLine: true,
    autoCloseTags: true,
    gutters: ['CodeMirror-linenumbers', 'CodeMirror-foldgutter', 'CodeMirror-lint-markers'],
    matchBrackets: true,
    extraKeys: { 'Alt-F': 'findPersistent' }
  };

  constructor(
    public dialogRef: MatDialogRef<ParseDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { message: string }) {
    this.message = JSON.stringify(JSON.parse(data.message), null, 2);
  }

  ngOnInit(): void {
  }

}

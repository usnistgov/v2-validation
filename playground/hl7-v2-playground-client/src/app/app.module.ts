import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { MatDialogModule } from '@angular/material/dialog';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { FormsModule } from '@angular/forms';
import { CodemirrorModule } from '@ctrl/ngx-codemirror';
import { ValidationResultComponent } from './components/validation-result/validation-result.component';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { BlockUIModule } from 'ng-block-ui';
import { ParseDialogComponent } from './components/parse-dialog/parse-dialog.component';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { PlaygroundComponent } from './pages/playground/playground.component';
import { HomeComponent } from './pages/home/home.component';
import { MarkdownModule } from 'ngx-markdown';
import { DocumentationComponent } from './pages/documentation/documentation.component';

@NgModule({
  declarations: [
    AppComponent,
    ValidationResultComponent,
    ParseDialogComponent,
    PlaygroundComponent,
    HomeComponent,
    DocumentationComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule,
    CodemirrorModule,
    BlockUIModule.forRoot(),
    NoopAnimationsModule,
    MatDialogModule,
    MarkdownModule.forRoot({ loader: HttpClient }),
  ],
  providers: [],
  bootstrap: [AppComponent],
  exports: [ParseDialogComponent],
  entryComponents: [ParseDialogComponent]
})
export class AppModule { }

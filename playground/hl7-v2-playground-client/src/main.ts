import { enableProdMode } from '@angular/core';
import { platformBrowserDynamic } from '@angular/platform-browser-dynamic';

import { AppModule } from './app/app.module';
import { environment } from './environments/environment';


import * as CodeMirror from 'codemirror';
import 'codemirror/mode/javascript/javascript';
import 'codemirror/mode/css/css';
import 'codemirror/mode/htmlmixed/htmlmixed';

import 'codemirror/addon/edit/matchbrackets';
import 'codemirror/addon/edit/closetag';
import 'codemirror/addon/selection/active-line';


import 'codemirror/mode/xml/xml';

import 'codemirror/addon/fold/foldgutter';
import 'codemirror/addon/fold/brace-fold';
import 'codemirror/addon/edit/closebrackets';
import 'codemirror/addon/edit/matchbrackets';
import 'codemirror/addon/fold/xml-fold';
import 'codemirror/addon/search/searchcursor';
import 'codemirror/addon/search/search';

CodeMirror.defineMode('config', () => {
  return {
    startState: () => {
      return {
        insideValueExtrapolation: false,
        insideString: false,
      };
    },
    token: (stream, state) => {
      const ch = stream.next();
      if (state.insideString && ch !== '"') {
        return 'string';
      }

      if (state.insideValueExtrapolation) {
        if (ch === '{') {
          return 'config-extrapolation-open-b';
        }
        if (ch === '}') {
          state.insideValueExtrapolation = false;
          return 'config-extrapolation-closed-b';
        }
        if (ch === '.') {
          return 'config-extrapolation-dot';
        }
        return 'config-extrapolation-close';
      }

      if (ch === '"') {
        if (state.insideString) {
          state.insideString = false;
        } else {
          state.insideString = true;
          stream.eatWhile((c) => {
            return c !== '"';
          });
        }
        return 'string';
      }
      if (ch === '{') {
        return 'config-open-b';
      }
      if (ch === '}') {
        return 'config-closed-b';
      }
      if (ch === '=') {
        return 'config-eq';
      }
      if (ch === '#') {
        stream.skipToEnd();
        return 'config-comment';
      }
      if (ch === '.') {
        return 'config-dot';
      }
      if (ch === '$') {
        state.insideValueExtrapolation = true;
        return 'config-extrapolation';
      }
      return 'config-word';
    }
  };
});

CodeMirror.defineMode('hl7v2', () => {
  const separators = {
    field_separator: '|',
    component_separator: '^',
    subcomponent_separator: '&',
    continuation_separator: '~,'
  };
  return {
    token: (stream) => {
      const ch = stream.next();
      if (stream.column() <= 2) {
        return 'segment-name';
      } else {
        if (ch === separators.field_separator) {
          return 'field-separator';
        }
        if (ch === separators.component_separator) {
          return 'component-separator';
        }
        if (ch === separators.subcomponent_separator) {
          return 'subcomponent-separator';
        }
        if (ch === separators.continuation_separator) {
          return 'continuation-separator';
        }
        return '';
      }
    }
  };
});

CodeMirror.defineMIME('text/hl7v2', 'hl7v2');
CodeMirror.defineMIME('text', 'config');

if (environment.production) {
  enableProdMode();
}

platformBrowserDynamic().bootstrapModule(AppModule)
  .catch(err => console.error(err));

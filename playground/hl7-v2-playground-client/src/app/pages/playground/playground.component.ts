import { ZipService } from './../../services/zip.service';
import { Component } from '@angular/core';
import { ValidationService } from '../../services/validation.service';
import { mergeMap, tap, take } from 'rxjs/operators';
import { IPlayGroundContent, DataService, ResourceType, Status } from '../../services/data.service';
import { Observable, of } from 'rxjs';
import { BlockUI, NgBlockUI } from 'ng-block-ui';
import { MatDialog } from '@angular/material/dialog';
import { ParseDialogComponent } from '../../components/parse-dialog/parse-dialog.component';

export interface IMessageID {
  structId: string;
  id: string;
  name: string;
}
@Component({
  selector: 'app-playground',
  templateUrl: './playground.component.html',
  styleUrls: ['./playground.component.scss']
})
export class PlaygroundComponent {

  @BlockUI() blockUI!: NgBlockUI;
  activeTab: ResourceType = ResourceType.PROFILE;

  xmlEditorOptions = {
    theme: 'monokai',
    mode: 'xml',
    lineNumbers: true,
    foldGutter: true,
    styleActiveLine: true,
    autoCloseTags: true,
    gutters: ['CodeMirror-linenumbers', 'CodeMirror-foldgutter', 'CodeMirror-lint-markers'],
    matchBrackets: true,
    extraKeys: { 'Alt-F': 'findPersistent' }
  };

  hl7EditorOptions = {
    theme: 'monokai',
    mode: 'hl7v2',
    lineNumbers: true,
    styleActiveLine: true,
    extraKeys: { 'Alt-F': 'findPersistent' }
  };

  confEditorOptions = {
    theme: 'monokai',
    mode: 'config',
    lineNumbers: true,
    styleActiveLine: true,
    extraKeys: { 'Cmd-F': 'findPersistent', 'Ctrl-F': 'findPersistent', 'Alt-F': 'findPersistent' }
  };

  resourceType = ResourceType;
  status = Status;
  tabList = [
    {
      value: ResourceType.PROFILE,
      text: 'Profile XML',
    },
    {
      value: ResourceType.CONSTRAINTS,
      text: 'Constraints XML',
    },
    {
      value: ResourceType.VALUESET,
      text: 'ValueSet Library XML',
    },
    {
      value: ResourceType.VALUESETSPEC,
      text: 'ValueSet Bindings XML',
    },
    {
      value: ResourceType.COCONSTRAINTS,
      text: 'Co-Constraints',
    },
    {
      value: ResourceType.SLICING,
      text: 'Slicing',
    },
    {
      value: ResourceType.CONFIGURATION,
      text: 'Configuration',
    },
    {
      value: ResourceType.MESSAGE,
      text: 'HL7 Message Validation',
    }
  ];

  content!: IPlayGroundContent;
  messages: IMessageID[] = [];

  constructor(
    private validationService: ValidationService,
    private dialog: MatDialog,
    private zipService: ZipService,
    private dataService: DataService) {
    this.content = this.dataService.getState();
  }

  select(tab: ResourceType): void {
    this.activeTab = tab;
  }

  loadResourceFromFile(a: Event, type: ResourceType): void {
    const fileInput = (a.target as HTMLInputElement);
    if (fileInput && fileInput.files && fileInput.files.length === 1) {
      const fr = new FileReader();
      fr.onload = () => {
        this.dataService.putValue(type, fr.result as string);
      };
      fr.readAsText(fileInput.files[0]);
    }
  }

  loadZipBundle(a: Event) {
    const fileInput = (a.target as HTMLInputElement);
    if (fileInput && fileInput.files && fileInput.files.length === 1) {
      this.zipService.getZipContent(fileInput.files[0]).pipe(
        take(1),
        tap((bundle) => {
          this.dataService.putValue(ResourceType.CONSTRAINTS, bundle.constraints);
          this.dataService.putValue(ResourceType.VALUESET, bundle.vsLib);
          this.dataService.putValue(ResourceType.VALUESETSPEC, bundle.vsBindings);
          this.dataService.putValue(ResourceType.SLICING, bundle.slicings || '');
          this.dataService.putValue(ResourceType.PROFILE, bundle.profile);
          this.dataService.putValue(ResourceType.COCONSTRAINTS, bundle.coConstraints || '');
        })
      ).subscribe();
    }
  }

  loadExampleVXU(): void {
    this.blockUI.start('Loading...');
    this.validationService.loadVxuExample().pipe(
      tap((value) => {
        this.dataService.putValue(ResourceType.PROFILE, value.profile);
        this.dataService.putValue(ResourceType.CONSTRAINTS, value.constraints);
        this.dataService.putValue(ResourceType.CONFIGURATION, value.configuration);
        this.dataService.putValue(ResourceType.VALUESET, value.vsLib);
        this.dataService.putValue(ResourceType.VALUESETSPEC, value.vsSpec);
        this.dataService.putValue(ResourceType.SLICING, value.slicing);
        this.dataService.putValue(ResourceType.COCONSTRAINTS, value.coConstraints);
        this.dataService.putValue(ResourceType.MESSAGE, value.message);
      })
    ).subscribe(
      () => this.blockUI.stop(),
      () => this.blockUI.stop(),
    );
  }

  format(type: ResourceType): void {
    this.dataService.format(type);
  }

  getMessageId(): Observable<IMessageID> {
    const ids = this.getMessageIds(this.dataService.get(ResourceType.PROFILE).text);
    if (ids && ids.length === 1) {
      return of(ids[0]);
    } else {
      return of();
    }
  }

  parseMessage(): void {
    this.getMessageId().pipe(
      mergeMap((messageId) => {
        this.blockUI.start('Parsing...');
        return this.validationService.parse({
          profile: this.dataService.get(ResourceType.PROFILE).text,
          id: messageId.id,
          message: this.dataService.get(ResourceType.MESSAGE).text,
        });
      })
    ).subscribe((value) => {
      this.dialog.open(ParseDialogComponent, {
        height: '90vh',
        minHeight: '90vh',
        width: '90vw',
        minWidth: '90vw',
        data: {
          message: value,
        }
      });
      this.blockUI.stop();
    },
      () => this.blockUI.stop());
  }

  validate(): void {
    this.getMessageId().pipe(
      mergeMap((messageId) => {
        this.blockUI.start('Validating...');
        return this.validationService.validate({
          profile: this.dataService.get(ResourceType.PROFILE).text,
          vsLib: this.dataService.get(ResourceType.VALUESET).text,
          constraints: this.dataService.get(ResourceType.CONSTRAINTS).text,
          vsSpec: this.dataService.get(ResourceType.VALUESETSPEC).text,
          coConstraints: this.dataService.get(ResourceType.COCONSTRAINTS).text,
          slicing: this.dataService.get(ResourceType.SLICING).text,
          configuration: this.dataService.get(ResourceType.CONFIGURATION).text,
          id: messageId.id,
          message: this.dataService.get(ResourceType.MESSAGE).text,
        });
      })
    ).subscribe((value) => {
      this.dataService.putIssues(ResourceType.PROFILE, value.profile.issues);
      this.dataService.putIssues(ResourceType.VALUESET, value.vsLib.issues);
      this.dataService.putIssues(ResourceType.CONSTRAINTS, value.constraints.issues);
      this.dataService.putIssues(ResourceType.VALUESETSPEC, value.vsSpec.issues);
      this.dataService.putIssues(ResourceType.COCONSTRAINTS, value.coConstraints.issues);
      this.dataService.putIssues(ResourceType.SLICING, value.slicing.issues);
      this.dataService.putIssues(ResourceType.CONFIGURATION, value.configuration.issues);
      this.dataService.putIssues(ResourceType.MESSAGE, value.message);
      this.blockUI.stop();
    },
      () => this.blockUI.stop());
  }

  hideIssues(tab: ResourceType): void {
    this.dataService.get(tab).issuesVisible = false;
  }

  edit(value: string, tab: ResourceType): void {
    if (value && value !== '') {
      this.dataService.setStatus(tab, Status.VALUED);
    } else {
      this.dataService.setStatus(tab, Status.EMPTY);
    }

    if (tab !== ResourceType.MESSAGE) {
      this.edit(this.dataService.get(ResourceType.MESSAGE).text, ResourceType.MESSAGE);
    }
  }

  checkResource(tab: ResourceType): void {
    this.blockUI.start('Validating...');
    this.validationService.checkResource(this.content[tab].text, tab).subscribe((value) => {
      this.dataService.putIssues(tab, value.issues);
      this.blockUI.stop();
    },
      () => this.blockUI.stop());
  }

  getMessageIds(value: string): IMessageID[] {
    const ids: IMessageID[] = [];
    if (window.DOMParser) {
      const xmlDoc = new DOMParser().parseFromString(value, 'text/xml');
      const container: HTMLCollectionOf<Element> = xmlDoc.getElementsByTagName('Messages');
      if (container.length > 0) {
        const collection = container[0].getElementsByTagName('Message');
        for (let i = 0; i < collection.length; i++) {
          if (collection.item(i)?.getAttribute('ID')) {
            const id = collection.item(i)?.getAttribute('ID') as string;
            const structId = collection.item(i)?.getAttribute('StructID') as string;
            const name = collection.item(i)?.getAttribute('Name') as string;

            ids.push({
              id,
              structId,
              name: name || structId || id,
            });
          }
        }
      }
    }
    return ids;
  }

}

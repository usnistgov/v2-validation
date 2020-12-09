import { Component, Input, OnInit } from '@angular/core';
import { IDetectionByClass, IDetectionByCategory } from '../../services/data.service';



@Component({
  selector: 'app-validation-result',
  templateUrl: './validation-result.component.html',
  styleUrls: ['./validation-result.component.scss']
})
export class ValidationResultComponent implements OnInit {

  @Input()
  set detections(d: IDetectionByClass[]) {
    console.log(d);
    if (d && d.length > 0) {
      this.selectClass(d[0]);
    } else {
      this.activeClass = undefined;
      this.activeCategory = undefined;
    }
    this.list = d;
  }

  list!: IDetectionByClass[];
  activeClass!: IDetectionByClass | undefined;
  activeCategory!: IDetectionByCategory | undefined;

  constructor() { }

  getStyle(c: IDetectionByClass): string {
    return 'b-' + c.class.toLowerCase();
  }

  selectClass(c: IDetectionByClass): void {
    this.activeClass = c;
    this.activeCategory = c.categories[0];
  }

  selectCategory(categ: IDetectionByCategory): void {
    this.activeCategory = categ;
  }

  ngOnInit(): void {
  }

}

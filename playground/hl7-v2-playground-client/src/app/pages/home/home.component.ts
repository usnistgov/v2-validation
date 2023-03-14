import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {


  links = [{
    icon: 'fa-book',
    color: 'blue',
    label: 'Documentation',
    link: '/documentation',
  }, {
    icon: 'fa-play',
    color: 'red',
    label: 'Playground',
    link: '/playground',
  }, {
    icon: 'fa-cloud',
    color: 'green',
    label: 'Web Service',
    link: '/webservice',
  }, {
    icon: 'fa-download',
    color: 'black',
    label: 'Downloads',
    link: '/downloads',
  }];

  constructor() { }

  ngOnInit(): void {
  }

}

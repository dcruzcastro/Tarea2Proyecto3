import { Component, EventEmitter, Input, Output } from '@angular/core';
import { ICategory} from '../../../interfaces';

@Component({
  selector: 'app-category',
  standalone: true,
  imports: [],
  templateUrl: './category.component.html',
  styleUrl: './category.component.scss'
})
export class CategoryComponent {
  @Input() category: ICategory[] = [];
  @Input() areActionsAvailable: boolean = false;
  @Output() callEditMethod: EventEmitter<ICategory> = new EventEmitter<ICategory>();
  @Output() callDeleteMethod: EventEmitter<ICategory> = new EventEmitter<ICategory>();
  
}

import { Component, effect, inject } from '@angular/core';
import { PaginationComponent } from '../../components/pagination/pagination.component';
import { CategoryService } from '../../services/category.service';
import { LoaderComponent } from '../../components/loader/loader.component';
import { CategoryComponent } from '../../components/product/category/category.component';
import { CategoryFormComponent } from '../../components/product/category-form/category-form.component';
import { FormBuilder, Validators } from '@angular/forms';
import { ICategory } from '../../interfaces';
import { AuthService } from '../../services/auth.service';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-categories',
  standalone: true,
  imports: [
    PaginationComponent,
    LoaderComponent,
    CategoryComponent,
    CategoryFormComponent
  ],
  templateUrl: './categories.component.html',
  styleUrl: './categories.component.scss'
})
export class CategoriesComponent {
  public categoryService: CategoryService = inject(CategoryService);
  public fb: FormBuilder = inject(FormBuilder);
  public areActionsAvailable: boolean = false;
  public authService: AuthService = inject(AuthService);
  public route: ActivatedRoute = inject(ActivatedRoute);
  public form = this.fb.group({
    id: [0],
    name: ['', Validators.required],
    description: ['', Validators.required]
  })
  constructor() {
    this.categoryService.getAll();
    effect(() => {
      console.log('categories updated', this.categoryService.categories$());
      if (this.categoryService.categories$()[0]) {
        this.categoryService.categories$()[0] ?  this.categoryService.categories$()[0].name = `${this.categoryService.categories$()[0].name}` : null;
        this.categoryService.categories$()[0] ?  this.categoryService.categories$()[0].description = `${this.categoryService.categories$()[0].description}` : null;
      }
    });
  }

  ngOnInit() {
    this.categoryService.getAll();
    this.route.data.subscribe( data => {
      this.areActionsAvailable = this.authService.areActionsAvailable(data['authorities'] ? data['authorities'] : []);
      console.log('areActionsAvailable', this.areActionsAvailable);
    });
  }

  save(item: ICategory) {
    item.id ? this.categoryService.update(item) : this.categoryService.save(item);
    this.form.reset();
  }

  delete(item: ICategory) {
    console.log('delete', item);
    this.categoryService.delete(item);
  }
}

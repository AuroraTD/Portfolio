import { NgModule, Query } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { StartComponent } from './start/start.component';
import { AgeComponent } from './age/age.component';
import { MenuComponent } from './menu/menu.component';
import { QuestionnaireComponent } from './questionnaire/questionnaire.component';
import { StorybookComponent } from './storybook/storybook.component';
import { GenerateStoryService } from './generate-story.service';

const routes: Routes = [
  {
    path: '',
    redirectTo: '/start',
    pathMatch: 'full'
  },
  {
    path: 'start',
    component: StartComponent
  },
  {
    path: 'age',
    component: AgeComponent
  },
  {
    path: 'menu',
    component: MenuComponent
  },
  {
    path: 'questionnaire',
    component: QuestionnaireComponent
  },
  {
    path: 'storybook',
    component: StorybookComponent
  }
]

@NgModule({
  imports: [ RouterModule.forRoot(routes, { scrollPositionRestoration: 'enabled' }) ],
  exports: [ RouterModule ],
  providers: []
})
export class AppRoutingModule { }

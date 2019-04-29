import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { AppComponent } from './app.component';
import { StartComponent } from './start/start.component';
import { AgeComponent } from './age/age.component';
import { MenuComponent } from './menu/menu.component';
import { QuestionnaireComponent } from './questionnaire/questionnaire.component';
import { StorybookComponent } from './storybook/storybook.component';
import { AppRoutingModule } from './app-routing.module';
import { HttpClientModule } from '@angular/common/http';
import { GenerateStoryService } from './generate-story.service';

@NgModule({
  declarations: [
    AppComponent,
    StartComponent,
    AgeComponent,
    MenuComponent,
    QuestionnaireComponent,
    StorybookComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule
  ],
  providers: [GenerateStoryService],
  bootstrap: [AppComponent]
})
export class AppModule { }

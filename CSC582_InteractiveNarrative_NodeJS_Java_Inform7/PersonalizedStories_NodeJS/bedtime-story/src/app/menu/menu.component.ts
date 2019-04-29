import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { GenerateStoryService } from '../generate-story.service';
import { Util } from '../utils';

@Component({
  selector: 'app-menu',
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.css']
})
export class MenuComponent implements OnInit {

  constructor(
    private router: Router,
    private generateStoryService: GenerateStoryService
  ) { }

  ngOnInit() {
    // Reset body colors in case user navigates backwards via the browser
    Util.resetColors();
  }

  /**
   * Route to QuestionnaireComponent
   */
  onClickYes() {
    // Logging
    console.log("Yes clicked on menu component"); 
    this.router.navigate(['/questionnaire']); 
  }

  /**
   * Skip the questionnaire page and automatically generate story.
   * Makes call to /generateStory endpoint.
   */
  onClickNo() {
    console.log("No clicked on menu component");
    // Navigate to 'Storybook' component, pass in empty dictionary for input because no input
    // Make loading spinner visible to indicate the request going through
    document.getElementById('loadingSpinner').style.display = "block";
    
    this.generateStoryService.fetchGeneratedStoryResponse({}).subscribe(res => {
      console.log(res);
      // Save response to service
      this.generateStoryService.storeGeneratedStoryResponse(res);
      this.router.navigate(['/storybook']);
    })
  }

}

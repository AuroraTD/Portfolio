import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { Router } from '@angular/router';
import { SetupService, PromptOption, Option } from '../setup.service';
import { stringify } from 'querystring';
import { bindCallback } from 'rxjs';
import { bind } from '@angular/core/src/render3';
import { Util } from '../utils';
import { GenerateStoryService } from '../generate-story.service';

@Component({
  selector: 'app-questionnaire',
  templateUrl: './questionnaire.component.html',
  styleUrls: ['./questionnaire.component.css'],
  providers: [SetupService]
})

export class QuestionnaireComponent implements OnInit {

  // List of prompt options retrieved from the server
  promptOptions: PromptOption[];

  // Display error if promptOptions is empty after getPromptOptions()
  displayError: boolean;

  // Store user input
  input = {};

  constructor(
    private router: Router,
    private setupService: SetupService,
    private generateStoryService: GenerateStoryService
  ) { }

  ngOnInit() {
    this.setUp();
    // Reset body colors in case user navigates backwards via the browser
    Util.resetColors();
  }

  /**
   * Fetch prompt options from server and create FormGroup.
   */
  setUp() {
    this.setupService.fetchPromptOptions().subscribe( res => {
      console.log(res);
      this.promptOptions = res;
      // Initialize input dictionary with prompts
      this.promptOptions.forEach( prompt => {
        this.input[prompt.promptName] = "";
      })
    })
  }

  /**
   * Stores user input on each click of an option.
   * @param promptName
   * @param optionName
   */
  onClickOption(promptName: any, optionName: string) {
    // Declare variables
    var cardDivs
    var i;
    // Logging
    console.log(promptName + " " + optionName + " clicked!");
    // Remember choice
    this.input[promptName] = optionName;
    // Provide visual feedback on which option was selected
    // Use vanilla JS instead of jQuery to avoid yet another dependency in the project
    cardDivs = document.getElementById(promptName).querySelectorAll("div.card");
    for (i = 0; i < cardDivs.length; i++) {
      cardDivs[i].classList.remove("selected-option");
    }
    document.getElementById(optionName).classList.add("selected-option");
  }

  /**
   * Executes when user clicks 'Tell me a story!'
   * Sends user's input to server, which will return a response.
   */
  onClickTellMeAStory() {
    // Log to console for debugging purposes
    console.log("Tell me a story button clicked on questionnaire component");
    console.log(this.input);

    // Make loading spinner visible to indicate the request going through
    document.getElementById('loadingSpinner').style.display = "block";

    // Navigate to 'Storybook' component
    this.generateStoryService.fetchGeneratedStoryResponse(this.input).subscribe(res => {
      console.log(res);
      // Save response to service
      this.generateStoryService.storeGeneratedStoryResponse(res);
      this.router.navigate(['/storybook']);
    })
    // this.router.navigate(['/storybook']);
  }

  /**
   * TODO: Extract into separate ErrorComponent
   * Executes when user clicks 'Start over'
   * Only possible when an error occurs and no PromptOption objects are loaded into promptOptions
   */
  onClickStartOver() {
    console.log("Start over clicked on storybook component");
    this.router.navigate(['/start']);
  }

  /**
   * Produces a color that has good contrast to the given color
   * Color input is of the form "#RRGGBB"
   * Color output is "black" or "white"
   * @param colorIn
   * @returns String colorOut
   */
  complementaryColor(colorIn: String): String {
    return Util.complementaryColor(colorIn);
  }

}

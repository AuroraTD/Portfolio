import { Component, OnInit, Input, ViewChild, ElementRef } from '@angular/core';
import { Router } from '@angular/router';
import { Util } from '../utils';

@Component({
  selector: 'app-age',
  templateUrl: './age.component.html',
  styleUrls: ['./age.component.css']
})
export class AgeComponent implements OnInit {
  // Age ranges to display to user
  ages = ["3", "4", "5", "6", "7", "8", "9", "10+"]
  // Age selected by user
  age: string;
  // Next button reference
  @ViewChild('nextButton') nextButton: ElementRef;

  constructor(
    private router: Router
  ) { }

  ngOnInit() {
    // By default, the 'Next' button should be disabled until the user has selected an age
    this.nextButton.nativeElement.disabled = true;
    
    // Reset body colors in case user navigates backwards via the browser
    Util.resetColors();
  }

  /**
   * Handle user clicking an age button. The maximum age considered by the backend is 10.
   * Therefore, if user presses "10+", 10 is sent in the request.
   * Enable 'Next' button if an age is selected.
   */
  onClickAge(age: string) {
    console.log("Age " + age + " button clicked");
    if (age == "10+") {
      this.age = "10";
    } else {
      this.age = age;
    }
    this.nextButton.nativeElement.disabled = false;
  }

   /**
    * Handle user clicking "Next" button.
    */
  onClickNext() {
    console.log("Next button clicked on age component. Age inputted = " + this.age);

    // Save age persistently so that we can use it to ask for story setup prompts and for story generation
    localStorage.setItem('age', this.age);

    this.router.navigate(['/menu']);
  }

}

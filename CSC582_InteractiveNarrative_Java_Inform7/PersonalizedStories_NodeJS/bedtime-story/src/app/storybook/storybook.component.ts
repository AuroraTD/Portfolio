import { Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { Router } from '@angular/router';
import { GenerateStoryService, Option } from '../generate-story.service';
import { generate } from 'rxjs';
import { Util } from '../utils';

@Component({
  selector: 'app-storybook',
  templateUrl: './storybook.component.html',
  styleUrls: ['./storybook.component.css'],
  providers: []
})
export class StorybookComponent implements OnInit {

  // Variables used in this component
  story: string[];
  images: string[];
  setupOptions: {};

  // References to elements in HTML
  @ViewChild('backgroundContainer') backgroundContainer: ElementRef;

  constructor(
    private router: Router,
    public generateStoryService: GenerateStoryService
  ) {}

  ngOnInit() {
    console.log("starting init");
    // Remember the child's choices
    // this.story = this.generateStoryService.story;
    // this.images = this.generateStoryService.images;
    this.setupOptions = this.generateStoryService.setupOptions;
    // Format story and images to be displayed in paragraph form in HTML
    this.formatStory(this.generateStoryService.story, this.generateStoryService.images);
    // Declare variables
    var cssColor;
    // Alter the color option from display form to valid CSS value form
    cssColor = this.setupOptions["color"].replace(/ /g,"").toLowerCase();
    // Color the application with the child's preferred color
    document.querySelector("body").style.backgroundColor = cssColor;
    document.querySelector("body").style.color = this.complementaryColor(cssColor).valueOf();

    
  }

  /**
   * Only use some of the images returned from the response (smaller age = more pictures).
   * Divide the story into n paragraphs, where n = the number of images used in the story.
   */
  private formatStory(story: string, images: string[]) {
    // The number of images to show is proportional to the input age
    let age = this.setupOptions["age"];
    let n = Math.ceil(images.length/age);
    // Select images to display in story
    this.images = this.getRandomImages(n, images);
    console.log(this.images.length);

    // Split story into n paragraphs (n = length)
    this.story = this.splitStoryIntoParagraphs(n, story);
    console.log(this.story);
  }

  /**
   * Randomly generate list of images to display in the component.
   * REFERENCE: https://stackoverflow.com/questions/19269545/how-to-get-n-no-elements-randomly-from-an-array?lq=1
   * @param n - the number of images to retrieve
   * @return list of n images
   */
  private getRandomImages(n: number, images: string[]) {
    // Shuffle array
    const shuffledImages = images.sort(() => 0.5 - Math.random());
    // Return sub-array of first n elements after shuffled
    return shuffledImages.slice(0, n);
  }

  /**
   * Split the story into n paragraphs
   * @param story - story text to split
   * @return list of n paragraphs
   */
  private splitStoryIntoParagraphs(n: number, story: string) {
    let sentences = story.split(".");
    console.log(sentences);
    // Number of sentences per paragraph
    // NOTE: splitting the string by periods leaves the last item in 
    // the 'sentences' list to be an empty string that can be disregarded.
    let numSentences = Math.ceil((sentences.length-1)/n);
    console.log(numSentences);
    // Group sentences in n groups (i.e. paragraphs)
    let paragraphs = [];
    for (let i = 0; i<n; i+=1) {
      let start = i*numSentences;
      let end = Math.min(start + numSentences, sentences.length);
      paragraphs.push(sentences.slice(start, end).join(". ") + ".");
    }
    console.log(paragraphs);
    return paragraphs;
  }

  /**
   * Start over - skip start button go right to age entry
   */
  onClickStartOver() {
    console.log("Start Over clicked on storybook component");
    this.router.navigate(['/age']);
    this.resetColors();
  }

  /**
   * Quit - go all the way back to home page
   */
  onClickQuit() {
    console.log("Quit clicked on storybook component");
    this.router.navigate(['/start']);
    this.resetColors();
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

  /**
   * Reset DOM colors back to normal
   */
  resetColors() {
    document.querySelector("body").style.backgroundColor = "white";
    document.querySelector("body").style.color = "black";
  }

}

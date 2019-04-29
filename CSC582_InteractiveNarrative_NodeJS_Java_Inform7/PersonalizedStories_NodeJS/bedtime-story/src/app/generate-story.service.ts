import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient } from '@angular/common/http';
import { generate, BehaviorSubject, Observable, of, Subject } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Util } from './utils';

/**
 * Object representation of response from server when /generate?* endpoint is hit
 * FIELDS - setupOptions - list of Option objects that the user selected in the questionnaire
 *          images - list of image urls in string format
 *          story - string
 */
export class GenerateStoryResponse {
  setupOptions: {};
  images: string[];
  story: string;
}

 /**
  * Object representation of the option objects returned in the options array.
  * FIELDS - name - name of the option that displays to the user
  *          value - either hex value (if 'color' option) or url (if any other option tye)
  */
 export class Option {
  name: string;
  value: string;
}

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable()
export class GenerateStoryService {

  // Base url to the server
  baseUrl = "http://localhost:3001/"

  // GenerateStoryResponse from server
  story: string;
  images: string[];
  setupOptions: {};

  /**
   * Constructor
   * @param http
   */
  constructor(
    private http: HttpClient
  ) {}

  //"generate?age=13&boy=Joe&girl=Sue&animal=bunny&color=red&theme=castle&location=africa"
  fetchGeneratedStoryResponse(input: any) {
    // Create a URL to make a request to
    // This should include the parameters needed by the back-end server
    // We need to get the age of the child each time we wish to fetch a story response
    // because it may have changed since the last fetch
    var url = this.baseUrl + "generate?age=" + localStorage.getItem("age");
    ;
    for (let promptName in input) {
      if (input[promptName] != "") {
        url += "&" + promptName + "=" + input[promptName];
      }
    }
    // Logging
    console.log(url);

    // Make the request
    return this.http.get<GenerateStoryResponse>(url).pipe(
      catchError(Util.handleError<GenerateStoryResponse>('generateStory', null))
    )
  }

  /**
   * Save given response to service so components can access the different parts of the response.
   * @param response
   */
  storeGeneratedStoryResponse(response: GenerateStoryResponse) {
    this.story = response.story;
    this.images = response.images;
    // this.images = this.sortImages(response.images);
    this.setupOptions = response.setupOptions;
  }

  /**
   * Parse images list into a dictionary where the key = animal/theme/etc. 
   * and the value = list of images associated with the key
   */
  private sortImages(images: string[]) {
    let imageDict = {};
    // Image urls are in the format: ./src/assets/images/*/*_*.jpg
    // Ex: ./src/assets/images/animals/elephant_1.jpg
    // So key = 'elephant', value = ['./src/assets/images/animals/elephant_1.jpg']
    images.forEach(image => {
      // Find index of last '/', which will indicate the start of the key
      let startIndex = image.lastIndexOf("/") + 1;
      let endIndex = Math.max(image.lastIndexOf("_"), image.lastIndexOf("."));
      let key = image.slice(startIndex, endIndex);
      // If key already in dictionary, append to its value 
      if (key in imageDict) {
        imageDict[key].push(image);
      } 
      // If key not in dictionary, create entry with value as a list
      else {
        imageDict[key] = [image];
      }
    })

    return imageDict;
  }
  
}

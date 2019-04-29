import { Injectable } from '@angular/core';
import { HttpHeaders, HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Util } from './utils';

/**
 * Object representation of the prompt options returned in the setUpPrompts array.
 * FIELDS - promptName - the type of options the prompt asks for
 *          prompt - the string that displays to the user 
 *          options - array of Option objects (see below)
 */
export class PromptOption {
  promptName: string;
  prompt: string;
  options: Option[];
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


@Injectable({
  providedIn: 'root'
})
export class SetupService {

  // Base url to the server
  baseUrl = "http://localhost:3001/"
  // Sub-url to the setup functions in the server
  setUpUrl = "setup"

  /**
   * Constructor
   * @param http 
   */
  constructor(
    private http: HttpClient
  ) { }

  /**
  * Fetch prompt options for the Questionnaire component from server.
  * Stores locally in service.
  * @returns observable PromptOption[]
  */
  fetchPromptOptions(): Observable<PromptOption[]> {

    // Create a URL to make a request to
    // This should include the parameters needed by the back-end server
    // In the case of story setup, the only parameter is the child's age
    var url = this.baseUrl + this.setUpUrl + "?age=" + localStorage.getItem('age');

    // Make the request 
    return this.http.get<PromptOption[]>(url).pipe(
      catchError(Util.handleError<PromptOption[]>('setUp', []))
    );
  }

}

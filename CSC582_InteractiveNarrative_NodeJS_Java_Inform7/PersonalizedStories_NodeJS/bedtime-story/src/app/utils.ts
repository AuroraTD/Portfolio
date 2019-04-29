import { Observable, of } from 'rxjs';

/**
 * Contains globally available functions that are used by
 * multiple components throughout the application.
 */
export class Util{
    /**
   * REFERENCE: https://angular.io/tutorial/toh-pt6#get-heroes-with-httpclient
   * Handle Http operation that failed.
   * Let the app continue.
   * @param operation - name of the operation that failed
   * @param result - optional value to return as the observable result
   * @returns empty result
   */
  static handleError<T> (operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      console.error(error); // log to console instead

      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }

  /**
   * Produces a color that has good contrast to the given color
   * Color input is of the form "#RRGGBB" OR in the form of a color name (e.g. "blue")
   * Color output is either "black" or "white"
   * https://stackoverflow.com/questions/12043187/how-to-check-if-hex-color-is-too-black
   * @param colorIn
   * @returns String colorOut
   */
  static complementaryColor(colorIn: String): String {

    // Declare variables
    var colorHex;
    var rgb;
    var r;
    var g;
    var b;
    var luma;
    var colorOut;
    var dummyDiv;
    var computedRGB;

    // Convert input to R, G, B values
    if (colorIn.includes("#")) {

      // Strip off "#"
      colorHex = colorIn.substring(1);

      // Convert RRGGBB to decimal
      rgb = parseInt(colorHex, 16);
      r = (rgb >> 16) & 0xff;
      g = (rgb >>  8) & 0xff;
      b = (rgb >>  0) & 0xff;

    }
    else {
      dummyDiv = document.createElement("div");
      dummyDiv.style.color = colorIn;
      document.body.appendChild(dummyDiv)
      computedRGB = getComputedStyle(dummyDiv).color.toString().match(/\d+/g);
      r = Number(computedRGB[0]);
      g = Number(computedRGB[1]);
      b = Number(computedRGB[2]);
      document.body.removeChild(dummyDiv);
    }

    // Calculate luma per ITU-R BT.709
    luma = 0.2126 * r + 0.7152 * g + 0.0722 * b;

    // Pick complementary color
    colorOut = luma < 110 ? "white" : "black";

    // Return
    return colorOut;

  }

  /**
   * Reset DOM colors of a page to "default" mode (white background with black text)
   */
  static resetColors() {
    document.querySelector("body").style.backgroundColor = "white";
    document.querySelector("body").style.color = "black";
  }

}
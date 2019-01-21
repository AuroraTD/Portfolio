/**
    @file pie.c
    @author Aurora Tiffany-Davis (attiffan)

    Given relative sizes for pie chart slices, draw one slice in red, one in green and one in blue.
    Start from negative x axis and fill in clockwise.
    Output is in PPM file format.
    Check for invalid input.
 */

// Define expected number of inputs
#define EXPECTED_INPUTS 3
// Define an exit status and message in the case of failure to read expected input
#define FAIL_CODE_INVALID_INPUT 100
#define FAIL_MSG_INVALID_INPUT "Invalid input\n"
// Define an exit status and message in the case of unexpected color (for debugging)
#define FAIL_CODE_UNEXPECTED_COLOR 200
#define FAIL_MSG_UNEXPECTED_COLOR "Unexpected color\n"
// Define an exit status and message in the case of unexpected slice (for debugging)
#define FAIL_CODE_UNEXPECTED_SLICE 300
#define FAIL_MSG_UNEXPECTED_SLICE "Unexpected slice\n"
// Define size of image (same size on both sides of square)
#define IMAGE_SIZE 100
// Define pie chart border size
#define BORDER 3
// Define empty space outside pie chart (and pie chart border)
#define EMPTY_SPACE 2
// Define color intensity
#define COLOR_INTENSITY 255
// Define image file format
#define FILE_FORMAT "P3"
// Define color characters
#define CHAR_RED 'R'
#define CHAR_GREEN 'G'
#define CHAR_BLUE 'B'
#define CHAR_WHITE 'W'
#define CHAR_BLACK 'K'
// Define degrees in a circle
#define TOTAL_DEGREES 360
// Ask for math constant definitions
#define _USE_MATH_DEFINES

// Include standard libraries
#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <math.h>

// Function prototypes
double normalizeCoord( int coordinate );
bool insideCircle( int x, int y, int rad );
bool insideSlice( int x, int y, double minAngle, double maxAngle);
void printPixel( char color );

/**
    Read relative sizes for pie chart slices, check for invalid input.
    Print PPM file header.
    Determine and print colors pixel by pixel, according to location
    (on circle, inside circle, inside particular slice?).

    @return FAIL_CODE_INVALID_INPUT if invalid input is seen, EXIT_SUCCESS otherwise.
 */
int main()
{
    // Declare variables
    int sliceRelSizeCount, relSizeRed, relSizeGreen, relSizeBlue, column, row;
    int inCircle, relSizeSum;
    int radius = IMAGE_SIZE/2 - (BORDER + EMPTY_SPACE);
    double maxAngleRed, maxAngleGreen, maxAngleBlue;

    /** Read slice relative size input
        Must have correct number of slices
        Must not have any values be negative
        Must not have all values be zero
    */
    sliceRelSizeCount = scanf("%d %d %d", &relSizeRed, &relSizeGreen, &relSizeBlue);
    if (sliceRelSizeCount != EXPECTED_INPUTS
        || relSizeRed < 0 || relSizeGreen < 0 || relSizeBlue < 0
        || (relSizeRed == 0 && relSizeGreen == 0 && relSizeBlue == 0)){
        printf(FAIL_MSG_INVALID_INPUT);
        exit(FAIL_CODE_INVALID_INPUT);
    }
    // Process input
    else{
        /** Determine actual slice sizes
            This is tracked in terms of maximum angle.
            Degrees are used for ease of thinking about the slices.
            Zero degrees is considered to be along the negative X axis
            from the center of the circle.
            Example: if maxAngleRed is 10, and maxAngleGreen is 20,
            then 0 deg to 10 deg is a red slice, 10 deg to 20 deg is a green slice,
            and 20 deg to 360 deg is a blue slice
            (blue always fills out the remainder of the pie chart).
        */
        relSizeSum = relSizeRed + relSizeGreen + relSizeBlue;
        maxAngleRed = TOTAL_DEGREES * ((double)relSizeRed/relSizeSum);
        maxAngleGreen = TOTAL_DEGREES * ((double)relSizeGreen/relSizeSum) + maxAngleRed;
        maxAngleBlue = TOTAL_DEGREES;

        // Print image file header
        printf("%s\n", FILE_FORMAT);
        printf("%d %d\n", IMAGE_SIZE, IMAGE_SIZE);
        printf("%d\n", COLOR_INTENSITY);

        // Print image file contents
        for ( row = 0; row < IMAGE_SIZE; row++ ) {
            for ( column = 0; column < IMAGE_SIZE; column++ ) {
                inCircle = insideCircle(column, row, radius);
                if (inCircle){
                    // This pixel is inside the circle itself: Print red, blue, or green
                    if (insideSlice(column, row, 0, maxAngleRed)){
                        printPixel(CHAR_RED);
                    }
                    else if (insideSlice(column, row, maxAngleRed, maxAngleGreen)){
                        printPixel(CHAR_GREEN);
                    }
                    else if (insideSlice(column, row, maxAngleGreen, maxAngleBlue)){
                        printPixel(CHAR_BLUE);
                    }
                    else{
                        printf(FAIL_MSG_UNEXPECTED_SLICE);
                        exit(FAIL_CODE_UNEXPECTED_SLICE);
                    }
                }
                else if (!inCircle && insideCircle(column, row, radius+BORDER)){
                    // This pixel is on the border of the circle: Print black
                    printPixel(CHAR_BLACK);
                }
                else{
                    // This pixel is totally outside the circle: Print white
                    printPixel(CHAR_WHITE);
                }
            }
            // End of row, move to next line
            printf("\n");
        }
        // Exit
        exit(EXIT_SUCCESS);
    }
}

/**
    This function normalizes a single pixel coordinate
    from image top-left position to circle center point.

    @param coordinate Pixel x or y coordinate, relative to image top-left position.
    @return normalizedCoordinate Pixel x or y coordinate, relative to circle center point.
 */
double normalizeCoord( int coordinate )
{
    // Determine circle center position
    double circleCenter = ((double) IMAGE_SIZE -1) / 2;
    double normalizedCoordinate = coordinate - circleCenter;

    // Normalize coordinate
    return normalizedCoordinate;
}

/**
    This function returns true if the given pixel location is
    inside a circle of given radius and centered in the image.
    Used to help decide if a pixel is inside the pie chart.

    @param x Pixel x coordinate, relative to image top-left position.
    @param y Pixel y coordinate, relative to image top-left position.
    @param rad Circle radius in pixels.
    @return inside True if inside a circle of given radius and centered in the image,
    False otherwise.
 */
bool insideCircle( int x, int y, int rad )
{
    // Declare variables
    double normalizedX, normalizedY, distanceToCenter;
    bool inside;

    // Normalize coordinates from image top-left position to circle center point
    normalizedX = normalizeCoord(x);
    normalizedY = normalizeCoord(y);

    // Determine distance of pixel from circle center (calculate hypotenuse)
    distanceToCenter = sqrt(normalizedX*normalizedX + normalizedY*normalizedY);

    // Determine whether this pixel is inside the circle
    inside = distanceToCenter <= rad;
    return inside;
}

/**
    Given a pixel location, this function will determine
    whether or not it's inside a particular slice of the pie chart.

    @param x Pixel x coordinate, relative to image top-left position.
    @param y Pixel y coordinate, relative to image top-left position.
    @param minAngle Minimum angle (expressed in degrees) of the slice.
    @param maxAngle Maximum angle (expressed in degrees) of the slice.
    @return inSlice True if inside the color slice of interest, False otherwise.
 */
bool insideSlice( int x, int y, double minAngle, double maxAngle)
{
    // Declare variables
    double normalizedX, normalizedY;
    double pixelAngle;
    bool inSlice;

    // Normalize coordinates from image top-left position to circle center point
    normalizedX = normalizeCoord(x);
    normalizedY = normalizeCoord(y);

    /** Determine pixel angle in degrees
        Zero along negative x axis, incrementing clockwise.
        The angle is expressed in these terms for ease of picturing the pie chart.
        The first (red) slice starts along the negative x axis and fills in clockwise.
    */
    pixelAngle = (atan2(normalizedY, normalizedX) / M_PI * (TOTAL_DEGREES/2)) + (TOTAL_DEGREES/2);

    // Determine whether this pixel is inside the slice of interest
    inSlice = pixelAngle >= minAngle && pixelAngle <= maxAngle;
    return inSlice;
}

/**
    Print one pixel in the PPM image format.

    @param color Pixel color code (defined in preprocessor directives).
 */
void printPixel( char color )
{
    // Declare variables
    int redIntensity, greenIntensity, blueIntensity;
    // Determine RGB code
    switch (color){
    case CHAR_WHITE:
        redIntensity = COLOR_INTENSITY;
        greenIntensity = COLOR_INTENSITY;
        blueIntensity = COLOR_INTENSITY;
        break;
    case CHAR_BLACK:
        redIntensity = 0;
        greenIntensity = 0;
        blueIntensity = 0;
        break;
    case CHAR_RED:
        redIntensity = COLOR_INTENSITY;
        greenIntensity = 0;
        blueIntensity = 0;
        break;
    case CHAR_GREEN:
        redIntensity = 0;
        greenIntensity = COLOR_INTENSITY;
        blueIntensity = 0;
        break;
    case CHAR_BLUE:
        redIntensity = 0;
        greenIntensity = 0;
        blueIntensity = COLOR_INTENSITY;
        break;
    default:
        printf(FAIL_MSG_UNEXPECTED_COLOR);
        exit(FAIL_CODE_UNEXPECTED_COLOR);
        break;
    }
    // Print pixel
    printf("%3d %3d %3d ", redIntensity, greenIntensity, blueIntensity);
}

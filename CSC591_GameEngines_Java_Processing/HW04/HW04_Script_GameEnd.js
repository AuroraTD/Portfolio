/*************************************************************************************************************
 * FILE:            HW04_Script_GameEnd.js
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Script for ending a game
 *************************************************************************************************************/

"use strict";

// Class References
var oClientClass =	Java.type("HW04_Client");
var oGlobalsClass =	Java.type("HW04_Globals");

/*********************************************************************************************************
 * FUNCTION:        endGame
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     End the game
 * 
 * ARGUMENTS:       bWon -	True if the game was won, false if the game was lost
 * 
 * RETURNS:         None
 *********************************************************************************************************/
function endGame (bWon) {
	
	try {
		
		// Client reacts to the end of a game
		if (oGlobalsClass.bClient === true) {
			
			// Print a message to the terminal that the user will see when the Processing windows are gone
			if (bWon == true) {
				print("\nYou Won!!!  You got " + oGlobalsClass.oMyCharacterObject.getScore() + " points!!!\n");
			}
			else {
				print("\nYou Lost, but you got " + oGlobalsClass.oMyCharacterObject.getScore() + " points!\n");
			}
			
			// Stop the Client
            oClientClass.getInstance().shutDown();
            
		}
		else {
			// Server doesn't care if one player won / lost - maybe another player will join!
		}
	    
	}
	catch (e) {
		print(e.stack);
	}
	
}
/*************************************************************************************************************
 * FILE:            HW04_Script_BubbleShooter.js
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Script for functionality related to the Bubble Shooter game
 *************************************************************************************************************/

"use strict";

// Constants
var N_BUBBLE_ADD_PERIOD_MS =	10000;

// Class References
var oServerClass = 				Java.type("HW04_Server");
var oColorClass =				Java.type("HW04_Color");
var oRealTimeClass =			Java.type("HW04_Time_Real");
var oArrowClass =				Java.type("HW04_ObjectArrow");
var oBoundaryClass =			Java.type("HW04_ObjectBoundary");
var oBubbleClass =				Java.type("HW04_ObjectBubble");
var oSpawnPointClass =			Java.type("HW04_ObjectSpawnPoint");
var oZoneDeathClass =			Java.type("HW04_ObjectZoneDeath");
var oGlobalsClass =				Java.type("HW04_Globals");

// Variables
var nLastBubbleAdvance = 		0;

/*********************************************************************************************************
 * FUNCTION:        instructUser
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Instruct the user on how to play the game
 * 
 * ARGUMENTS:       bClient -	True if this is a client, false if this is the server
 * 
 * RETURNS:         None
 *********************************************************************************************************/
function instructUser (bClient) {
	
	try {
		
		print("\nBubble Shooter");
		
		if (bClient === true) {

            print("Win points by popping bubbles.");
            print("Pop bubbles by shooting a bubble at another of the same color.");
            print("The game ends when you shoot all the bubbles (win), or they reach you (lose).");
            print("Win as many points as you can before the game ends!\n");
			
		}
	    
	}
	catch (e) {
		print(e.stack);
	}
	
}

/*********************************************************************************************************
 * FUNCTION:        getSpaceBarAction
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Get a description of what pressing the space bar does
 * 
 * ARGUMENTS:       None
 * 
 * RETURNS:         sSpaceBarAction -	A description of what pressing the space bar does
 *********************************************************************************************************/
function getSpaceBarAction () {

	return "shoot";
	
}

/*********************************************************************************************************
 * FUNCTION:        isReplayEnabled
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Get a boolean describing whether or not replay is enabled
 * 
 * ARGUMENTS:       None
 * 
 * RETURNS:         bReplayEnabled -	True if replay is enabled, otherwise false
 *********************************************************************************************************/
function isReplayEnabled () {

	return false;
	
}

/*********************************************************************************************************
 * FUNCTION:        setUpNewCharacter
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Set up a new character object with reasonable defaults for this game
 * 
 * ARGUMENTS:       oCharacterObject -	The character object
 * 
 * RETURNS:         None
 *********************************************************************************************************/
function setUpNewCharacter (oCharacterObject) {
	
	try {
		
		// No horizontal motion
		oCharacterObject.setBaseSpeedHorizontal(0);
		
		// No vertical motion
		oCharacterObject.setBaseSpeedVertical(0);
		
		// Hide character object (it exists for historical and event handling reasons, but needn't be shown)
		oCharacterObject.setHiddenFlag(true);
	    
	}
	catch (e) {
		print(e.stack);
	}
	
}

/*********************************************************************************************************
 * FUNCTION:        populateGameWorld
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Populate the game world with game objects
 * 
 * ARGUMENTS:       None
 * 
 * RETURNS:         None
 *********************************************************************************************************/
function populateGameWorld () {
	
	try {
		
		// Populate everything but bubbles
		oBoundaryClass.addBoundaryObjects();
		oZoneDeathClass.addDeathZoneToBottomOfWindow();
		oSpawnPointClass.addSpawnPointAtBottomOfWindow();
		
		// Add two rows of bubbles
		oBubbleClass.addNewRowOfBubblesAtTopOfWindow();
		oBubbleClass.addNewRowOfBubblesAtTopOfWindow();
		nLastBubbleAdvance = oRealTimeClass.getInstance().getTime();
		
		// Add one shooter bubble
		oBubbleClass.addShooterBubbleAtBottomOfWindow();
		
		// Add one arrow
		oArrowClass.addArrowToLatestShooterBubble();
	    
	}
	catch (e) {
		print(e.stack);
	}
	
}

/*********************************************************************************************************
 * FUNCTION:        performGameLoopIterationClient
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Perform one game loop iteration
 * 
 * ARGUMENTS:       oCharacterObject -	The character object
 * 
 * RETURNS:         None
 *********************************************************************************************************/
function performGameLoopIterationClient (oCharacterObject) {
	
	try {
		
		// Client is pretty dumb in bubble shooter since there is no character object as such
	    
	}
	catch (e) {
		print(e.stack);
	}
	
}

/*********************************************************************************************************
 * FUNCTION:        performGameLoopIterationServer
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Perform one game loop iteration
 * 
 * ARGUMENTS:       None
 * 
 * RETURNS:         None
 *********************************************************************************************************/
function performGameLoopIterationServer () {
	
	try {

		// If it's time to add another row of bubbles, do so
		if (oRealTimeClass.getInstance().getTime() > nLastBubbleAdvance + N_BUBBLE_ADD_PERIOD_MS) {
			oBubbleClass.addNewRowOfBubblesAtTopOfWindow();
			nLastBubbleAdvance = oRealTimeClass.getInstance().getTime();
		}
		
		// Move any existing shooter bubbles
		oBubbleClass.moveShooterBubbles();
		
		// Update angle of shooter arrow
		oArrowClass.getLatestArrow().updateAngle();
	    
	}
	catch (e) {
		print(e.stack);
	}
	
}

/*********************************************************************************************************
 * FUNCTION:        handleEventUserInput
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Handle a user input event
 * 
 * ARGUMENTS:       oCharacterObject -	The character object
 * 					oEventToHandle - 	An event for which we have registered interest
 * 
 * RETURNS:         None
 *********************************************************************************************************/
function handleEventUserInput (oCharacterObject, oEventToHandle) {
	
	try {
		
		// Get event arguments
		var oEventArguments = oEventToHandle.getEventArguments();
		
		// Inputs the server acts on
		if (oGlobalsClass.bClient == false) {
			
			// SPACE
		    if (oEventArguments.get("sKey").equals("SPACE")) {
			    if (oEventArguments.get("bPressed").equals("TRUE")) {
			    	oBubbleClass.shootBubble(
			    			oArrowClass.getLatestArrow().getAngle()
	    			);
			    }
		    }
		    
		    // LEFT
		    if (oEventArguments.get("sKey").equals("LEFT")) {
			    if (oEventArguments.get("bPressed").equals("TRUE")) {
					// Rotate arrow
			    	oArrowClass.getLatestArrow().setMovementDirection(-1);
			    }
			    else if (oEventArguments.get("bPressed").equals("FALSE")) {
					// Stop rotating arrow
			    	oArrowClass.getLatestArrow().setMovementDirection(0);
			    }
		    }
		    
		    // RIGHT
		    else if (oEventArguments.get("sKey").equals("RIGHT")) {
			    if (oEventArguments.get("bPressed").equals("TRUE")) {
					// Rotate arrow
			    	oArrowClass.getLatestArrow().setMovementDirection(1);
			    }
			    else if (oEventArguments.get("bPressed").equals("FALSE")) {
					// Stop rotating arrow
			    	oArrowClass.getLatestArrow().setMovementDirection(0);
			    }
		    }
		    
		}
	    
	    // Inputs the client acts on
		if (oGlobalsClass.bClient == true) {
			
			// Client is pretty dumb in bubble shooter since there is no character object as such
			
		}
	    
	}
	catch (e) {
		print(e.stack);
	}
	
}
/*************************************************************************************************************
 * FILE:            HW04_Script_Platformer.js
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Script for functionality related to the 2D Platformer game
 *************************************************************************************************************/

"use strict";

//Class References
var oServerClass = 		Java.type("HW04_Server");
var oBoundaryClass =	Java.type("HW04_ObjectBoundary");
var oPlatformClass =	Java.type("HW04_ObjectPlatform");
var oSpawnPointClass =	Java.type("HW04_ObjectSpawnPoint");
var oZoneDeathClass =	Java.type("HW04_ObjectZoneDeath");
var oZoneWinClass =		Java.type("HW04_ObjectZoneWin");
var oGlobalsClass =		Java.type("HW04_Globals");

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
		
		print("\n2D Platformer");
		
		if (bClient === true) {

            print("You are player " + oGlobalsClass.nPlayerID);
            print("If you reach the TOP, you win a point!");
            print("If you fall to the BOTTOM, you lose a point ('the floor is lava')!\n");
			
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
	
	return "jump";
	
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

	return true;
	
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
		
		// Falling due to gravity
		oCharacterObject.setBaseSpeedVertical(oCharacterObject.getDefaultSpeed() * -1);
	    
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
		
		oBoundaryClass.addBoundaryObjects();
		oPlatformClass.addPlatformObjects();
		oSpawnPointClass.addSpawnPointToEveryStaticPlatform();
		oZoneDeathClass.addDeathZoneToBottomOfWindow();
		oZoneWinClass.addWinZoneToTopOfWindow();
	    
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
		
		oCharacterObject.handleJumpStatus();
		oCharacterObject.handlePlatformAdjacency();
		oCharacterObject.updateLocationAndCheckForCollisions();
	    
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
		
		oPlatformClass.movePlatforms();
	    
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
		
		// Inputs the server acts on (creation of new game objects)
		if (oGlobalsClass.bClient == false) {
			// None so far
		}
	    
	    // Inputs the client acts on (manipulation of client-controlled objects)
		if (oGlobalsClass.bClient == true) {
			
			// SPACE
		    if (oEventArguments.get("sKey").equals("SPACE")) {
			    if (oEventArguments.get("bPressed").equals("TRUE") && oCharacterObject.getJumpAllowed() == true) {
			    	oCharacterObject.startJumpSequence();
			    	oCharacterObject.setJumpAllowed(false);
			    }
			    else if (oEventArguments.get("bPressed").equals("FALSE")) {
			    	oCharacterObject.setJumpAllowed(true);
			    }
		    }
			
		    // LEFT
		    else if (oEventArguments.get("sKey").equals("LEFT")) {
			    if (oEventArguments.get("bPressed").equals("TRUE")) {
			    	oCharacterObject.setHorizontalDirection(-1);
			    }
			    else if (oEventArguments.get("bPressed").equals("FALSE")) {
			    	oCharacterObject.setHorizontalDirection(0);
			    }
		    }
		    
		    // RIGHT
		    else if (oEventArguments.get("sKey").equals("RIGHT")) {
			    if (oEventArguments.get("bPressed").equals("TRUE")) {
			    	oCharacterObject.setHorizontalDirection(1);
			    }
			    else if (oEventArguments.get("bPressed").equals("FALSE")) {     
			    	oCharacterObject.setHorizontalDirection(0);
			    }
		    }
			
		}

	}
	catch (e) {
		print(e.stack);
	}
	
}
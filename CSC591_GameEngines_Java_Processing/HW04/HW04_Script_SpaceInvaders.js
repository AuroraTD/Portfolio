/*************************************************************************************************************
 * FILE:            HW04_Script_SpaceInvaders.js
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Script for functionality related to the Space Invaders game
 *************************************************************************************************************/

"use strict";

//Class References
var oServerClass = 				Java.type("HW04_Server");
var oBoundaryClass =			Java.type("HW04_ObjectBoundary");
var oBulletClass =				Java.type("HW04_ObjectBullet");
var oEnemyClass =				Java.type("HW04_ObjectEnemy");
var oGameObjectClass =			Java.type("HW04_ObjectGame");
var oSpawnPointClass =			Java.type("HW04_ObjectSpawnPoint");
var oZoneDeathClass =			Java.type("HW04_ObjectZoneDeath");
var oGlobalsClass =				Java.type("HW04_Globals");

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
		
		print("\nSpace Invaders");
		
		if (bClient === true) {

            print("Win points by shooting enemy invaders.");
            print("The game ends when you destroy all the enemies (win)");
            print(" or they shoot you, or reach you (lose).");
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
		
		// Add multiple rows of invaders
		oEnemyClass.addNewRowOfEnemiesAtTopOfWindow();
		oEnemyClass.addNewRowOfEnemiesAtTopOfWindow();
	    
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
		
		// Update location and check for collisions on character object
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
		
		// Move any existing enemies
		oEnemyClass.moveEnemies();
		
		// Move any existing bullets
		oBulletClass.moveBullets();
	    
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
			
			// SPACE
		    if (oEventArguments.get("sKey").equals("SPACE")) {
			    if (
		    		oEventArguments.get("bPressed").equals("TRUE") && 
		    		oGameObjectClass.getObjectByGUID(oCharacterObject.getGUID()).getShootingAllowed() == true
	    		) {
			    	/* Once the character fires, it must wait until the bullet hits something before firing again
			    	 * 	It might hit an enemy, or the top of the window
			    	 * 	Why do we get character object by GUID instead of acting on it directly?
                     * 		Because, to eliminate "intelligent" code,
                     * 		both the server and the client replace game objects
                     * 		when they get updates on objects from network partners
                     * 		So we want the latest game object to act on the event,
                     * 		not some "stale" object
			    	 */
			    	oGameObjectClass.getObjectByGUID(oCharacterObject.getGUID()).setShootingAllowed(false);
			    	
			    	// Shoot
			    	oBulletClass.shootBullet(oCharacterObject);
			    	
			    }
		    }
			
		}
	    
	    // Inputs the client acts on (manipulation of client-controlled objects)
		if (oGlobalsClass.bClient == true) {
			
		    // LEFT
		    if (oEventArguments.get("sKey").equals("LEFT")) {
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
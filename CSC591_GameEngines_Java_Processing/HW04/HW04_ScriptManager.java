/*************************************************************************************************************
 * FILE:            HW04_ObjectCharacter.java
 *
 * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
 *
 * DESCRIPTION:     Define a class of object that represents a character object in the game world
 *************************************************************************************************************/

// IMPORTS
import javax.script.*;

// CLASS DEFINITION
public class HW04_ScriptManager {

	// The javax.script JavaScript engine used by this class
	private static ScriptEngine oScriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");
	
	// The Invocable reference to the engine
	private static Invocable oScriptInvocable = (Invocable) oScriptEngine;
	
    /*********************************************************************************************************
     * FUNCTION:        bindArgument
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Used to bind the provided object to the provided name 
     *                  in the scope of the scripts being executed by this engine.
     * 
     * ARGUMENTS:       sBindName -     Name to bind to object
     *                  oBindObject -   Object to bind to name
     * 
     * RETURNS:         None
     *********************************************************************************************************/
	public static void bindArgument (String sBindName, Object oBindObject) {
	    try {
	        oScriptEngine.put(sBindName, oBindObject);
	    }
	    catch (Throwable oError) {
	        HW04_Utility.handleError(oError);
	    }
	}
	
    /*********************************************************************************************************
     * FUNCTION:        loadScript
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Load the script source from the provided filename
     * 
     * ARGUMENTS:       sScriptFile -   Filename of the script to load
     * 
     * RETURNS:         None
     *********************************************************************************************************/
	public static void loadScript (String sScriptFile) {
		try {
		    
			oScriptEngine.eval(new java.io.FileReader(sScriptFile));
			
		}
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
	}

    /*********************************************************************************************************
     * FUNCTION:        invokeFunction
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Invoke the specified function of the script loaded by this engine
     * 
     * ARGUMENTS:       sFunctionName -   Name of the function to invoke
     * 
     * RETURNS:         [whatever the script function returns]
     *********************************************************************************************************/
	public static Object invokeFunction (String sFunctionName) {
	    Object oReturnValue = null;
		try {
		    oReturnValue = oScriptInvocable.invokeFunction(sFunctionName);
		}
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
		return oReturnValue;
	}
	
    /*********************************************************************************************************
     * FUNCTION:        invokeFunction
     *
     * AUTHOR:          attiffan    Aurora T. Tiffany-Davis
     *
     * DESCRIPTION:     Invoke the specified function of the script loaded by this engine
     * 
     * ARGUMENTS:       sFunctionName - Name of the function to invoke
     *                  asArguments -   Arguments to pass to the function
     * 
     * RETURNS:         None
     *********************************************************************************************************/
	public static void invokeFunction (String sFunctionName, Object... asArguments) {
		try {
			oScriptInvocable.invokeFunction(sFunctionName, asArguments);
		}
        catch (Throwable oError) {
            HW04_Utility.handleError(oError);
        }
	}

}


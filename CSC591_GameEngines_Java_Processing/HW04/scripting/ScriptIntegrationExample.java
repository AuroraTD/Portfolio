public class ScriptIntegrationExample {

	static GameObject go1 = new GameObject();
	static GameObject go2 = new GameObject();
	static GameObject go3 = new GameObject();

	public static void main(String[] args) {

		while(true) {

			/*  ONE (execute a script)
			ScriptManager.loadScript("scripts/hello_world.js");
			*/ // ONE

			/*  TWO and THREE (reference an object)
			ScriptManager.loadScript("scripts/print_object.js");

			ScriptManager.bindArgument("game_object", go1);
			ScriptManager.executeScript();
			*/ // TWO

			/* THREE (reference multiple objects)
			ScriptManager.bindArgument("game_object", go2);
			ScriptManager.executeScript();
			*/ // THREE

			/* FOUR and FIVE (pass in arguments to function)
			ScriptManager.loadScript("scripts/modify_position.js");

			ScriptManager.bindArgument("game_object", go1);
			ScriptManager.executeScript("5","5");

			ScriptManager.bindArgument("game_object", go2);
			ScriptManager.executeScript("10","15");
			ScriptManager.executeScript("25","-10");
			*/ // FOUR

			/* FIVE (more complicated scripts)
			ScriptManager.bindArgument("game_object", go3);
			ScriptManager.loadScript("scripts/random_position.js");
			for(int i=0; i<5; i++) {
				ScriptManager.executeScript();
			}
			*/ // FIVE

			/* SIX (more complicated scripts)
			ScriptManager.bindArgument("game_object_1", go1);
			ScriptManager.bindArgument("game_object_2", go2);
			ScriptManager.loadScript("scripts/random_object.js");
			for(int i=0; i<5; i++) {
				ScriptManager.executeScript();
			}
			*/ // SIX

			try { System.in.read(); }
			catch(Exception e) { }
		}

	}

}

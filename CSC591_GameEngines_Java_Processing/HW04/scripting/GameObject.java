public class GameObject {

	public static int GUID = 0;

	public int ID;
	public int x;
	public int y;

	public GameObject() {
		ID = ++GUID;
		x = 0;
		y = 0;
	}

	public void move(int x, int y) {
		this.x += x;
		this.y += y;
	}

	public String toString() {
		return "GameObject " + ID + " has position (" + x + ", " + y + ")";
	}

}

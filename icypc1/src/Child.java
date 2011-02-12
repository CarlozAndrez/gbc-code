import java.awt.Point;
import java.util.ArrayList;


class Child
{
	// Location of the child.
	Point pos = new Point();

	// True if the child is standing.
	boolean standing;

	// Side the child is on.
	int color;

	// What's the child holding.
	int holding;

	// How many more turns this child is dazed.
	int dazed;

	int runTimer = 0;
	
	Point runTarget = new Point();

    java.util.List<Integer> dazedTurnsList = new ArrayList<Integer>();

	public boolean canBeSeen()
	{
		return (pos.x > -1) && (pos.y > -1);
	}
	
	public String toString()
	{
		return new String("at: (" + pos.x +"," + pos.y + "), " + (standing? "S": "C") + ", color: " + color + ", holding: " + holding + ", dazed: " + dazed);
	}

	public boolean isFriend(Child c)
	{
		return color == c.color;
	}
	
	public boolean isFoe(Child c)
	{
		return color != c.color;
	}

    public int getNumberOfRecentDazedTurns(int numTurns) {
        int num = 0;
        if (dazedTurnsList.size() >= numTurns) {
            for (int i = 1; i <= numTurns; i++) {
                num += dazedTurnsList.get(dazedTurnsList.size() - i);
            }
        }
        return num;
    }
}

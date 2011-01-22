import java.awt.Point;
import java.io.PrintStream;


class Move
{
	
	// Action the child is making.
	String action = "idle";

	// Destination of this action (or null, if it doesn't need one) */
	Point dest = null;
	
	Move()
	{
		this.action = "idle";
		this.dest = null;
		
	}
	Move(String action)
	{
		this.action = action;
		this.dest = null;
	}
	
	Move (String action, Point dest)
	{
		this.action = action;
		this.dest = dest;
	}

	Move (String action, int x, int y)
	{
		this.action = action;
		this.dest = new Point(x, y);
	}
	
	public void writeAction(PrintStream out)
    {
	    /** Write out the child's move */
	    if (dest == null)
	    {
	    	out.println(action);
	    }
	    else
	    {
	    	out.println(action + " " + dest.x + " " + dest.y);
	    }
    }
}
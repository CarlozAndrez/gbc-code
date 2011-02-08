import java.awt.Point;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class Game
{
	/** Width and height of the playing field. */
	public static final int SIZE = 31;

	/** Number of children on each team. */
	public static final int CCOUNT = 4;

	/** Constants for the objects in each cell of the field */
	public static final int GROUND_EMPTY = 0; // Just powdered snow in this
	                                          // space.
	public static final int GROUND_TREE = 1; // A tree in this space
	public static final int GROUND_S = 2; // A small snowball in this space
	public static final int GROUND_M = 3; // A medium snowball in this space
	public static final int GROUND_MS = 4; // A small snowball on a medium one
	public static final int GROUND_L = 5; // A large snowball in this space
	public static final int GROUND_LM = 6; // A medium snowball on a large one.
	public static final int GROUND_LS = 7; // A small snowball on a large one.
	public static final int GROUND_SMR = 8; // A red Snowman in this space
	public static final int GROUND_SMB = 9; // A blue Snowman in this space
	public static final int GROUND_FRIEND = 10;
	public static final int GROUND_FOE = 11;

	/** Constants for the things a child can be holding */
	public static final int HOLD_EMPTY = 0; // Child is holding nothing
	public static final int HOLD_P1 = 1; // Child is holding one unit of
	                                     // powdered snow
	public static final int HOLD_P2 = 2; // Child is holding two units of
	                                     // powdered snow
	public static final int HOLD_P3 = 3; // Child is holding three units of
	                                     // powdered snow
	public static final int HOLD_S1 = 4; // Child is holding one small snowball.
	public static final int HOLD_S2 = 5; // Child is holding two small
	                                     // snowballs.
	public static final int HOLD_S3 = 6; // Child is holding three small
	                                     // snowballs.
	public static final int HOLD_M = 7; // Child is holding one medium snowball.
	public static final int HOLD_L = 8; // Child is holding one large snowball.

	/** Constant for the red player color */
	public static final int RED = 0;

	/** Constant for the blue player color */
	public static final int BLUE = 1;

	/** Height for a standing child. */
	public static final int STANDING_HEIGHT = 9;

	/** Height for a crouching child. */
	public static final int CROUCHING_HEIGHT = 6;

	/** Maximum Euclidean distance a child can throw. */
	public static final int THROW_LIMIT = 24;

	/** Snow capacity limit for a space. */
	public static final int MAX_PILE = 9;

	/** Snow that's too deep to move through. */
	public static final int OBSTACLE_HEIGHT = 6;

	// Constant used to mark child locations in the map.
	public static final int GROUND_CHILD = 10;

	// Constant returned from checkForObstructions indicating one was not found.
	private static final Point NO_OBSTRUCTION = new Point(-1, -1);

	// Current game score for self (red) and opponent (blue).
	int[] score = new int[2];

	// Current snow height in each cell.
	int[][] height = new int[Game.SIZE][Game.SIZE];

	// Contents of each cell.
	int[][] ground = new int[Game.SIZE][Game.SIZE];

	// List of children on the field, half for each team.
	Child[] cList = new Child[2 * Game.CCOUNT];

    List<Point> redSnowmen = new ArrayList<Point>();

	int turnNum;

	// Set to null to turn of debug output
	private static PrintStream dbgout = null; // System.err;

	public static void debug(String message)
	{
		if (dbgout != null) dbgout.println(message);
	}

	public Game()
	{
		for (int i = 0; i < cList.length; i++)
			cList[i] = new Child();
	}

	// Return true if this is not the last turn.
	public boolean readGameData(Scanner in)
	{
		turnNum = readTurn(in);
		if (turnNum < 0) return false;

		Game.debug("turn: " + turnNum);

		readField(in);
		readChildren(in);
		markChildren();

		return true;
	}

	private int readTurn(Scanner in)
	{
		String token = in.next();
		Game.debug("turn token: " + token);
		return Integer.parseInt(token);
	}

	private void readField(Scanner in)
	{
		String token;
		// Read current game score.
		score[Game.RED] = in.nextInt();
		score[Game.BLUE] = in.nextInt();

		// Parse the current map.
		for (int i = 0; i < Game.SIZE; i++)
		{
			for (int j = 0; j < Game.SIZE; j++)
			{
				// Can we see this cell?
				token = in.next();
				if (token.charAt(0) == '*')
				{
					height[i][j] = -1;
					ground[i][j] = -1;
				}
				else
				{
					height[i][j] = token.charAt(0) - '0';
					ground[i][j] = token.charAt(1) - 'a';
				}
                if (GROUND_SMR == ground[i][j])
                {
                    redSnowmen.add(new Point(i, j));   
                }
			}
		}
	}

	// Read the states of all the children.
	private void readChildren(Scanner in)
	{
		for (int nchild = 0; nchild < Game.CCOUNT * 2; nchild++)
		{
			readChild(in, cList[nchild]);

			// Compute child color based on it's index.
			cList[nchild].color = (nchild < Game.CCOUNT ? Game.RED : Game.BLUE);
		}
	}

	private void readChild(Scanner in, Child child)
	{
		String token;
		// Can we see this child?
		token = in.next();
		if (token.equals("*"))
		{
			child.pos.x = -1;
			child.pos.y = -1;

			// @todo, what do we set the other fields to if we can't see the
			// child?
		}
		else
		{
			// Record the child's location.
			child.pos.x = Integer.parseInt(token);
			child.pos.y = in.nextInt();

			Game.debug("token: " + token + ", pos: " + child.pos);

			// Read the stance, what the child is holding and how much
			// longer he's dazed.
			token = in.next();
			child.standing = token.equals("S");

			token = in.next();
			child.holding = token.charAt(0) - 'a';

			child.dazed = in.nextInt();
		}

		Game.debug("child: " + child);
	}

	private void markChildren()
	{
		// Mark all the children in the map, so they are easy to
		// look up.
		for (int i = 0; i < Game.CCOUNT * 2; i++)
		{
			Child c = cList[i];
			if (c.canBeSeen())
			{
				ground[c.pos.x][c.pos.y] = GROUND_CHILD;
			}
		}
	}

	// Find the nearest neighbor (child or red snowman).
	public int findNearestNeighbor(Point pos)
	{
		int nearDist = 1000;
		for (int i = 0; i < Game.SIZE; i++)
			for (int j = 0; j < Game.SIZE; j++)
				if ((i != pos.x || j != pos.y)
				        && (ground[i][j] == Game.GROUND_CHILD || ground[i][j] == Game.GROUND_SMR))
				{

					int dx = (pos.x - i);
					int dy = (pos.y - j);

					if (dx * dx + dy * dy < nearDist)
					    nearDist = dx * dx + dy * dy;
				}

		return nearDist;
	}

	public static int roundPathValue(double x)
	{
		if (x < 0)
		{
			return -(int) (Math.round(-x));
		}

		return (int) (Math.round(x));
	}

	// starts at integer position ( x1, y1 ) and moves toward position ( x2, y2 ) 
	// The movement will be implemented as n steps, where n = max( | x2 -
	// x1 |, | y2 - y1 | ). The steps will be spaced uniformly in time, with the
	// first step occurring 1/n of the way through the turn and subsequent steps
	// occurring at times 2/n, 3/n, … n/n during the turn. At time t/n, the
	// entity moves to integer location ( x1 + round( ( t ( x2 - x1 ) )/n ), y1
	// + round( ( t ( y2 - y1 ) )/n ) ). Thus, the entity will reach its
	// destination exactly at the end of the turn. Rounding is performed using
	// the following function. While the Java Math.round() functions rounds in
	// the positive direction for values halfway between two integers, the
	// function below rounds these values away from zero. This helps to ensure
	// that linear paths are symmetric for the red and the blue player.
	public Point[] linearPath(Point from, Point to, int steps)
	{
		int nsteps = Math.max(Math.abs(to.x - from.x), Math.abs(to.y - from.y));
		Point[] results = new Point[nsteps];
		for (int step = 0; step < nsteps; step++)
		{
			int time = step + 1;
			double increment = time / nsteps;
			results[step] = new Point(
					from.x + roundPathValue(increment * (to.x - from.x)),
					from.y + roundPathValue(increment * (to.y - from.y)));
		}
		return results;
	}
	
	boolean hasFriend(Point pos)
	{
		for (int nchild = 0; nchild < Game.CCOUNT; nchild++)
		{
			if (cList[nchild].pos == pos) return true;
		}
		return false;
	}
	
	boolean hasFoe(Point pos)
	{
		for (int nchild = Game.CCOUNT; nchild < (Game.CCOUNT * 2); nchild++)
		{
			if (cList[nchild].pos == pos) return true;
		}
		return false;
	}
	
	public Point checkForObstructions(Point[] path)
	{
		for (int step = 0; step < path.length; step++)
		{
			if (ground[path[step].x][path[step].y] == GROUND_TREE) return path[step];
			if (ground[path[step].x][path[step].y] == GROUND_SMR) return path[step];
			if (ground[path[step].x][path[step].y] == GROUND_SMB) return path[step];
			if (hasFriend(path[step])) return path[step];
			if (hasFoe(path[step])) return path[step];
			
			// Return height as a negative value.
			if (height[path[step].x][path[step].y] > 5) return path[step];
		}
		return NO_OBSTRUCTION;
	}
}

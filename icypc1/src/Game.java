import java.awt.Point;
import java.io.PrintStream;
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

	// Current game score for self (red) and opponent (blue).
	int[] score = new int[2];

	// Current snow height in each cell.
	int[][] height = new int[Game.SIZE][Game.SIZE];

	// Contents of each cell.
	int[][] ground = new int[Game.SIZE][Game.SIZE];

	// List of children on the field, half for each team.
	Child[] cList = new Child[2 * Game.CCOUNT];

	int turnNum;
	
	// Set to null to turn of debug output
	private static PrintStream dbgout = null; //System.err;
	
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
			cList[nchild].color = (nchild < Game.CCOUNT ? Game.RED
			        : Game.BLUE);
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
	
	public int findNearestNeighbor(Point pos)
	{
		// Find the nearest neighbor.
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
}

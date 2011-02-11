import java.awt.Point;

abstract class Strategy
{

	public abstract Move chooseNextAction(Game game, Child child);
	
	// There are 12 relative positions to which a child can run.
	private Point[] runOffsets = 
	{ 
		new Point(0, 2),
		new Point(-1, 1),
		new Point(0, 1),
		new Point(1, 1),
		new Point(-2, 0),
		new Point(-1, 0),
		new Point(1, 0),
		new Point(2, 0),
		new Point(-1, -1),
		new Point(0, -1),
		new Point(1, -1),
		new Point(0, -2)
	};
	
	// There are 4 relative positions to which a child can crawl.
	private Point[] crawlOffsets = 
	{ 
		new Point(0, 1),
		new Point(-1, 0),
		new Point(1, 0),
		new Point(0, -1)
	};

    // Return a value from 0.0 to 1.0, where 0.0 indicates this player strategy should not be a Planter
    // and 1.0 means the player strategy should be a Planter.  Values in between will be weighed
    // against other votes.
    abstract protected double voteOnBeingAPlanter(Game game, Child me);

    /**
	 * A destination is valid if its on the map and not blocked.
	 * NOTE! this may not be a valid test for a player who is trying to run 
	 * over a snowmen.
	 * 
	 * @todo MDK doesn't check for an obstruction between the runner and destination.
	 */
	private boolean isValidDestination(Game game, Point test)
	{
		// Make sure we test point is on the map.
		if (test.x < 0) return false;
		if (test.x >= Game.SIZE) return false;
		if (test.y < 0) return false;
		if (test.y >= Game.SIZE) return false;
		
		// Make sure there's no obstructions.
		if (game.ground[test.x][test.y] == Game.GROUND_TREE) return false;
		if (game.ground[test.x][test.y] == Game.GROUND_SMR) return false;
		if (game.ground[test.x][test.y] == Game.GROUND_SMB) return false;
		if (game.hasFriend(test)) return false;
		if (game.hasFoe(test)) return false;
		
		// Check the height of snow.
		if (game.height[test.x][test.y] > 5) return false;
		
		return true;
	}

	private Move moveToward(Game game, Child child, Point target, Point[] offsets, String moveType)
	{
		Move m;
		Point dest = chooseDestination(game, child, target, offsets);
		
		if (dest == null)
		{
			// @todo MDK if we can't find a legal move, maybe we should try again with a random destination?
			Game.debug("moveToward(): Unable to find legal move from (" + child.pos.x + ", " + child.pos.y + ") to (" + target.x + ", " + target.y + ")");
			m = new Move();
		}
		else
		{
			m = new Move(moveType, dest);
		}

		return m;
	}

	// Walk through all possible positions (as determined by the offset list)
	// and pick the closest non-blocked position to our target.
	private Point chooseDestination(Game game, Child child, Point target, Point[] offsets)
	{
		double nearest = 1000.0;
		Point dest = null;
		
		for (Point offset : offsets)
		{
			Point testPt = new Point(child.pos.x + offset.x, child.pos.y + offset.y);
			if (isValidDestination(game, testPt))
			{
				double dist = Point.distance(testPt.x, testPt.y, target.x, target.y);
				if (dist < nearest)
				{
					nearest = dist;
					dest = testPt;
				}
			}
		}
		return dest;
	}

	/**
	 * Create a Move action given a child and its intended destination.
	 */
	protected Move moveToward(Game game, Child child, Point target)
	{
		Move m = new Move();
		if (child.standing)
		{
			m = moveToward(game, child, target, runOffsets, "run");
		}
		else
		{
			m = moveToward(game, child, target, crawlOffsets, "crawl");
		}
		return m;
	}
}
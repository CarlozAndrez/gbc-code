// A simple player that just tries to plant snowmen in interesting
// places.
//
// Feel free to use this as a starting point for your own player.
// -> Thank you, I will ^^
//
// ICPC Challenge
// Sturgill, Baylor University

import java.awt.Point;

class PlanterStrategy extends Strategy
{
	// Constant for checking spacing.
	final static int ACCEPTABLE_DISTANCE = 5;
	
	// Constants that govern the position seeking strategy.
	final static double R0 = 4.0;
	final static double JITTER_LOWER = 0.5;
	final static double JITTER_UPPER = 1.5;
	
	// Various "repulsive factors"
	final static double RF_ENEMY = 1.0;
	final static double RF_FRIEND = 1.0;
	final static double RF_WALL = 4.0;

    private int costOfNotBuilding = 0;

	/**
	 * sequence of moves templates to build a snowman to the right of the player. For
	 * the first one, we're just looking for a place to build.
	 */
	private static final Move[] instructions = { 
		new Move("idle"), 
		new Move("crouch"),
		new Move("pickup", 1, 0),
		new Move("pickup", 1, 0),
		new Move("pickup", 1, 0), 
		new Move("crush"),
		new Move("drop", 1, 0), 
		new Move("pickup", 1, 1),
		new Move("pickup", 1, 1), 
		new Move("crush"),
	 	new Move("drop", 1, 0), 
	 	new Move("pickup", 1, 1),
	  	new Move("crush"), 
	  	new Move("drop", 1, 0), 
	  	new Move("stand"),
	};

	/** Current instruction this child is executing. */
	private int state = 0;
	
	private boolean isGoodPositionToBuild(Game game, Child child)
	{
		// Find the nearest neighbor.
		int nearDist = game.findNearestNeighbor(child.pos);

		// See if we should start running our build script.
		// Are we far from other things, is the ground empty
		// and do we have enough snow to build a snowman.
		return (nearDist > (ACCEPTABLE_DISTANCE * ACCEPTABLE_DISTANCE)
		        && child.pos.x < Game.SIZE - 1
		        && child.pos.y < Game.SIZE - 1
		        && game.ground[child.pos.x + 1][child.pos.y] == Game.GROUND_EMPTY
		        && game.ground[child.pos.x + 1][child.pos.y + 1] == Game.GROUND_EMPTY
		        && game.height[child.pos.x + 1][child.pos.y] >= 3
		        && game.height[child.pos.x + 1][child.pos.y + 1] >= 3
		        && child.holding == Game.HOLD_EMPTY);
	}

	private double calcRepulsiveWeight(int x1, int y1, int x2, int y2)
	{	
		double dist;
		dist = Point.distance(x1, y1, x2, y2);
		if (dist < 1.0)
		{
			Game.debug("calcRepulsiveWeight(): distance was < 1 " + dist);
			dist = 1.0;
		}
		return R0/(dist * dist);
	}

	// Use "antigravity technique" from robocode.
	private Move moveToBuildingPosition(Game game, Child child) 
	{
		
		// Compute a set of inverted weighted vectors relative to our current position
		// and sum them up.
		
		double dx = 0;
		double dy = 0;
		double weight;

		for (Child c : game.cList)
		{
			if (!c.pos.equals(child.pos) && c.canBeSeen())
			{
				weight = calcRepulsiveWeight(child.pos.x, child.pos.y, c.pos.x, c.pos.y);
				if (child.isFriend(c)) weight *= RF_FRIEND;
				else weight *= RF_ENEMY;
				
				// NB. order of subtraction is set up to invert the vectors.
				dx += weight * (child.pos.x - c.pos.x);
				dy += weight * (child.pos.y - c.pos.y);
			}
		}

		for (Point p : game.redSnowmen)
		{
			if (!p.equals(child.pos))
			{
				weight = calcRepulsiveWeight(child.pos.x, child.pos.y, p.x, p.y);
				dx += (int) Math.round(weight * (child.pos.x - p.x));
				dy += (int) Math.round(weight * (child.pos.y - p.y));
			}
		}
		
		// Apply some force to keep us away from the walls.
		weight = RF_WALL * calcRepulsiveWeight(child.pos.x, child.pos.y, child.pos.x, -1);
		dy += (int) Math.round(weight * (child.pos.y + 1));
		
		weight = RF_WALL * calcRepulsiveWeight(child.pos.x, child.pos.y, child.pos.x, Game.SIZE + 1);
		dy += (int) Math.round(weight * (child.pos.y - Game.SIZE - 1));
		
		weight = RF_WALL * calcRepulsiveWeight(child.pos.x, child.pos.y, -1, child.pos.y);
		dx += (int) Math.round(weight * (child.pos.x + 1));
		
		weight = RF_WALL * calcRepulsiveWeight(child.pos.x, child.pos.y, Game.SIZE + 1, child.pos.y);
		dx += (int) Math.round(weight * (child.pos.x - Game.SIZE - 1));
		
		// Add some jitter to prevent us being trapped between two obstacles.
		dx = game.makeRandomNumber(JITTER_LOWER*dx, JITTER_UPPER*dx);
		dy = game.makeRandomNumber(JITTER_LOWER*dy, JITTER_UPPER*dy);
		
		Point newPos = new Point(child.pos.x + (int)Math.round(dx), child.pos.y + (int)Math.round(dy));
		
		return moveToward(game, child, newPos);
}

	public Move chooseNextAction(Game game, Child child)
	{
		if (child.dazed > 0)
        {
            costOfNotBuilding++;
            costOfNotBuilding++;
            return new Move();
        }

		if (!isBuildingASnowman())
		{
			// Not building a snowman.

			// If we didn't get to finish the last snowman, maybe we're
			// holding something.
			// We should drop it.
			if (child.holding != Game.HOLD_EMPTY
			        && child.pos.y < Game.SIZE - 1
			        && game.height[child.pos.x][child.pos.y + 1] <= Game.MAX_PILE - 3)
            {
			    return new Move("drop", child.pos.x, child.pos.y + 1);
            }
			
			if (!isGoodPositionToBuild(game, child))
			{
                costOfNotBuilding++;
				return moveToBuildingPosition(game, child);
			}

			// We're not holding anything and we are in a good position to build.
			state = 1;
		}

		// We should only arrive here if we are inGoodPositionToBuild() or we 
		// are already in the process of building a snowman.
        costOfNotBuilding = 0;
		
        // Stamp out a move from our instruction template and return it.
        Move m = new Move(instructions[state].action);
        if (instructions[state].dest != null)
            m.dest = new Point(
            		child.pos.x + instructions[state].dest.x,
                    child.pos.y + instructions[state].dest.y);
        
        state = (state + 1) % instructions.length;

        return m;
	}

    public boolean isBuildingASnowman() 
    {
        return state != 0;
    }

    protected double voteOnBeingAPlanter(Game game, Child me) {
        if (isBuildingASnowman()) {
            return 1.0;
        }
        else if (costOfNotBuilding < 17) {
            return 1.0;
        }
        else {
            return 0.0;
        }
    }
}

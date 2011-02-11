import java.awt.Point;


class HunterStrategy extends Strategy
{
	private static final double MAX_THROWING_RANGE = 8.0;
	private static final double IDEAL_THROWING_RANGE = 5.0;
	private static final double R0 = 4.0;
	private static final double RF_FRIEND = 1.0;
	private static final double RF_ENEMY = 0.2;
	private static final double RF_WALL = 1.0;
	private static final double JITTER_LOWER = 0.5;
	private static final double JITTER_UPPER = 1.5;
	
	@Override
    public Move chooseNextAction(Game game, Child me)
    {
		Move m = new Move();
		Child victim;
		if (me.dazed == 0)
		{
			// If we don't have a snowball, get one.
			if (me.holding != Game.HOLD_S1)
			{
				m = armChild(game, me);
			}
			
		    // Stand up if the child is armed.
			else if (!me.standing)
		    {
		    	m = new Move("stand");
		    }
			else
			{
				victim = findVictim(game, me);
				if (inRange(me, victim))
				{
					// If target is in range, throw.
					m = attack(game, me, victim);
				}
				else
				{
					// Try to close on the next target.
					m = trackTarget(game, me, victim);
				}
			}
		}
		return m;
    }

	private Move armChild(Game game, Child me)
	{
		Move result = new Move();
		
	    // Crush into a snow ball, if we have snow.
	    if (me.holding == Game.HOLD_P1)
	    {
	    	result = new Move("crush");
	    }
	    else
	    {
	    	// We don't have snow, see if there is some nearby.
	    	Point snowAt = findSnow(game, me);
	
	    	// If there is snow, try to get it.
	    	if (snowAt.x >= 0)
	    	{
	    		result = getSnow(me.standing, snowAt);
	    	}
	    	else
	    	{
	    		// Move to random location looking for snow.
	    		// @todo MDK choose direction a little more effectively. Anti-gravity?
	    		result = seekTarget(game, me);
	    	}
	    }
	    
	    return result;
	}

	private Point findSnow(Game game, Child child)
	{
	    Point snowAt = new Point(-1, -1);
	    
	    // If there is another child adjacent to us return "false"
	    // so we don't compete for the same snow.
	    Child nearest = getNearestChild(game, child);
	    if ((nearest != null) && (Point.distance(child.pos.x, child.pos.y, nearest.pos.x, nearest.pos.y) < 3.0)) return snowAt;
	    
	    for (int ox = child.pos.x - 1; ox <= child.pos.x + 1; ox++)
	    	for (int oy = child.pos.y - 1; oy <= child.pos.y + 1; oy++)
	    	{
	    		// Is there snow to pick up?
	    		if (ox >= 0
	    		        && ox < Game.SIZE
	    		        && oy >= 0
	    		        && oy < Game.SIZE
	    		        && (ox != child.pos.x || oy != child.pos.y)
	    		        && game.ground[ox][oy] == Game.GROUND_EMPTY
	    		        && game.height[ox][oy] > 0)
	    		{
	    			snowAt.x = ox;
	    			snowAt.y = oy;
	    		}
	    	}
	    return snowAt;
	}

	// Return nearest visible child that is not me.
	private Child getNearestChild(Game game, Child me)
	{
		double nearest = 10000.0;
		Child result = null;
		for (Child ch : game.cList)
		{
			if (ch.canBeSeen() && !ch.pos.equals(me.pos))
			{
				double dist = Point.distance(ch.pos.x, ch.pos.y, me.pos.x, me.pos.y);
				if (dist < nearest)
				{
					nearest = dist;
					result = ch;
				}
			}
		}

		return result;
	}

	private Move getSnow(boolean standing, Point snowAt)
	{
		Move result;
	    if (standing)
	    {
	    	result = new Move("crouch");
	    }
	    else
	    {
	    	result = new Move("pickup", snowAt);
	    }
	    return result;
	}

	private Child findVictim(Game game, Child child)
	{
		Child victim = findNonDazedVictim(game, child);
		if (victim == null) victim = findAnyVictim(game, child);
		
		return victim;
	}
	
	private Child findAnyVictim(Game game, Child child)
	{
		return findVictimBelowDazedThreshold(game, child, 1000);
	}

	private Child findNonDazedVictim(Game game, Child child)
	{
		return findVictimBelowDazedThreshold(game, child, 0);
	}
	
	// Find a victim at less then or equal the specified "dazed" threshold.
	private Child findVictimBelowDazedThreshold(Game game, Child me, int dazedThreshold)
	{
		Child victim = null;
		int closest = 10000;
		
		for (Child other : game.cList)
		{
			// Look for the closest not-dazed foe.
			if (other.canBeSeen() && me.isFoe(other) && (other.dazed <= dazedThreshold))
			{
				int dx = me.pos.x - other.pos.x;
				int dy = me.pos.y - other.pos.y;
				int dist = dx * dx + dy * dy;
				if (dist < closest)
				{
					closest = dist;
					victim = other;
				}
			}
		}
		return victim;
	}

	private boolean inRange(Child me, Child victim)
	{	
		if (victim == null) return false;
		
		return Game.dist(me, victim) < MAX_THROWING_RANGE;
	}
	
	private boolean isThrowObstructed(Game game, Child me, Child victim)
	{
		Point[] path = game.linearPath(me.pos, victim.pos);
		
		return isThrowObstructed(game, path);
	}
	
	private boolean isThrowObstructed(Game game, Point[] path)
	{
		for (Point pt : path)
		{
			if (isThrowObstructed(game, pt)) return true;
		}
		return false;
	}
	
	private boolean isThrowObstructed(Game game, Point test)
	{
		// Make sure we test point is on the map.
		if (test.x < 0) return true;
		if (test.x >= Game.SIZE) return true;
		if (test.y < 0) return true;
		if (test.y >= Game.SIZE) return true;
		
		// Make sure there's no obstructions.
		if (game.ground[test.x][test.y] == Game.GROUND_TREE) return true;
		if (game.ground[test.x][test.y] == Game.GROUND_SMR) return true;
		if (game.ground[test.x][test.y] == Game.GROUND_SMB) return true;
		if (game.hasFriend(test)) return true;
		
		// We don't care if a square is occupied by a foe, even if its not 
		// our intended target.
		// if (game.hasFoe(test)) return true;
		
		// Check the height of snow.
		if (game.height[test.x][test.y] > 5) return true;
		
		return false;
	}
	
	private Move attack(Game game, Child me, Child victim)
	{
		Move result;
		if (!isThrowObstructed(game, me, victim))
		{
			int dx = victim.pos.x - me.pos.x;
			int dy = victim.pos.y - me.pos.y;
	
			// Compute a destination beyond the victim
			result =  new Move("throw", new Point(me.pos.x + (dx * 2), me.pos.y + (dy * 2)));
		}
		else
		{
			Game.debug("attack() throw from " + Game.p2s(me.pos)+ " to " + Game.p2s(victim.pos) + " is obstructed"); 
			result = jink(game, me, victim);
		}
		
		return result;
	}

	// Make a short move perpendicular to the line between us.
	private Move jink(Game game, Child me, Child victim)
	{
		int dx = victim.pos.x - me.pos.x;
		int dy = victim.pos.y - me.pos.y;
		
		if (game.makeRandomNumber(0, 1) == 0) dx = -1 * dx;
		else dy = -1 * dy;

		// Note that dy is added to the x position and dx to the y position.
		return moveToward(game, me, me.pos.x + dy, me.pos.y + dx);
	}

	// Move towards the indicated victim if it exists, otherwise move randomly.
	private Move trackTarget(Game game, Child me, Child victim)
	{
		if (victim != null) return moveToward(game, me, attackPos(me.pos, victim.pos));
		return seekTarget(game, me);
	}
	
	// Choose a point near the victim (on a line between me and the victim)
	// put not actually on top of them.
	private Point attackPos(Point me, Point victim)
	{
		double dist = Game.dist(me, victim);
		double weight = (dist - IDEAL_THROWING_RANGE)/dist;
		
		int dx = (int)Math.round(weight * (victim.x - me.x));
		int dy = (int)Math.round(weight * (victim.y - me.y));

		// Compute a destination near the victim.
		return new Point(me.x + dx, me.y + dy);
	}

	private double calcRepulsiveWeight(Child me, Point pos)
	{	
		return calcRepulsiveWeight(me, pos.x, pos.y);
	}

	private double calcRepulsiveWeight(Child me, int px, int py)
	{	
		double dist;
		dist = Point.distance(me.pos.x, me.pos.y, px, py);
		if (dist < 1.0)
		{
			Game.debug("calcRepulsiveWeight(): distance was < 1 " + dist);
			dist = 1.0;
		}
		return R0/(dist * dist);
	}
	
	// Use "antigravity technique" from to move to a new position from which we can 
	// look for another victim.
	private Move seekTarget(Game game, Child child) 
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
				weight = calcRepulsiveWeight(child, c.pos);
				if (child.isFriend(c)) weight *= RF_FRIEND;
				else weight *= RF_ENEMY;
				
				// NB. order of subtraction is set up to invert the vectors.
				dx += weight * (child.pos.x - c.pos.x);
				dy += weight * (child.pos.y - c.pos.y);
			}
		}

		// Apply some force to keep us away from the walls.
		weight = RF_WALL * calcRepulsiveWeight(child, child.pos.x, -1);
		dy += (int) Math.round(weight * (child.pos.y + 1));
		
		weight = RF_WALL * calcRepulsiveWeight(child, child.pos.x, Game.SIZE + 1);
		dy += (int) Math.round(weight * (child.pos.y - Game.SIZE - 1));
		
		weight = RF_WALL * calcRepulsiveWeight(child, -1, child.pos.y);
		dx += (int) Math.round(weight * (child.pos.x + 1));
		
		weight = RF_WALL * calcRepulsiveWeight(child, Game.SIZE + 1, child.pos.y);
		dx += (int) Math.round(weight * (child.pos.x - Game.SIZE - 1));
		
		// Add some jitter to prevent us being trapped between two obstacles.
		dx = game.makeRandomNumber(JITTER_LOWER*dx, JITTER_UPPER*dx);
		dy = game.makeRandomNumber(JITTER_LOWER*dy, JITTER_UPPER*dy);
		
		Point newPos = new Point(child.pos.x + (int)Math.round(dx), child.pos.y + (int)Math.round(dy));
		
		return moveToward(game, child, newPos);
	}

    protected double voteOnBeingAPlanter(Game game, Child me) {
        // vote to become a Planter if empty handed
        if (me.holding == Game.HOLD_EMPTY)
            return 1.0;
        else
            return 0.0;
    }
}

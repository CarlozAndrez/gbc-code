import java.awt.Point;


class HunterStrategy extends Strategy
{
	private static final double THROWING_RANGE = 8.0;
	
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
					m = attack(me, victim);
					Game.debug("chooseNextAction(): attacking: " + m + ", me: " + Game.p2s(me.pos) + ", victim: " + Game.p2s(victim.pos));
				}
				else
				{
					// Try to close on the next target.
					m = seekTarget(game, me, victim);
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
	    		result = runToRandomLocation(game, me);
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
		
		double dist = Point.distance(me.pos.x, me.pos.y, victim.pos.x, victim.pos.y);
		
		Game.debug("inRange(): me: " + Game.p2s(me.pos) + ", victim: " + Game.p2s(victim.pos) + ", dist: " + dist);
		
		return dist < THROWING_RANGE;
	}
	
	private Move attack(Child me, Child victim)
	{
		int dx = victim.pos.x - me.pos.x;
		int dy = victim.pos.y - me.pos.y;

		// Compute a destination beyond the victim
		return new Move("throw", new Point(me.pos.x + (dx * 2), me.pos.y + (dy * 2)));

	}

	// Move towards the indicated victim if it exists, otherwise move randomly.
	private Move seekTarget(Game game, Child me, Child victim)
	{
		if (victim != null) return moveToward(game, me, victim.pos);
		return runToRandomLocation(game, me);
	}
	
	private Move runToRandomLocation(Game game, Child me)
	{
		return moveToward(game, me, new Point(game.makeRandomNumber(0, Game.SIZE), game.makeRandomNumber(0, Game.SIZE)));	
	}

}

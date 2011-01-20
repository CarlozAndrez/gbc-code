import java.awt.Point;
import java.util.Random;


public class HunterStrategy extends Strategy
{
	/** Source of randomness for this player. */
	static Random rnd = new Random();
	
	/* (non-Javadoc)
     * @see Strategy#chooseNextAction(Game, Game.Child)
     */
	@Override
    public Game.Move chooseNextAction(Game game, Game.Child child)
    {
		Game.Move m = new Game.Move();
		if (child.dazed == 0)
		{
			// See if the child needs a new destination.
			while (child.runTimer <= 0 || child.runTarget.equals(child.pos))
			{
				child.runTarget.setLocation(rnd.nextInt(Game.SIZE),
				        rnd.nextInt(Game.SIZE));
				child.runTimer = 1 + rnd.nextInt(14);
			}

			// Try to acquire a snowball if we need one.
			if (child.holding != Game.HOLD_S1)
			{
				armChild(game, child, m);
			}
			else
			{
				searchAndAttack(game, child, m);
			}

			// Try to run toward the destination.
			if (m.action.equals("idle"))
			{
				m = moveToward(child, child.runTarget);
				child.runTimer--;
			}
		}
		return m;
    }

	private void searchAndAttack(Game game, Game.Child c, Game.Move m)
    {
	    // Stand up if the child is armed.
	    if (!c.standing)
	    {
	    	m.action = "stand";
	    }
	    else
	    {
	    	// Try to find a victim.
	    	findVictim(game, c, m);
	    	
	    	// @todo what do you do if the victim is not found?
	    }
    }

	private void armChild(Game game, Game.Child c, Game.Move m)
    {
	    // Crush into a snow ball, if we have snow.
	    if (c.holding == Game.HOLD_P1)
	    {
	    	m.action = "crush";
	    }
	    else
	    {
	    	// We don't have snow, see if there is some nearby.
	    	Point snowAt = findSnow(game, c);

	    	// If there is snow, try to get it.
	    	if (snowAt.x >= 0)
	    	{
	    		getSnow(c.standing, m, snowAt);
	    	}
	    }
    }

	private void getSnow(boolean standing, Game.Move m, Point snowAt)
    {
	    if (standing)
	    {
	    	m.action = "crouch";
	    }
	    else
	    {
	    	m.action = "pickup";
	    	m.dest = snowAt;
	    }
    }

	private Point findSnow(Game game, Game.Child c)
    {
	    Point snowAt = new Point(-1, -1);
	    for (int ox = c.pos.x - 1; ox <= c.pos.x + 1; ox++)
	    	for (int oy = c.pos.y - 1; oy <= c.pos.y + 1; oy++)
	    	{
	    		// Is there snow to pick up?
	    		if (ox >= 0
	    		        && ox < Game.SIZE
	    		        && oy >= 0
	    		        && oy < Game.SIZE
	    		        && (ox != c.pos.x || oy != c.pos.y)
	    		        && game.ground[ox][oy] == Game.GROUND_EMPTY
	    		        && game.height[ox][oy] > 0)
	    		{
	    			snowAt.x = ox;
	    			snowAt.y = oy;
	    		}
	    	}
	    return snowAt;
    }

	private boolean findVictim(Game game, Game.Child c, Game.Move m)
	{
		boolean victimFound = false;
		for (int j = Game.CCOUNT; !victimFound && j < Game.CCOUNT * 2; j++)
		{
			if (game.cList[j].pos.x >= 0)
			{
				int dx = game.cList[j].pos.x - c.pos.x;
				int dy = game.cList[j].pos.y - c.pos.y;
				int dsq = dx * dx + dy * dy;
				if (dsq < 8 * 8)
				{
					victimFound = true;
					m.action = "throw";
					// throw past the victim, so we will
					// probably hit them
					// before the snowball falls into the
					// snow.
					m.dest = new Point(c.pos.x + dx * 2, c.pos.y + dy * 2);
				}
			}
		}
		return victimFound;
	}


}

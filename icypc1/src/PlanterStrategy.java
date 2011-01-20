// A simple player that just tries to plant snowmen in interesting
// places.
//
// Feel free to use this as a starting point for your own player.
//
// ICPC Challenge
// Sturgill, Baylor University

import java.awt.Point;
import java.util.Random;

public class PlanterStrategy extends Strategy
{
	/** Source of randomness for this player. */
	private static Random rnd = new Random();

	/**
	 * sequence of moves templates to build a snowman to the right of the player. For
	 * the first one, we're just looking for a place to build.
	 */
	private static final Game.Move[] instructions = { 
		new Game.Move("idle"), 
		new Game.Move("crouch"),
		new Game.Move("pickup", 1, 0),
		new Game.Move("pickup", 1, 0),
		new Game.Move("pickup", 1, 0), 
		new Game.Move("crush"),
		new Game.Move("drop", 1, 0), 
		new Game.Move("pickup", 1, 1),
		new Game.Move("pickup", 1, 1), 
		new Game.Move("crush"),
	 	new Game.Move("drop", 1, 0), 
	 	new Game.Move("pickup", 1, 1),
	  	new Game.Move("crush"), 
	  	new Game.Move("drop", 1, 0), 
	  	new Game.Move("stand"),
	};

	/** Current instruction this child is executing. */
	private int state = 0;

	public Game.Move chooseNextAction(Game game, Game.Child child)
	{
		if (child.dazed > 0) return new Game.Move();

		if (state == 0)
		{
			// Not building a snowman.

			// If we didn't get to finish the last snowman, maybe we're
			// holding something.
			// We should drop it.
			if (child.holding != Game.HOLD_EMPTY
			        && child.pos.y < Game.SIZE - 1
			        && game.height[child.pos.x][child.pos.y + 1] <= Game.MAX_PILE - 3)
			    return new Game.Move("drop", child.pos.x, child.pos.y + 1);

			// Find the nearest neighbor.
			int nearDist = game.findNearestNeighbor(child.pos);

			// See if we should start running our build script.
			// Are we far from other things, is the ground empty
			// and do we have enough snow to build a snowman.
			if (nearDist > 5 * 5
			        && child.pos.x < Game.SIZE - 1
			        && child.pos.y < Game.SIZE - 1
			        && game.ground[child.pos.x + 1][child.pos.y] == Game.GROUND_EMPTY
			        && game.ground[child.pos.x + 1][child.pos.y + 1] == Game.GROUND_EMPTY
			        && game.height[child.pos.x + 1][child.pos.y] >= 3
			        && game.height[child.pos.x + 1][child.pos.y + 1] >= 3
			        && child.holding == Game.HOLD_EMPTY)
			{
				// Start trying to build a snowman.
				state = 1;
			}
		}

		// Are we building a snowman?
		if (state > 0)
		{
			// Stamp out a move from our instruction template and return it.
			Game.Move m = new Game.Move(instructions[state].action);
			if (instructions[state].dest != null)
			    m.dest = new Point(child.pos.x + instructions[state].dest.x,
			            child.pos.y + instructions[state].dest.y);
			state = (state + 1) % instructions.length;

			return m;
		}

		// Run around looking for a good place to build

		// See if the child needs a new, random destination.
		while (child.runTimer <= 0 || child.runTarget.equals(child.pos))
		{
			// Pick somewhere to run, omit the top and righmost edges.
			child.runTarget.setLocation(rnd.nextInt(Game.SIZE - 1),
			        rnd.nextInt(Game.SIZE - 1));
			child.runTimer = 1 + rnd.nextInt(14);
		}

		child.runTimer--;
		return moveToward(child, child.runTarget);
	}
}

import java.awt.Point;

abstract class Strategy
{

	public abstract Move chooseNextAction(Game game, Child child);

	/** Return the value of x, clamped to the [ a, b ] range. */
	private static int clamp(int x, int a, int b)
	{
		if (x < a) return a;
		if (x > b) return b;
		return x;
	}

	/**
	 * Create a Game.Move action given a child and its intended destination.
	 */
	protected Move moveToward(Child child, Point target)
	{
		Move m = new Move();
		if (child.standing)
		{
			m = runToward(child, target);
		}
		else
		{
			m = crawlTowards(child, target);
		}
		return m;
	}

	private Move crawlTowards(Child child, Point target)
	{
		Move m;

		// Crawl to the destination
		if (child.pos.x != target.x)
		{
			// crawl left or right
			Point dest = new Point(child.pos.x + clamp(target.x - child.pos.x, -1, 1), child.pos.y);
			m = new Move("crawl", dest);
		}
		else if (child.pos.y != target.y)
		{
			// crawl up or down.
			Point dest = new Point(child.pos.x, child.pos.y + clamp(target.y - child.pos.y, -1, 1));
			m = new Move("crawl", dest);

		}
		else
		{
			m = new Move();
		}

		return m;
	}

	private Move runToward(Child child, Point target)
	{
		Move m;

		// Run to the destination
		if (child.pos.x != target.x)
		{
			if (child.pos.y != target.y)
			{
				// Run diagonally.
				Point dest = new Point(child.pos.x + clamp(target.x - child.pos.x, -1, 1),
				        child.pos.y + clamp(target.y - child.pos.y, -1, 1));

				m = new Move("run", dest);
			}
			else
			{
				// Run left or right
				Point dest = new Point(child.pos.x + clamp(target.x - child.pos.x, -2, 2),
				        child.pos.y);
				m = new Move("run", dest);
			}
		}
		else if (child.pos.y != target.y)
		{
			// Run up or down.
			Point dest = new Point(child.pos.x, child.pos.y
			        + clamp(target.y - child.pos.y, -2, 2));
			m = new Move("run", dest);
		}
		else
		{
			m = new Move();
		}
		return m;
	}

}
import java.awt.Point;

public abstract class Strategy
{

	public abstract Game.Move chooseNextAction(Game game, Game.Child child);

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
	protected Game.Move moveToward(Game.Child child, Point target)
	{
		Game.Move m = new Game.Move();
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

	private Game.Move crawlTowards(Game.Child child, Point target)
	{
		Game.Move m;

		// Crawl to the destination
		if (child.pos.x != target.x)
		{
			// crawl left or right
			Point dest = new Point(child.pos.x + clamp(target.x - child.pos.x, -1, 1), child.pos.y);
			m = new Game.Move("crawl", dest);
		}
		else if (child.pos.y != target.y)
		{
			// crawl up or down.
			Point dest = new Point(child.pos.x, child.pos.y + clamp(target.y - child.pos.y, -1, 1));
			m = new Game.Move("crawl", dest);

		}
		else
		{
			m = new Game.Move();
		}

		return m;
	}

	private Game.Move runToward(Game.Child child, Point target)
	{
		Game.Move m;

		// Run to the destination
		if (child.pos.x != target.x)
		{
			if (child.pos.y != target.y)
			{
				// Run diagonally.
				Point dest = new Point(child.pos.x + clamp(target.x - child.pos.x, -1, 1),
				        child.pos.y + clamp(target.y - child.pos.y, -1, 1));

				m = new Game.Move("run", dest);
			}
			else
			{
				// Run left or right
				Point dest = new Point(child.pos.x + clamp(target.x - child.pos.x, -2, 2),
				        child.pos.y);
				m = new Game.Move("run", dest);
			}
		}
		else if (child.pos.y != target.y)
		{
			// Run up or down.
			Point dest = new Point(child.pos.x, child.pos.y
			        + clamp(target.y - child.pos.y, -2, 2));
			m = new Game.Move("run", dest);
		}
		else
		{
			m = new Game.Move();
		}
		return m;
	}

}
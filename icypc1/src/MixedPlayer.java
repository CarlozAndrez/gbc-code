// A simple player that just tries to plant snowmen in interesting
// places.
//
// Feel free to use this as a starting point for your own player.
//
// ICPC Challenge
// Sturgill, Baylor University

import java.awt.*;
import java.util.Scanner;

public class MixedPlayer
{
    private static Point[] snowmanPositions = {new Point(7, 7), new Point(7, 24), new Point(24, 24), new Point(24, 7)};

	public static void main(String[] args)
	{
		Game game = new Game();
		Strategy[] strategy = getInitialFieldStrategy(game);

		// Scanner to parse input from the game engine.
		Scanner in = new Scanner(System.in);

		// Keep reading states until the game ends.
		while (game.readGameData(in))
		{
            for (Point p : snowmanPositions)
            {
                if (!isRedSnowmanBuiltFor(p, 5, game))
                {
                    game.cList[0].runTarget = p;
                }
            }

			// Decide what each child should do
            for (int nchild = 0; nchild < Game.CCOUNT; nchild++)
            {
                Move move = strategy[nchild].chooseNextAction(game, game.cList[nchild]);
                move.writeAction(System.out);
            }
		}
	}

    private static Strategy[] getInitialFieldStrategy(Game game)
    {
        Strategy[] strategy = new Strategy[Game.CCOUNT];

        int CHILD_INDEX = 0;
        strategy[CHILD_INDEX] = new PlanterStrategy();
        game.cList[CHILD_INDEX].runTarget = snowmanPositions[0];

        CHILD_INDEX++;
        strategy[CHILD_INDEX] = new DefenderStrategy(game.cList[0]);

        CHILD_INDEX++;
        strategy[CHILD_INDEX] = new HunterStrategy();
        game.cList[CHILD_INDEX].runTarget = new Point(10, 20);

        CHILD_INDEX++;
        strategy[CHILD_INDEX] = new HunterStrategy();
        game.cList[CHILD_INDEX].runTarget = new Point(24, 8);

        return strategy;
    }

    private static boolean isRedSnowmanBuiltFor(Point p, int proximity, Game game)
    {
        for(Point snowmanPoint : game.redSnowmen)
        {
            if (isWithinProximity(p, snowmanPoint, proximity)) {
                return true;
            }
        }
        return false;
    }

    protected static boolean isWithinProximity(Point p1, Point p2, int proximity)
    {
        int dx = p1.x - p2.x;
        int dy = p1.y - p2.y;
        int dsq = dx * dx + dy * dy;
        return dsq < proximity * proximity;
    }
}

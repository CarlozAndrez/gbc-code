// A simple player that just tries to plant snowmen in interesting
// places.
//
// Feel free to use this as a starting point for your own player.
//
// ICPC Challenge
// Sturgill, Baylor University

import java.awt.*;
import java.util.ArrayList;
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
            assignPlanter(game, strategy, 0);
            assignDefender(game, strategy, 1, 0);

            ArrayList<Integer> visibleEnemyChildren = game.getVisibleEnemyChildren();
            if (visibleEnemyChildren.size() > 0)
            {
                Integer enemyIndex = game.getNearestEnemyIndex(game.cList[2], visibleEnemyChildren);
                visibleEnemyChildren.remove(enemyIndex);
                assignHunter(game, strategy, 2, enemyIndex);
            }
            if (visibleEnemyChildren.size() > 0)
            {
                Integer enemyIndex = game.getNearestEnemyIndex(game.cList[3], visibleEnemyChildren);
                visibleEnemyChildren.remove(enemyIndex);
                assignHunter(game, strategy, 3, enemyIndex);
            }

			// Decide what each child should do
            for (int nchild = 0; nchild < Game.CCOUNT; nchild++)
            {
                Move move = strategy[nchild].chooseNextAction(game, game.cList[nchild]);
                move.writeAction(System.out);
            }
		}
	}

    private static void assignPlanter(Game game, Strategy[] sList, int childIndex)
    {
        if (!(sList[childIndex] instanceof PlanterStrategy))
        {
            sList[childIndex] = new PlanterStrategy();
        }

        if (!((PlanterStrategy) sList[childIndex]).isBuildingASnowman())
        {
            for (Point p : snowmanPositions)
            {
                if (!isRedSnowmanBuiltFor(p, 5, game))
                {
                    game.cList[childIndex].runTarget = p;
                    break;
                }
            }
        }
    }

    private static void assignDefender(Game game, Strategy[] sList, int childIndex, int defendantChildIndex)
    {
        if (!(sList[childIndex] instanceof DefenderStrategy))
        {
            sList[childIndex] = new DefenderStrategy(game.cList[defendantChildIndex]);
        }
    }

    private static void assignHunter(Game game, Strategy[] sList, int childIndex, Point p)
    {
        if (!(sList[childIndex] instanceof HunterStrategy))
        {
            sList[childIndex] = new HunterStrategy();
        }

        game.cList[childIndex].runTarget = p;
    }

    private static void assignHunter(Game game, Strategy[] sList, int childIndex, int enemyChildIndex)
    {
        if (!(sList[childIndex] instanceof HunterStrategy))
        {
            sList[childIndex] = new HunterStrategy();
        }

        if (game.cList[enemyChildIndex].canBeSeen())
        {
            game.cList[childIndex].runTarget = game.cList[enemyChildIndex].pos;
        }
        else
        {
            // ToDo:
        }
    }

    private static Strategy[] getInitialFieldStrategy(Game game)
    {
        Strategy[] strategy = new Strategy[Game.CCOUNT];

        assignPlanter(game, strategy, 0);
        assignDefender(game, strategy, 1, 0);
        assignHunter(game, strategy, 2, new Point(10, 20));
        assignHunter(game, strategy, 3, new Point(24, 8));

        return strategy;
    }

    private static boolean isRedSnowmanBuiltFor(Point p, int proximity, Game game)
    {
        for(Point snowmanPoint : game.redSnowmen)
        {
            if (game.isWithinProximity(p, snowmanPoint, proximity)) {
                return true;
            }
        }
        return false;
    }
}

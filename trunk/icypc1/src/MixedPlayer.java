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

            ArrayList<Integer> visibleEnemyChildren = getVisibleEnemyChildren(game);
            if (visibleEnemyChildren.size() > 0)
            {
                Integer enemyIndex = getNearestEnemyIndex(game, 2, visibleEnemyChildren);
                visibleEnemyChildren.remove(enemyIndex);
                assignHunter(game, strategy, 2, enemyIndex);
            }
            if (visibleEnemyChildren.size() > 0)
            {
                Integer enemyIndex = getNearestEnemyIndex(game, 3, visibleEnemyChildren);
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

    private static ArrayList<Integer> getVisibleEnemyChildren(Game game)
    {
        ArrayList<Integer> visibleEnemyChildren = new ArrayList<Integer>();
        for (int index = 4; index <= 7; index++)
        {
            if (game.cList[index].canBeSeen())
            {
                visibleEnemyChildren.add(index);
            }
        }
        return visibleEnemyChildren;
    }

    private static int getNearestEnemyIndex(Game game, int childIndex, ArrayList<Integer> visibleEnemyChildren)
    {
        double distance = 999;
        int dazedCount = 999; // todo: consider dazed values
        int index = visibleEnemyChildren.get(0);
        for (Integer i : visibleEnemyChildren)
        {
            double pDistance = getDistance(game.cList[childIndex].pos, game.cList[i].pos);
            if (pDistance < distance)
            {
                distance = pDistance;
                index = i;
            }
        }

        return index;
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
            if (isWithinProximity(p, snowmanPoint, proximity)) {
                return true;
            }
        }
        return false;
    }

    protected static boolean isWithinProximity(Point p1, Point p2, int proximity)
    {
        return getDistance(p1, p2) < proximity;
    }

    protected static double getDistance(Point p1, Point p2)
    {
        int dx = p1.x - p2.x;
        int dy = p1.y - p2.y;
        int dsq = dx * dx + dy * dy;
        return Math.sqrt(dsq);
    }
}

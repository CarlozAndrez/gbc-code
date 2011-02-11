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

    private static Strategy[] strategy;

	public static void main(String[] args)
	{
		Game game = new Game();
		strategy = getInitialFieldStrategy(game);

		// Scanner to parse input from the game engine.
		Scanner in = new Scanner(System.in);

		// Keep reading states until the game ends.
		while (game.readGameData(in))
		{
            // get a list of the planters
            java.util.List<Integer> planters = new ArrayList<Integer>();
            for (int i = 0; i <= 3; i++) {
                if (strategy[i] instanceof PlanterStrategy)
                {
                    planters.add(i);
                }
            }

            // consider flipping a planter to a hunter
            if (planters.size() >= 2)
            {
                int index = -1;
                for (int i : planters)
                {
                    if (game.cList[i].getNumberOfRecentDazedTurns(10) > 6)
                    {
                        index = i;
                        break;
                    }
                }
                if (index != -1)
                {
                    assignHunter(game, strategy, index);
                    planters.remove((Integer) index);
                }
            }

            // consider reassigning planters to hunters
            for (Integer i : planters) {
                if (strategy[i].voteOnBeingAPlanter(game, game.cList[i]) < 0.1) {
                    pickHunterToBecomePlanter(game);
                    assignHunter(game, strategy, i);
                }
            }

			// Decide what each child should do
            for (int nchild = 0; nchild < Game.CCOUNT; nchild++)
            {
                Move move = strategy[nchild].chooseNextAction(game, game.cList[nchild]);
                Game.debug("move " + move + ", for child " + nchild);
                move.writeAction(System.out);
            }
		}
	}

    private static void pickHunterToBecomePlanter(Game game) {
        int hunterIndex = -1;
        double bestVote = 0.0;
        for (int i = 0; i <= 3; i++) {
            if (strategy[i] instanceof HunterStrategy && strategy[i].voteOnBeingAPlanter(game, game.cList[i]) > bestVote) {
                hunterIndex = i;
            }
        }
        assignPlanter(game, strategy, hunterIndex);
    }

    private static void assignPlanter(Game game, Strategy[] sList, int childIndex)
    {
        if (!(sList[childIndex] instanceof PlanterStrategy))
        {
            sList[childIndex] = new PlanterStrategy();
        }
    }

    private static void assignDefender(Game game, Strategy[] sList, int childIndex, int defendantChildIndex)
    {
        if (!(sList[childIndex] instanceof DefenderStrategy))
        {
            sList[childIndex] = new DefenderStrategy(game.cList[defendantChildIndex]);
        }
    }

    private static void assignHunter(Game game, Strategy[] sList, int childIndex)
    {
        if (!(sList[childIndex] instanceof HunterStrategy))
        {
            sList[childIndex] = new HunterStrategy();
        }
    }

    private static Strategy[] getInitialFieldStrategy(Game game)
    {
        Strategy[] strategy = new Strategy[Game.CCOUNT];

        assignPlanter(game, strategy, 0);
        assignPlanter(game, strategy, 1);
        assignHunter(game, strategy, 2);
        assignHunter(game, strategy, 3);

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

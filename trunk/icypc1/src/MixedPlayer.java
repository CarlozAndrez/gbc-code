// A simple player that just tries to plant snowmen in interesting
// places.
//
// Feel free to use this as a starting point for your own player.
//
// ICPC Challenge
// Sturgill, Baylor University

import java.util.Scanner;

public class MixedPlayer
{
	public static void main(String[] args)
	{	
		Game game = new Game();
		
		Strategy[] strategy = new Strategy[Game.CCOUNT];
		for (int nchild = 0; nchild < Game.CCOUNT; nchild++)
		{
			if ((nchild % 2) == 0) strategy[nchild] = new PlanterStrategy();
			else strategy[nchild] = new HunterStrategy();
		}

		// Scanner to parse input from the game engine.
		Scanner in = new Scanner(System.in);

		// Keep reading states until the game ends.
		while (game.readGameData(in))
		{
			// Decide what each child should do
			for (int nchild = 0; nchild < Game.CCOUNT; nchild++)
			{
				Move move = strategy[nchild].chooseNextAction(game, game.cList[nchild]);
				move.writeAction(System.out);
			}
		}
	}
}

// A simple player that just tries to plant snowmen in interesting
// places.
//
// Feel free to use this as a starting point for your own player.
//
// ICPC Challenge
// Sturgill, Baylor University

import java.util.Scanner;

public class PlanterPlayer
{
	public static void main(String[] args)
	{
		Game game = new Game();
		
		Strategy[] strategy = new Strategy[Game.CCOUNT];
		for (int nchild = 0; nchild < Game.CCOUNT; nchild++)
			strategy[nchild] = new PlanterStrategy();
		
		// Debug
//		strategy[0] = new NoopStrategy();
//		strategy[1] = new NoopStrategy();
		
		// Scanner to parse input from the game engine.
		Scanner in = new Scanner(System.in);

		// Keep reading states until the game ends.
		while (game.readGameData(in))
		{
			// Decide what each child should do
			for (int nchild = 0; nchild < Game.CCOUNT; nchild++)
			{
				Move m = strategy[nchild].chooseNextAction(game, game.cList[nchild]);
				m.writeAction(System.out);
			}
		}
	}
}

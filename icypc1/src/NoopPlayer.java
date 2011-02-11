// A simple player that just tries to hit children on the opponent's
// team with snowballs.
//
// Feel free to use this as a starting point for your own player.
//
// ICPC Challenge
// Sturgill, Baylor University

import java.util.Scanner;

public class NoopPlayer
{
	public static void main(String[] args)
	{
		Game.debug("new game");
		
		Game game = new Game();
		Strategy strategy = new NoopStrategy();
		
		// Scanner to parse input from the game engine.
		Scanner in = new Scanner(System.in);

		// Keep reading states until the game ends.
		while (game.readGameData(in))
		{
			// Decide what each child should do
			for (int nchild = 0; nchild < Game.CCOUNT; nchild++)
			{
				Move m = strategy.chooseNextAction(game, game.cList[nchild]);
				m.writeAction(System.out);
			}
		}
	}
}

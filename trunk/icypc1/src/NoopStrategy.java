
public class NoopStrategy extends Strategy
{

	@Override
	public Move chooseNextAction(Game game, Child child)
	{
		return new Move();
	}

}

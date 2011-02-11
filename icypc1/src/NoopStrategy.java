class NoopStrategy extends Strategy
{

	@Override
	public Move chooseNextAction(Game game, Child child)
	{
		return new Move();
	}

	@Override
	protected double voteOnBeingAPlanter(Game game, Child me)
	{
		// TODO Auto-generated method stub
		return 0;
	}

}

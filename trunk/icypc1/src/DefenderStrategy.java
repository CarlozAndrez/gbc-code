import java.awt.*;
import java.util.Random;


class DefenderStrategy extends HunterStrategy
{
    Child childToDefend;

    public DefenderStrategy(Child childToDefend) {
        this.childToDefend = childToDefend;
    }

	@Override
    public Move chooseNextAction(Game game, Child child)
    {
        child.runTarget = new Point(childToDefend.pos.x + 2, childToDefend.pos.y);
        return super.chooseNextAction(game, child);
    }
}

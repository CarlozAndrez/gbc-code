import java.awt.*;
import java.util.ArrayList;
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
        child.runTarget = getPointBetweenDefendantAndEnemy(game);
        return super.chooseNextAction(game, child);
    }

    private Point getPointBetweenDefendantAndEnemy(Game game) {

        Point defendAgainst = new Point(16, 16);
        ArrayList<Integer> enemyChildren = game.getVisibleEnemyChildren();
        if (enemyChildren.size() > 0) {
            Child enemy = game.cList[game.getNearestEnemyIndex(childToDefend, enemyChildren)];
            defendAgainst = enemy.pos;
        }
        int x = childToDefend.pos.x;
        int y = childToDefend.pos.y;
        if (x < defendAgainst.x)
            x+= 2;
        else if (x > defendAgainst.x)
            x -= 2;
        if (y < defendAgainst.y)
            y+= 2;
        else if (y > defendAgainst.y)
            y -= 2;

        return new Point(x, y);
    }
}

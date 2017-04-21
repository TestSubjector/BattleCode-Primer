package SloBro;
import battlecode.common.*;

public class LumberjackMemory extends RobotMemory
{
	static float attackRange = RobotType.LUMBERJACK.bodyRadius + GameConstants.LUMBERJACK_STRIKE_RADIUS;
	
	
	public boolean isChopping; 
	
	
	public LumberjackMemory(RobotController rc)
	{
		super(rc);
		isChopping = false;
	}
	
	public void updateMemory()
	{
		super.updateMemory();
	}
	
	public MapLocation getImportantLoc()
	{
		return null;
	}
	
	public boolean shouldAttack()
	{
		if(alliesInRange.length == 0 && enemiesInRange.length > 0)
			return true;
		return false;
	}
	
	public boolean shouldChop()
	{
		return trees.length > 0;
	}
	
	public boolean shouldMove() {
		return !isChopping;
	}
	
}

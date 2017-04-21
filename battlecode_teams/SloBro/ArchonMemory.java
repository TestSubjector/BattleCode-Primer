package SloBro;
import battlecode.common.*;

public class ArchonMemory extends RobotMemory
{
	RobotController rc;
	Direction myDir; 
	
	public ArchonMemory(RobotController rc)
	{
		super(rc);
		myDir = Util.randomDirection();
	}
	
	public void updateMemory()
	{
		super.updateMemory();
	}
	
	public MapLocation getImportantLoc()
	{
		return null;
	}
	
	public void updateDir() {
		myDir = Util.randomDirection();
	}
	
}

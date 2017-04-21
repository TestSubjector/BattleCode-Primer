package SloBro;
import battlecode.common.*;

public class TankMemory extends RobotMemory
{
	RobotController rc;
	
	public TankMemory (RobotController rc)
	{
		super(rc);
	}
	
	public void updateMemory()
	{
		super.updateMemory();
	}
	
	public MapLocation getImportantLoc()
	{
		return null;
	}
}

package SloBro;
import battlecode.common.*;

public class ScoutMemory extends RobotMemory
{	
	Direction myDir; 
	
	public ScoutMemory(RobotController rc) 
	{
		super(rc);
		
		try {
			myDir = rc.getLocation().directionTo(TeamComms.getArchonLoc());
		} catch(Exception e) {
			myDir = Util.randomDirection();
		} 
	}
	
	public void updateMemory()
	{
		super.updateMemory();
	}
	
	public MapLocation getImportantLoc()
	{
		return null;
	}
	
	public Direction getMyDirection() {
		return myDir; 
	}
	
	public void setDirection(Direction dir) {
		myDir = dir; 
	}
	
}

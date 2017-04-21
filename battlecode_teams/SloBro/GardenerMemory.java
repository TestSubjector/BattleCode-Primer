package SloBro;
import battlecode.common.*;

public class GardenerMemory extends RobotMemory
{
	Direction dir; 
	int strat;
	TreeInfo tree; 
	
	public GardenerMemory(RobotController rc)
	{
		super(rc);
		strat = 0;
		tree = null; 
	}
	
	public void updateMemory()
	{
		super.updateMemory();
		
	}
	
	public MapLocation getImportantLoc()
	{
		return null;
	}
	
	public void setDirectionToDeploy(Direction direction) {
		this.dir = direction; 
	}
	
	public Direction getDirectionToDeploy() {
		return dir; 
	}
	
	public int getStrat() {
		return this.strat;
	}
	
	public void setStrat(int strat) {
		this.strat = strat; 
	}
	
	public void setWateringTree(TreeInfo tree) {
		this.tree = tree; 
	}
	
	public TreeInfo getWateringTree(int strat) {
		return tree; 
	}
}

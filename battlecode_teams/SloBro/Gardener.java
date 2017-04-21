package SloBro;

import battlecode.common.*;


public class Gardener extends RobotPlayer {
	RobotController rc;
	GardenerMemory mem;
	

	public Gardener(RobotController rc) throws GameActionException
	{
		this.rc = rc; 
		mem = new GardenerMemory(rc);		
		mem.setStrat(3);
	}
	
	// Gardener run method
	public void run() throws GameActionException
	{
		while (true)
		{
            try
            {
	           	logic(mem.getStrat());
	           	Clock.yield();
            } 
            catch (Exception e)
            {
            	System.out.println("Gardener Exception");
	            e.printStackTrace();
	        }
		}
	}
	
	// The Logic for a given turn
	public void logic(int strat) throws GameActionException 
	{
		switch (strat) {
		case 0:
			wallStrat(); 
			break;
		case 1: 
			normalStrat(); 
			break;
		case 2: 
			tryPlantFarm();
			break;
		case 3: 
			deployInitialLumberjack();
			break;
		}
	}
	
	// Depending on gardener count, gardeners make a wall in front of archon 
	public void wallStrat() throws GameActionException
	{
		int numGardeners = TeamComms.getGardeners();
		MapLocation archonLoc = TeamComms.getArchonLoc();
		MapLocation enemyArchonLoc = TeamComms.getOppArchonLoc();
		MapLocation wall = archonLoc.add(archonLoc.directionTo(enemyArchonLoc), 10); 
		
		if (numGardeners == 1) 
		{ 
			createTreeWall(wall, archonLoc.directionTo(enemyArchonLoc));
		}
	}
	
	// The normal strategy
	public void normalStrat() throws GameActionException
	{	
		
		waterTree();
		deployRobotLogic();
	}
	
	public void deployRobotLogic() throws GameActionException {
		// Deploy military units if possible
		if (mem.enemiesInView.length > 0) {
			deployRobot(RobotType.SOLDIER);
		}
				
		if (TeamComms.getScouts() < 10) {
			deployRobot(RobotType.SCOUT);
		} else if (TeamComms.getLumberjacks() < 10) {
			deployRobot(RobotType.LUMBERJACK);
		} else if (TeamComms.getSoldiers() < 20) {
			deployRobot(RobotType.SOLDIER);
		} else if (TeamComms.getTanks() < 3) {
			// deployRobot(RobotType.TANK);
		}
	}
		
	public void tryPlantFarm() throws GameActionException 
	{
		Direction toEnemyArchon = TeamComms.getDirectionToInitialArchonLoc();
		Direction dir = toEnemyArchon != null? toEnemyArchon.rotateRightDegrees(72) : Util.randomDirection();
		TreeInfo tree = findTreeToWater();
		int num = 0; 
		
		System.out.println(toEnemyArchon.toString());
		
		if (foundLand(3))
		{
			while (num < 4)
			{
				if (!dir.equals(toEnemyArchon)) {
					if (plantTree(dir)) {
						num++;
					} 
					
					if (tree != null) {
						waterTree();
					}
				}
				
				dir = dir.rotateRightDegrees(72);
				Clock.yield();
			}
		} 
		else 
		{
			Clock.yield();
			tryPlantFarm(); 
		}
		

		
		mem.setDirectionToDeploy(toEnemyArchon);
		mem.setStrat(1);
	}
	
	public boolean foundLand(int radius) throws GameActionException
	{
		RobotInfo[] team = rc.senseNearbyRobots(radius, rc.getTeam());
		TreeInfo[] trees = rc.senseNearbyTrees(radius, rc.getTeam());
		
		if (team.length == 0 && trees.length == 0)
		{
			return true;
		} 
			
		//if (team.length > 0 && Util.tryMove(rc, rc.getLocation().directionTo(team[0].location).rotateLeftDegrees(180))) {
		//} else if (trees.length > 0 && Util.tryMove(rc, rc.getLocation().directionTo(trees[0].location).rotateLeftDegrees(180))) {
		//}else {
			Util.tryMove(rc, Util.randomDirection());
		//}
		
		return false; 
		
	}
	
	
	// Returns an array of trees in robots sight. 
	public TreeInfo[] findTreesInSight()
	{
		return rc.senseNearbyTrees();
	}
	
	// Water given tree
	public boolean waterTree() throws GameActionException
	{
		TreeInfo tree = findTreeToWater();
		
		// Attemp to water tree
		if (tree != null && rc.canWater(tree.ID)) 
		{
				rc.water(tree.ID);
				return true;
		}
		else
		{
			return false;
		}
	}
	
	// Plants a tree behind the robot
	public boolean plantTree(Direction dir) throws GameActionException
	{
		if (rc.canPlantTree(dir))
		{
			rc.plantTree(dir);
			return true; 
		}
		else
		{
			return false; 
		}
	}
	
	public boolean planTreeOnBullet() throws GameActionException
	{
		BulletInfo[] bullets = RobotPlayer.rc.senseNearbyBullets();
		for(int i = 0; i < bullets.length; i++) 
		{
			if(MilitaryUtil.willHitMe(bullets[i])) 
			{
				plantTree(rc.getLocation().directionTo(bullets[i].location));
				return true;
			}
		}
		return false; 
	}	
	
	// Shakes the given tree
	public boolean shakeTree(TreeInfo tree) throws GameActionException
	{
		// Attempt to shake given tree
		if (rc.canShake(tree.ID))
		{
				rc.shake(tree.ID);
				System.out.println("Shook tree");
				return true; 
		}
		else 
		{
			return false; 
		}
	}
	
	TreeInfo findTreeToWater()
	{
		TreeInfo[] treeInfo = findTreesInSight();
		
		if (treeInfo.length == 0) 
		{
			return null; 
		} 
		
		TreeInfo lowestInWater = treeInfo[0];
		
		for (int i = 0; i < treeInfo.length; i++) 
		{
			if (treeInfo[i].health < lowestInWater.health) 
			{
				lowestInWater = treeInfo[i];
			}
		}
		
		return lowestInWater; 
				
		
	} 
	
	// Finds lowest tree to shake
	TreeInfo findTreeToShake() 
	{
		TreeInfo[] treeInfo = findTreesInSight();
		
		if (treeInfo.length == 0) 
		{
			return null; 
		} 
		
		TreeInfo highestInBullets = treeInfo[0];
		
		for (int i = 0; i < treeInfo.length; i++)
		{
			if (treeInfo[i].getContainedBullets() > highestInBullets.getContainedBullets())
			{
				highestInBullets = treeInfo[i];
			}
		}
		
		return highestInBullets; 
				
		
	}
	
	// Create a tree wall 
	public void createTreeWall(MapLocation location, Direction dirToEnemy) throws GameActionException
	{
		boolean working = true; 
		boolean moveRight = false; 
		boolean createWall = false; 
		
		int numTrees = 0;
		int moves = 0;
		
		MapLocation sideOne = location.add(dirToEnemy.rotateLeftDegrees(90), 5);
		Direction dirBehind = dirToEnemy.rotateLeftDegrees(90);

		while (working)
		{
			
			if (createWall) 
			{
				if (rc.canPlantTree(dirBehind)) 
				{
					rc.plantTree(dirBehind);
					numTrees++;
					moves = 0; 
				}
				else if (rc.canMove(dirBehind.rotateLeftDegrees(180)) && moves < 3)
				{
					rc.move(dirBehind.rotateLeftDegrees(180));
					moves++;
				} 
				
				if (numTrees > 5)
				{
					working = false; 
				}
				
			} 
			else if (moveRight)
			{
				if (rc.getLocation().equals(sideOne)) 
				{
					createWall = true; 
				}
				else if (rc.canMove(sideOne))
				{
					rc.move(sideOne);
				}
				
			} 
			else 
			{
				if (rc.getLocation().equals(location))
				{
					moveRight = true; 
				} 
				else if (rc.canMove(location)) 
				{
					rc.move(location);
				}	
			}	
			
			Clock.yield();
		}
	}
	
	public boolean deployInitialLumberjack() throws GameActionException 
	{
		int tries = 0;
		
		while (tries++ < 5) {
			Direction dir = Util.randomDirection();
		
			if (rc.canBuildRobot(RobotType.LUMBERJACK, dir)) 
			{
				rc.buildRobot(RobotType.LUMBERJACK, dir);
				incrementCount(RobotType.LUMBERJACK);
				mem.setStrat(2);
				return true; 
			} 
		}
		mem.setStrat(2);
		return false; 
	}
	
	// Deploys a robot given 
	public boolean deployRobot(RobotType type)
	{
		Direction dir = mem.getDirectionToDeploy();
		if (dir == null)
		{
			dir = Util.randomDirection();
		}
		
		if (rc.canBuildRobot(type, dir))
		{
			try
			{
				rc.buildRobot(type, dir);
				incrementCount(type);
				return true; 
			} 
			catch (GameActionException e)
			{
				// ERROR: deployment failed
				System.out.println("ERROR: buildRobot failed!");
				e.printStackTrace();
			}
		}
		else 
		{
		   // Robot cannot build this robot
		   System.out.println("I cannot deploy this robot! - Gardener");
		}
		
		return false; 
	}
}

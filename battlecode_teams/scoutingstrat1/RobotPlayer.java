package scoutingstrat1;
import battlecode.common.*;


/*
 * Work by Ryan Zhao, Bryce Miles, Alex Wee, and Cole Stevens
 * Battlecode 2017
 * 
 * Team xX_EdgeMasters_Xx
 */

public strictfp class RobotPlayer {
static RobotController rc;
    
    
    /*
     * BROADCAST ARRAY KEY
     * 
     * 999 - is there a target (0=no, 1=yes)
     * 1000 - integer part of X-coordinate of target enemy
     * 1001 - decimal part of X-coordinate of target enemy
     * 1002 - integer part of Y-coordinate of target enemy
     * 1003 - decimal part of Y-coordinate of target enemy
     * 1004 - priority of enemy targeted (1 = Archon, 2 = Gardener, 3 = Soldier, 4 = ...   , 1048575 = No Robot Targeted)
     * 1005 - turn number that robot was spotted
     * 1006 - bool: was robot spotted this round (0=no, 1=yes)
     */
    
    static int IS_TARGET_CHANNEL = 999;
    
    /**
     * integer part of x
     */
    static int TARGET_X_INT_CHANNEL = 1000;
    
    /**
     * decimal part of x * 10^6 and rounded
     */
    static int TARGET_X_DECI_CHANNEL = 1001;
    
    /**
     * integer part of y
     */
    static int TARGET_Y_INT_CHANNEL = 1002;
    
    /**
     * decimal part of y * 10^6 and rounded
     */
    static int TARGET_Y_DECI_CHANNEL = 1003;
    static int TARGET_PRIORITY_CHANNEL = 1004;
    static int TARGET_TURN_NUMBER_CHANNEL = 1005;
    static int WAS_ROBOT_SPOTTED_CHANNEL = 1006;
    
    //Priority options
    static int ARCHON_PRIORITY = 1;
    static int GARDENER_PRIORITY = 2;
    static int SOLDIER_PRIORITY = 3;
    static int MISC_PRIORITY = 20;
    static int NO_ROBOT_TARGETED_PRIORITY = 1048575;
    
    //has circle sizes to make sure gardeners do not plant against walls only
    static float GARDENER_CIRCLE_SIZE = 1.0f + 2.0f + 2.0f;
    

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
    **/
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;

        // Here, we've separated the controls into a different method for each RobotType.
        // You can add the missing ones or rewrite this into your own control structure.
        switch (rc.getType()) {
            case ARCHON:
                runArchon();
                break;
            case GARDENER:
                runGardener();
                break;
            case SOLDIER:
                runSoldier();
                break;
            case LUMBERJACK:
                runLumberjack();
                break;
            case SCOUT:
                runScout();
                break;
            case TANK:
                runTank();
                break;
        }
	}

    static void runArchon() throws GameActionException {
    	
    	System.out.println(RobotType.ARCHON.sensorRadius);
    	
    	MapLocation toSpawnGardnerLoc;
    	boolean offMapNorth = false;
    	boolean offMapEast = false;
    	boolean offMapWest = false;
    	boolean offMapSouth = false;
    	MapLocation toMoveLocation;
    	int moveAwayCounter = 0;
    	MapLocation averagedRunLocation;
    	
    	rc.broadcast(IS_TARGET_CHANNEL, 0);
    	rc.broadcast(TARGET_X_INT_CHANNEL, -1);
    	rc.broadcast(TARGET_X_DECI_CHANNEL, -1);
    	rc.broadcast(TARGET_Y_INT_CHANNEL, -1);
    	rc.broadcast(TARGET_Y_DECI_CHANNEL, -1);
    	rc.broadcast(TARGET_PRIORITY_CHANNEL, NO_ROBOT_TARGETED_PRIORITY);
    	rc.broadcast(TARGET_TURN_NUMBER_CHANNEL, -1000);
    	rc.broadcast(WAS_ROBOT_SPOTTED_CHANNEL, 0);
    	
    	
        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	
            	MapLocation target = readTargetMapLocation();
            	rc.setIndicatorDot(target, 255, 0, 0);

            	
            	//if no robot was found the previous turn, reset targeting
            	if (rc.getRoundNum() > rc.readBroadcast(TARGET_TURN_NUMBER_CHANNEL)+10 && rc.readBroadcast(WAS_ROBOT_SPOTTED_CHANNEL) == 0){
            		rc.broadcast(IS_TARGET_CHANNEL, 0);
            		rc.broadcast(TARGET_X_INT_CHANNEL, -1);
                	rc.broadcast(TARGET_X_DECI_CHANNEL, -1);
                	rc.broadcast(TARGET_Y_INT_CHANNEL, -1);
                	rc.broadcast(TARGET_Y_DECI_CHANNEL, -1);
                	rc.broadcast(TARGET_PRIORITY_CHANNEL, NO_ROBOT_TARGETED_PRIORITY);
                	System.out.println("NO ROBOT SPOTTED THIS TURN, RESETTING");
            	}
            	
            	toSpawnGardnerLoc = rc.getLocation().add(Direction.getWest(), 3);
            	rc.setIndicatorDot(toSpawnGardnerLoc, 255, 255, 255);
            	toMoveLocation = rc.getLocation();
            	
            	if (rc.getRoundNum() < 200){
            		if ( rc.canHireGardener(Direction.getWest()) ){
            			rc.hireGardener(Direction.getWest());
            			moveAwayCounter = 20;
            		}
            		if (moveAwayCounter > 0){
                		tryMove(Direction.getEast(), 20, 6);
                		moveAwayCounter--;
                	}
            	}
            	
            	
            	
            	if (rc.onTheMap(toSpawnGardnerLoc, 3)){
            		if (rc.senseNearbyTrees(toSpawnGardnerLoc, 3, Team.NEUTRAL).length == 0 && 
            										rc.senseNearbyTrees(toSpawnGardnerLoc, 3, rc.getTeam()).length == 0 &&
            										rc.senseNearbyRobots(toSpawnGardnerLoc, 3, rc.getTeam()).length == 0){
            			if ( rc.canHireGardener(Direction.getWest()) ){
                			rc.hireGardener(Direction.getWest());
                		}
            		}
            		
            		offMapNorth = false;
            		offMapEast = false;
            		offMapWest = false;
            		offMapSouth = false;
            	}
            	else{
            		if (!rc.onTheMap(toSpawnGardnerLoc.add(Direction.getNorth(), 3))){
            			offMapNorth = true;
            		}
            		if (!rc.onTheMap(toSpawnGardnerLoc.add(Direction.getEast(), 3))){
            			offMapEast = true;
            		}
            		if (!rc.onTheMap(toSpawnGardnerLoc.add(Direction.getSouth(), 3))){
            			offMapSouth = true;
            		}
            		if (!rc.onTheMap(toSpawnGardnerLoc.add(Direction.getWest(), 3))){
            			offMapWest = true;
            		}
            		
            	}
            	
            	if (offMapNorth || offMapEast || offMapWest || offMapSouth){

            		if (offMapNorth){
            			toMoveLocation = toMoveLocation.add(Direction.getSouth());
            		}
            		if (offMapEast){
            			toMoveLocation = toMoveLocation.add(Direction.getWest());
            		}
            		if (offMapWest){
            			toMoveLocation = toMoveLocation.add(Direction.getEast());
            		}
            		if (offMapSouth){
            			toMoveLocation = toMoveLocation.add(Direction.getNorth());
            		}
            		if (rc.getLocation().directionTo(toMoveLocation) != null){
            			if (!rc.hasMoved()){
            				tryMove(rc.getLocation().directionTo(toMoveLocation));
            			}
            			
            		}else{
            			if (!rc.hasMoved()){
            				wander();
            			}
            		}
            	}
            	else{
            		if (!rc.hasMoved()){
            			//runaway block
                    	averagedRunLocation = rc.getLocation();
                    	RobotInfo[] nearbyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
                    	for (RobotInfo r : nearbyRobots){
                    		averagedRunLocation = averagedRunLocation.subtract(rc.getLocation().directionTo(r.getLocation()));
                    	}
                    	rc.setIndicatorDot(averagedRunLocation, 0, 0, 255);
                    	if (!averagedRunLocation.equals(rc.getLocation())){				//if there was any modification to run location
                    		tryMove(rc.getLocation().directionTo(averagedRunLocation));
                    	}else{
                    		wander();
                    	}
        			}
            	}

   
            	
            	if (rc.getTeamBullets() > 500){
            		rc.donate(500);
            	}
            	
            	
            	rc.broadcast(WAS_ROBOT_SPOTTED_CHANNEL, 0);			//resets counter for "was robot spotted this turn"
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
        }
    }

	static void runGardener() throws GameActionException {
    	int directionToPlant = 0;
    	Direction directionToBuild = Direction.getEast();								//default build direction
    	boolean hasBuildDirection = false;
    	//can we detect borders, if so should we check to face a border
    	//what other conditions should we consider for placement
    	//potentially towards the location of our archon, so we can build a wall as a defensive strat
    	
    	Direction randomDir = Direction.getEast();
    	int buildcounter = 0;

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	if (rc.onTheMap(rc.getLocation(), GARDENER_CIRCLE_SIZE))
            	{
            		if (hasBuildDirection == false){
                		for(int i = 0; i < 6; i++)
                    	{
                    		if(rc.canPlantTree( new Direction( (float)Math.PI*(i)/3) ) && rc.canBuildRobot(RobotType.LUMBERJACK, new Direction( (float)Math.PI*(i)/3) ))
                    		{
                    			directionToBuild = new Direction( (float)Math.PI*(i)/3);
                    			hasBuildDirection = true;
                    			break;
                    		}
                    	}
                	}
            	}
            	else 
            	{
            		if (!tryMove(randomDir)){
                        randomDir = randomDirection();
                    }
            	}
            	
            	if(rc.getRoundNum() < 300 && rc.getRoundNum() > 100){
            		
            		if (  rc.canBuildRobot(RobotType.LUMBERJACK, directionToBuild)  ){
            			rc.buildRobot(RobotType.LUMBERJACK, directionToBuild);
            		}
            		
            	}
            	else if (rc.getRoundNum() < 50){
            		if (  rc.canBuildRobot(RobotType.SCOUT, directionToBuild)  ){
            			rc.buildRobot(RobotType.SCOUT, directionToBuild);
            		}
            	}
            	else if (rc.getRoundNum() < 500){
            		if (  rc.canBuildRobot(RobotType.SOLDIER, directionToBuild)  ){
            			rc.buildRobot(RobotType.SOLDIER, directionToBuild);
            		}
            	}
            	else{
            		System.out.println(rc.getRoundNum() % 8);
            		System.out.println(buildcounter);
            		System.out.println(rc.canBuildRobot(RobotType.TANK, directionToBuild));
            		if (buildcounter % 8 == 0){
            			if (  rc.canBuildRobot(RobotType.SCOUT, directionToBuild) && rc.getTeamBullets() > 130 ){
                			rc.buildRobot(RobotType.SCOUT, directionToBuild);
                			buildcounter++;
                		}
            			else{
            				buildcounter++;
            			}
            		}
            		else if (buildcounter % 8 == 1){
            			if (  rc.canBuildRobot(RobotType.TANK, directionToBuild)  ){
                			rc.buildRobot(RobotType.TANK, directionToBuild);
                			buildcounter++;
                		}
            			else{
            				buildcounter++;
            			}
            		}
            		else{
            			if (  rc.canBuildRobot(RobotType.SOLDIER, directionToBuild)  ){
                			rc.buildRobot(RobotType.SOLDIER, directionToBuild);
                			buildcounter++;
                		}
            			else{
            				buildcounter++;
            			}
            		}
            	}

            	//attempts to water nearby tree
            	TreeInfo[] nearbyTrees = rc.senseNearbyTrees(2, rc.getTeam());
            	for (TreeInfo t : nearbyTrees){
           			if (  rc.canWater(t.getID()) && t.getHealth() < t.getMaxHealth()*0.9 ){
           				rc.water(t.getID());

           				break;
            		}

            	}
            	
            	if(rc.hasTreeBuildRequirements()){
	            	if (rc.canPlantTree( new Direction( (float)Math.PI*(directionToPlant)/3)) && !(new Direction((float)Math.PI*(directionToPlant)/3).equals(directionToBuild)) ){
	            		rc.plantTree(new Direction( (float)Math.PI*(directionToPlant)/3));
	            	}else{
	            	}
            	}
            	directionToPlant = (directionToPlant+1)%6;
            	
            	if (rc.getTeamBullets() > 500){
            		rc.donate(500);
            	}
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
        }
    }

    static void runSoldier() throws GameActionException {												//copy paste of scout strat

    	Direction randomDir = randomDirection();
    	int targetpriority;
    	MapLocation averagedRunLocation;

    	
        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	

            	
            	
            	RobotInfo[] nearbyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
            	
            	//scouting bot
            	for (RobotInfo r : nearbyRobots){
            		if (r.getType() == RobotType.ARCHON){
            			targetpriority = ARCHON_PRIORITY;
            		}
            		else if (r.getType() == RobotType.GARDENER){
            			targetpriority = GARDENER_PRIORITY;
            		}
            		else if (r.getType() == RobotType.SOLDIER){
            			targetpriority = SOLDIER_PRIORITY;
            		}
            		else{
            			targetpriority = MISC_PRIORITY;
            		}
            		
            		if (targetpriority < rc.readBroadcast(TARGET_PRIORITY_CHANNEL)){
            			rc.broadcast(TARGET_PRIORITY_CHANNEL, targetpriority);
            			rc.broadcast(TARGET_TURN_NUMBER_CHANNEL, rc.getRoundNum());
            			rc.broadcast(WAS_ROBOT_SPOTTED_CHANNEL, 1);
            			rc.broadcast(IS_TARGET_CHANNEL, 1);
            			broadcastTargetLocation(r.getLocation());
            			break;
            		}
            	}
            	
            	RobotInfo[] nearbyFriendlies = rc.senseNearbyRobots(-1, rc.getTeam());
            	MapLocation enemyLocation;
            	boolean toShoot = true;
            	for (RobotInfo r : nearbyRobots){
            		toShoot = true;
            		enemyLocation = r.getLocation();
            		
            		for (RobotInfo f : nearbyFriendlies){            			
            			if ( willShoot(rc.getLocation().directionTo(enemyLocation), f) ){
            				toShoot = false;
            			}
            		}
            		
            		if (toShoot){
            			rc.fireSingleShot( rc.getLocation().directionTo(enemyLocation) );
                		break;
            		}
            		
            	}
            	
            	
            	
            	//movement block
            	averagedRunLocation = rc.getLocation();
            	for (RobotInfo r : nearbyRobots){
            		averagedRunLocation = averagedRunLocation.subtract(rc.getLocation().directionTo(r.getLocation()));
            	}
            	rc.setIndicatorDot(averagedRunLocation, 0, 0, 255);
            	if (!averagedRunLocation.equals(rc.getLocation())){				//if there was any modification to run location
            		tryMove(rc.getLocation().directionTo(averagedRunLocation));
            	}
            	if (!rc.hasMoved()){
	            	if (rc.readBroadcast(IS_TARGET_CHANNEL) == 0){
	            		if (!tryMove(randomDir)){
	            			randomDir = randomDirection();
	            		}
	            	}
	            	else{
	            		tryMove(rc.getLocation().directionTo(readTargetMapLocation()));
	            	}
            	}
               
            	if (rc.getTeamBullets() > 500){
            		rc.donate(500);
            	}
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
        }
    }

    static void runLumberjack() throws GameActionException {

    	TreeInfo[] toCut;
    	RobotInfo[] nearbyRobots;

    	Direction randomMovingDirection = randomDirection();
    	
    	boolean shouldMove = true;
    	
        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	shouldMove = true;
            	
            	//attack code
           		nearbyRobots = rc.senseNearbyRobots();
           		
           		for (RobotInfo r : nearbyRobots){
           			if (r.getTeam() != rc.getTeam()){
               			if (rc.getLocation().isWithinDistance(r.getLocation(), GameConstants.LUMBERJACK_STRIKE_RADIUS+2)){
               				System.out.println("Within distance");
               				if (rc.canStrike()){
               					rc.strike();

               					System.out.println("Attacked");
               					break;
               				}
               			}
           			}
           		}
            	
            	//cut tree code
            	toCut = rc.senseNearbyTrees();
            	
            	if (toCut.length > 0){
	            	for (TreeInfo t : toCut){
	            		if (t.getTeam() != rc.getTeam()){
	            			if (rc.canShake(t.getID())){
	            				rc.shake(t.getID());
	            			}
	            			if (rc.canChop(t.getID())){
	                			rc.chop(t.getID());
	                			shouldMove = false;
	                			break;
	                		}
	            			//else{
	                			//tryMove(rc.getLocation().directionTo(t.getLocation()));
	                			//break;
	                		//}
	            		}
	            	}
            	}
            	
            	//random movement code (if not cutting a tree
           		if (shouldMove && !rc.hasMoved()){
    	           	if (rc.readBroadcast(IS_TARGET_CHANNEL) == 0){
    	           		if (!tryMove(randomMovingDirection)){
    	           			randomMovingDirection = randomDirection();
    	           		}
    	           	}
    	           	else{
    	           		tryMove(rc.getLocation().directionTo(readTargetMapLocation()));
    	           	}
          
           		}


           		if (rc.getTeamBullets() > 500){
            		rc.donate(500);
            	}
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
        }
    }
    
    static void runScout() throws GameActionException {

    	Direction randomDir = randomDirection();
    	int targetpriority;
    	MapLocation averagedRunLocation;

    	
        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	

            	
            	
            	RobotInfo[] nearbyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
            	
            	//scouting bot
            	for (RobotInfo r : nearbyRobots){
            		if (r.getType() == RobotType.ARCHON){
            			targetpriority = ARCHON_PRIORITY;
            		}
            		else if (r.getType() == RobotType.GARDENER){
            			targetpriority = GARDENER_PRIORITY;
            		}
            		else if (r.getType() == RobotType.SOLDIER){
            			targetpriority = SOLDIER_PRIORITY;
            		}
            		else{
            			targetpriority = MISC_PRIORITY;
            		}
            		
            		if (targetpriority < rc.readBroadcast(TARGET_PRIORITY_CHANNEL)){
            			rc.broadcast(TARGET_PRIORITY_CHANNEL, targetpriority);
            			rc.broadcast(TARGET_TURN_NUMBER_CHANNEL, rc.getRoundNum());
            			rc.broadcast(WAS_ROBOT_SPOTTED_CHANNEL, 1);
            			rc.broadcast(IS_TARGET_CHANNEL, 1);
            			broadcastTargetLocation(r.getLocation());
            			break;
            		}
            	}
            	
            	/*
            	for (RobotInfo r : nearbyRobots){
            		rc.fireSingleShot( rc.getLocation().directionTo(r.getLocation()) );
            		break;
            	}
            	*/
            	RobotInfo[] nearbyFriendlies = rc.senseNearbyRobots(-1, rc.getTeam());
            	MapLocation enemyLocation;
            	boolean toShoot = true;
            	for (RobotInfo r : nearbyRobots){
            		toShoot = true;
            		enemyLocation = r.getLocation();
            		
            		for (RobotInfo f : nearbyFriendlies){            			
            			if ( willShoot(rc.getLocation().directionTo(enemyLocation), f) ){
            				toShoot = false;
            			}
            		}
            		
            		if (toShoot){
            			rc.fireSingleShot( rc.getLocation().directionTo(enemyLocation) );
                		break;
            		}
            		
            	}
            	
            	
            	
            	
            	if (!rc.hasMoved()){
            		TreeInfo[] nearbyTrees = rc.senseNearbyTrees(-1, Team.NEUTRAL);
            		for (TreeInfo t : nearbyTrees){
            			if (t.containedBullets > 0){
            				if (rc.canShake(t.getID())){
            					rc.shake(t.getID());
            					break;
            				}
            				else{
            					tryMove(rc.getLocation().directionTo(t.getLocation()));
            					break;
            				}
            			}
            		}
            	}
            	
            	
            	if (!rc.hasMoved()){
                	//movement block
                	averagedRunLocation = rc.getLocation();
                	for (RobotInfo r : nearbyRobots){
                		averagedRunLocation = averagedRunLocation.subtract(rc.getLocation().directionTo(r.getLocation()));
                	}
                	rc.setIndicatorDot(averagedRunLocation, 0, 0, 255);
                	if (!averagedRunLocation.equals(rc.getLocation())){				//if there was any modification to run location
                		tryMove(rc.getLocation().directionTo(averagedRunLocation));
                	}
            	}
            	

            	if (!rc.hasMoved()){
	            	if (rc.readBroadcast(IS_TARGET_CHANNEL) == 0){
	            		if (!tryMove(randomDir)){
	            			randomDir = randomDirection();
	            		}
	            	}
	            	else{
	            		tryMove(rc.getLocation().directionTo(readTargetMapLocation()));
	            	}
            	}
               
            	
            	if (rc.getTeamBullets() > 500){
            		rc.donate(500);
            	}
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
        }
    }
    
    static void runTank() throws GameActionException {

    	Direction randomDir = randomDirection();
    	int targetpriority;
    	MapLocation averagedRunLocation;

    	
        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	

            	
            	
            	RobotInfo[] nearbyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
            	
            	//scouting bot
            	for (RobotInfo r : nearbyRobots){
            		if (r.getType() == RobotType.ARCHON){
            			targetpriority = ARCHON_PRIORITY;
            		}
            		else if (r.getType() == RobotType.GARDENER){
            			targetpriority = GARDENER_PRIORITY;
            		}
            		else if (r.getType() == RobotType.SOLDIER){
            			targetpriority = SOLDIER_PRIORITY;
            		}
            		else{
            			targetpriority = MISC_PRIORITY;
            		}
            		
            		if (targetpriority < rc.readBroadcast(TARGET_PRIORITY_CHANNEL)){
            			rc.broadcast(TARGET_PRIORITY_CHANNEL, targetpriority);
            			rc.broadcast(TARGET_TURN_NUMBER_CHANNEL, rc.getRoundNum());
            			rc.broadcast(WAS_ROBOT_SPOTTED_CHANNEL, 1);
            			rc.broadcast(IS_TARGET_CHANNEL, 1);
            			broadcastTargetLocation(r.getLocation());
            			break;
            		}
            	}
            	
            	RobotInfo[] nearbyFriendlies = rc.senseNearbyRobots(-1, rc.getTeam());
            	MapLocation enemyLocation;
            	boolean toShoot = true;
            	for (RobotInfo r : nearbyRobots){
            		toShoot = true;
            		enemyLocation = r.getLocation();
            		
            		for (RobotInfo f : nearbyFriendlies){            			
            			if ( willShoot(rc.getLocation().directionTo(enemyLocation), f) ){
            				toShoot = false;
            			}
            		}
            		
            		if (toShoot){
            			rc.fireSingleShot( rc.getLocation().directionTo(enemyLocation) );
                		break;
            		}
            		
            	}
            	
            	
            	
            	//movement block
            	averagedRunLocation = rc.getLocation();
            	for (RobotInfo r : nearbyRobots){
            		averagedRunLocation = averagedRunLocation.subtract(rc.getLocation().directionTo(r.getLocation()));
            	}
            	rc.setIndicatorDot(averagedRunLocation, 0, 0, 255);
            	if (!averagedRunLocation.equals(rc.getLocation())){				//if there was any modification to run location
            		tryMove(rc.getLocation().directionTo(averagedRunLocation));
            	}
            	if (!rc.hasMoved()){
	            	if (rc.readBroadcast(IS_TARGET_CHANNEL) == 0){
	            		if (!tryMove(randomDir)){
	            			randomDir = randomDirection();
	            		}
	            	}
	            	else{
	            		tryMove(rc.getLocation().directionTo(readTargetMapLocation()));
	            	}
            	}
               
            	if (rc.getTeamBullets() > 500){
            		rc.donate(500);
            	}
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
        }
    }
    
    
    
//-----------------------------------------------------------------------------------------------------------------

    /**
     * Returns a random Direction
     * @return a random Direction
     */
    static Direction randomDirection() {
        return new Direction((float)Math.random() * 2 * (float)Math.PI);
    }

    /**
     * Attempts to move in a given direction, while avoiding small obstacles directly in the path.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException {
        return tryMove(dir,20,3);
    }

    /**
     * Attempts to move in a given direction, while avoiding small obstacles direction in the path.
     *
     * @param dir The intended direction of movement
     * @param degreeOffset Spacing between checked directions (degrees)
     * @param checksPerSide Number of extra directions checked on each side, if intended direction was unavailable
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {
    	
    	
        // First, try intended direction
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }

        // Now try a bunch of similar angles
        boolean moved = false;
        int currentCheck = 1;

        while(currentCheck<=checksPerSide) {
            // Try the offset of the left side
            if(rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateLeftDegrees(degreeOffset*currentCheck));
                return true;
            }
            // Try the offset on the right side
            if(rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateRightDegrees(degreeOffset*currentCheck));
                return true;
            }
            // No move performed, try slightly further
            currentCheck++;
        }

        // A move never happened, so return false.
        return false;
    }

    /**
     * A slightly more complicated example function, this returns true if the given bullet is on a collision
     * course with the current robot. Doesn't take into account objects between the bullet and this robot.
     *
     * @param bullet The bullet in question
     * @return True if the line of the bullet's path intersects with this robot's current position.
     */
    static boolean willCollideWithMe(BulletInfo bullet) {
        MapLocation myLocation = rc.getLocation();

        // Get relevant bullet information
        Direction propagationDirection = bullet.dir;
        MapLocation bulletLocation = bullet.location;

        // Calculate bullet relations to this robot
        Direction directionToRobot = bulletLocation.directionTo(myLocation);
        float distToRobot = bulletLocation.distanceTo(myLocation);
        float theta = propagationDirection.radiansBetween(directionToRobot);

        // If theta > 90 degrees, then the bullet is traveling away from us and we can break early
        if (Math.abs(theta) > Math.PI/2) {
            return false;
        }

        // distToRobot is our hypotenuse, theta is our angle, and we want to know this length of the opposite leg.
        // This is the distance of a line that goes from myLocation and intersects perpendicularly with propagationDirection.
        // This corresponds to the smallest radius circle centered at our location that would intersect with the
        // line that is the path of the bullet.
        float perpendicularDist = (float)Math.abs(distToRobot * Math.sin(theta)); // soh cah toa :)

        return (perpendicularDist <= rc.getType().bodyRadius);
    }
    
    static void wander() throws GameActionException{
    	Direction movingDir = randomDirection();
    	if (rc.canMove(movingDir) && !rc.hasMoved()){
    		rc.move(movingDir);
    	}
    }
    
   
    static void broadcastTargetLocation(MapLocation map) throws GameActionException{
    	int xcoordInt = (int) map.x;
    	int xcoordDeci = Math.round( (map.x%1) * (int)(Math.pow(10, 6)) );
    	int ycoordInt = (int) map.y;
    	int ycoordDeci = Math.round( (map.y%1) * (int)(Math.pow(10, 6)) );
    	
    	rc.broadcast(TARGET_X_INT_CHANNEL, xcoordInt);
    	rc.broadcast(TARGET_X_DECI_CHANNEL, xcoordDeci);
    	rc.broadcast(TARGET_Y_INT_CHANNEL, ycoordInt);
    	rc.broadcast(TARGET_X_DECI_CHANNEL, ycoordDeci);
    }
    
    static MapLocation readTargetMapLocation() throws GameActionException{
    	int xcoordInt = rc.readBroadcast(TARGET_X_INT_CHANNEL);
    	int xcoordDeci = rc.readBroadcast(TARGET_X_DECI_CHANNEL);
    	int ycoordInt = rc.readBroadcast(TARGET_Y_INT_CHANNEL);
    	int ycoordDeci = rc.readBroadcast(TARGET_Y_DECI_CHANNEL);
    	
    	float xcoord = (float)(xcoordInt + xcoordDeci/Math.pow(10,6));
        float ycoord = (float)(ycoordInt + ycoordDeci/Math.pow(10,6));
        return(new MapLocation(xcoord,ycoord));
    	
    	
    }
    
    static boolean willShoot(Direction dir, RobotInfo targetRobot){
    	MapLocation myLocation = rc.getLocation();
    	MapLocation targetLocation = targetRobot.getLocation();
    	float distanceToTarget = myLocation.distanceTo(targetLocation);
    	float theta = dir.radiansBetween(myLocation.directionTo(targetLocation));
    	
    	float oppositeSide = (float)Math.abs(distanceToTarget * Math.sin(theta));
    	
    	return (oppositeSide <= targetRobot.getType().bodyRadius);
    }
}

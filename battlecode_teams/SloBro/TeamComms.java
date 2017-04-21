package SloBro;
import battlecode.common.*;

// Maximum channels: 1000

public class TeamComms {

	// Friendly Team Information
	// Friendly Team Archon Location: Channels 0 and 1
	private static final int archonX = 0;
	private static final int archonY = 1;
	
	// Friendly Team Unit Counts: Channels 2 - 6
	private static final int numGardeners = 2;
	private static final int numSoldiers = 3;
	private static final int numLumberjacks = 4;
	private static final int numScouts = 5;
	private static final int numTanks = 6;
	
	// DonationCount: Channel 50
	private static final int donationCount = 50;  
	
	// Enemy Team Information: Starting at Channel 500
	private static final int numEnemyArchons = 500; 
	private static final int archonID1 = 500; 
	private static final int archonID2 = 501; 
	private static final int archonID3 = 502; 
	private static final int oppArchonX1 = 503;
	private static final int oppArchonY1 = 504;
	private static final int oppArchonX2 = 505;
	private static final int oppArchonY2 = 506;
	private static final int oppArchonX3 = 507;
	private static final int oppArchonY3 = 508;
	private static final int archonSightingTimestamp = 509;
	
	// Prototype for marking Locations to Explore/Secure
	// Note: in general, coords for aoe[i] are on channel (2000 + 2i) and (2000 + 2i + 1)
	public static final int areaOfInterestCount = 1999;
	public static final int areaOfInterestX = 2000;
	public static final int areaOfInterestY = 2001;
	
	// Military Informatin: Starting at Channel 4000
	public static final int areaofInterstMilCount = 3999;
	public static final int areaOfInterestMilitaryX = 4000; 	
	public static final int areaOfInterestMilitaryY = 4001;
	
	// Communication Functions
	// Friendly Archon Location

	public static MapLocation getArchonLoc() throws GameActionException
	{
		RobotController rc = RobotPlayer.rc;
		int x = rc.readBroadcast(archonX);
		int y = rc.readBroadcast(archonY);
		return new MapLocation(x,y);
	}
	
	public static void broadcastArchonLoc() throws GameActionException
	{
		RobotController rc = RobotPlayer.rc;
		if(rc.getType() == RobotType.ARCHON)
		{
			MapLocation l = rc.getLocation();
			rc.broadcast(archonX, (int)l.x);
			rc.broadcast(archonY, (int) l.y);
		}
	}
	
	
	// Friendly Team Counts
	// Gardener Counts
	public static int getGardeners() throws GameActionException
	{
		RobotController rc = RobotPlayer.rc;
		return rc.readBroadcast(numGardeners);
	}
	
	public static void updateGardeners(int n) throws GameActionException
	{
		RobotController rc = RobotPlayer.rc;
		rc.broadcast(numGardeners, n);
	}
	
	// Soldier Counts
	public static int getSoldiers() throws GameActionException
	{
		RobotController rc = RobotPlayer.rc;
		return rc.readBroadcast(numSoldiers);
	}
	
	public static void updateSoldiers(int n) throws GameActionException
	{
		RobotController rc = RobotPlayer.rc;
		rc.broadcast(numSoldiers, n);
	}
	
	// Scouts Counts
	public static int getScouts() throws GameActionException
	{
		RobotController rc = RobotPlayer.rc;
		return rc.readBroadcast(numScouts);
	}
	
	public static void updateScouts(int n) throws GameActionException
	{
		RobotController rc = RobotPlayer.rc;
		rc.broadcast(numScouts, n);
	}
	
	// Lumberjack Counts
	public static int getLumberjacks() throws GameActionException
	{
		RobotController rc = RobotPlayer.rc;
		return rc.readBroadcast(numLumberjacks);
	}
	
	public static void updateLumberjacks(int n) throws GameActionException
	{
		RobotController rc = RobotPlayer.rc;
		rc.broadcast(numLumberjacks, n);
	}
	
	// Tank Counts
	public static int getTanks() throws GameActionException
	{
		RobotController rc = RobotPlayer.rc;
		return rc.readBroadcast(numTanks);
	}
	
	public static void updateTanks(int n) throws GameActionException
	{
		RobotController rc = RobotPlayer.rc;
		rc.broadcast(numTanks, n);
	}
	
	public static void broadcastNumArchons() throws GameActionException {
		RobotController rc = RobotPlayer.rc;
		int num = rc.getInitialArchonLocations(rc.getTeam().opponent()).length;
		rc.broadcast(numEnemyArchons, num);
	}
		
	// Return the donation count 
	public static int getDonationCount() throws GameActionException 
	{
		RobotController rc = RobotPlayer.rc;
		return rc.readBroadcast(donationCount);
	}
	
	public static void broadcastDonationCount(int toAdd) throws GameActionException
	{
		RobotController rc = RobotPlayer.rc;
		rc.broadcast(donationCount, getDonationCount() + toAdd);
	}
	
	public static void setArchonIDs(int first, int second, int third) throws GameActionException {
		RobotController rc = RobotPlayer.rc;
		if (first != -1) {
			rc.broadcast(archonID1, first);
		}
		
		if (second != -1) {
			rc.broadcast(archonID2, second);
		}
		
		if (third != -1) {
			rc.broadcast(archonID3, third);
		}
		
	}
	
	
	// Last Known Enemy Archon Location
	public static void broadcastOppArchon(MapLocation l, int id) throws GameActionException
	{
		RobotController rc = RobotPlayer.rc;
		int id1 = rc.readBroadcast(archonID1);
		int id2 = rc.readBroadcast(archonID2);
		int id3 = rc.readBroadcast(archonID3);
	
		if (id1 == 0) {
			rc.broadcast(archonID1, -1);
			id1 = -1;
		} else if (id2 == 0) {
			rc.broadcast(archonID2, -2);
			id2 = -2; 
		} else if (id3 == 0) {
			rc.broadcast(archonID3, -3);
			id3 = -3;
		}
		
		if (id1 == id || id == -1) {
			rc.broadcast(oppArchonX1, (int) l.x);
			rc.broadcast(oppArchonY1, (int) l.y);
			rc.broadcast(archonSightingTimestamp, rc.getRoundNum());
		} else if (id2 == id || id == -2) {
			rc.broadcast(oppArchonX2, (int) l.x);
			rc.broadcast(oppArchonY2, (int) l.y);
			rc.broadcast(archonSightingTimestamp, rc.getRoundNum());
		} else if (id3 == id || id == -3) {
			rc.broadcast(oppArchonX3, (int) l.x);
			rc.broadcast(oppArchonY3, (int) l.y);
			rc.broadcast(archonSightingTimestamp, rc.getRoundNum());
		} 
	}
	
	public static MapLocation[] getArchonLocations() throws GameActionException {
		RobotController rc = RobotPlayer.rc;
		MapLocation[] locations = new MapLocation[0];
		switch (rc.readBroadcast(numEnemyArchons)) {
		case 1: 
			locations = new MapLocation[1];
			locations[0] = new MapLocation(rc.readBroadcast(oppArchonX1), rc.readBroadcast(oppArchonY1)); 
			break;
		case 2: 
			locations = new MapLocation[2];
			locations[0] = new MapLocation(rc.readBroadcast(oppArchonX1), rc.readBroadcast(oppArchonY1)); 
			locations[1] = new MapLocation(rc.readBroadcast(oppArchonX2), rc.readBroadcast(oppArchonY2)); 
			break;
		case 3: 
			locations = new MapLocation[3];
			locations[0] = new MapLocation(rc.readBroadcast(oppArchonX1), rc.readBroadcast(oppArchonY1)); 
			locations[1] = new MapLocation(rc.readBroadcast(oppArchonX2), rc.readBroadcast(oppArchonY2)); 
			locations[2] = new MapLocation(rc.readBroadcast(oppArchonX3), rc.readBroadcast(oppArchonY3)); 
			break;
		}
				
		return locations; 
	}
	
	public static MapLocation getClosestArchonLocation() throws GameActionException {
		RobotController rc = RobotPlayer.rc;
		MapLocation[] locations = getArchonLocations(); 
		
		if (locations.length == 0) {
			return null; 
		}
				
		MapLocation closest = null;
		float distance = Integer.MAX_VALUE;
		
		
		for (int i = 0; i < locations.length; i++) {
			if (locations[i].x != -1) {
				float temp = rc.getLocation().distanceTo(locations[i]);
				if (temp < distance) {
					distance = temp; 
					closest = locations[i];
				}
			}
		}
		
		return closest; 
	}
	
	public static int[] getClosestArchonLocationAndID() throws GameActionException {
		RobotController rc = RobotPlayer.rc;
		MapLocation[] locations = getArchonLocations(); 
		int[] locationAndId = new int[3];
		int position = 0; 
		
		if (locations.length == 0) {
			return null; 
		}
		
		MapLocation closest = locations[0];
		float distance = rc.getLocation().distanceTo(closest);
		
		
		for (int i = 1; i < locations.length; i++) {
			float temp = rc.getLocation().distanceTo(locations[i]);
			if (temp < distance) {
				distance = temp; 
				closest = locations[i];
				position = i; 
			}
		}
		
		locationAndId[0] = (int) closest.x;
		locationAndId[1] = (int) closest.y;
		locationAndId[2] = position == 0? rc.readBroadcast(archonID1) : position == 1? rc.readBroadcast(archonID2) : rc.readBroadcast(archonID3);
		
		return locationAndId[0] != -1? locationAndId : null; 
	}
	public static int getLastArchonSighting() throws GameActionException
	{
		RobotController rc = RobotPlayer.rc;
		return rc.readBroadcast(archonSightingTimestamp);
	}
	
	public static MapLocation getOppArchonLoc() throws GameActionException
	{
		RobotController rc = RobotPlayer.rc;
		int x = rc.readBroadcast(oppArchonX1);
		int y = rc.readBroadcast(oppArchonY1);
		return x != -1 && y != -1? new MapLocation(x,y) : null;
	}
	
	public static void setAreaOfInterest(MapLocation l) throws GameActionException
	{
		RobotController rc = RobotPlayer.rc;
		RobotType t = rc.getType();
		if(t == RobotType.ARCHON || t == RobotType.SCOUT)
		{
			rc.broadcast(areaOfInterestX, (int)l.x);
			rc.broadcast(areaOfInterestY, (int)l.y);
		}
	}
	

	public static MapLocation getAreaOfInterest() throws GameActionException
	{
		RobotController rc = RobotPlayer.rc;
		int x = rc.readBroadcast(areaOfInterestX);
		int y = rc.readBroadcast(areaOfInterestY);
		return new MapLocation(x,y);
	}
	
	public static void setAreaOfMilitaryInterest(MapLocation l) throws GameActionException 
	{
		RobotController rc = RobotPlayer.rc;
		rc.broadcast(areaOfInterestMilitaryX, (int) l.x);
		rc.broadcast(areaOfInterestMilitaryY, (int) l.y);
	}
	
	public static MapLocation getAreaOfMilitaryInterest() throws GameActionException 
	{
		RobotController rc = RobotPlayer.rc;
		int x = rc.readBroadcast(areaOfInterestMilitaryX);
		int y = rc.readBroadcast(areaOfInterestMilitaryY);
		return x != -1 && y != -1? new MapLocation(x,y) : null;
	}
	
	public static Direction getDirectionToInitialArchonLoc() throws GameActionException
	{
		RobotController rc = RobotPlayer.rc;
		MapLocation oppArchon = rc.getInitialArchonLocations(rc.getTeam().opponent())[0];
		if(oppArchon == null)
			return null;
		return rc.getLocation().directionTo(oppArchon);
	}
}

package mangelware.util;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Broadcast {

	public static RobotController rc;

	private static final int CHANNEL_COUNT_ARCHON = 0;
	private static final int CHANNEL_COUNT_GARDENER = 1;
	private static final int CHANNEL_COUNT_LUMBERJACK = 2;
	private static final int CHANNEL_COUNT_SOLDIER = 3;
	private static final int CHANNEL_COUNT_TANK = 4;
	private static final int CHANNEL_COUNT_SCOUT = 5;


	/*
	 * depending on the turn, either updates own unit count,
	 * resets broadcast for future count,
	 * or adds itself to broadcast count.
	 *
	 * gets round number modulo 6:
	 * 0: archons report		| 	gardeners reset
	 * 1: gardeners report		| 	lumberjacks reset
	 * 2: lumberjacks report	| 	soldiers reset
	 * 3: soldiers report		| 	tanks reset
	 * 4: tanks report			| 	scouts reset
	 * 5: scouts report			|	archons rest
	 *
	 */
    public static void updateUnitCount() throws GameActionException {
        int round = rc.getRoundNum() % 6;
        RobotType type = rc.getType();
        updateUnitCount(round, type);
    }
	private static void updateUnitCount(int round, RobotType type) throws GameActionException {
		switch(type) {
		case ARCHON: 		archonUpdate(round); 	break;
		case GARDENER: 		gardenerUpdate(round);	break;
		case LUMBERJACK:	lumberUpdate(round);	break;
		case SCOUT:			scoutUpdate(round);		break;
		case SOLDIER:		soldierUpdate(round);	break;
		case TANK:			tankUpdate(round);		break;
		}

		switch(round) {
		case 0:	countLumberjacks(); countSoldiers(); 	countTanks(); 		countScouts(); 		break;
		case 1:	countArchons(); 	countSoldiers(); 	countTanks(); 		countScouts(); 		break;
		case 2:	countArchons(); 	countGardeners(); 	countTanks(); 		countScouts(); 		break;
		case 3:	countArchons(); 	countGardeners(); 	countLumberjacks(); countScouts(); 		break;
		case 4:	countArchons(); 	countGardeners(); 	countLumberjacks(); countSoldiers();	break;
		case 5:	countGardeners(); 	countLumberjacks();	countSoldiers(); 	countTanks(); 		break;
		}

	}
	private static void archonUpdate(int round) throws GameActionException {
		if(round == 0) {
			int curr = rc.readBroadcast(CHANNEL_COUNT_ARCHON);
			rc.broadcast(CHANNEL_COUNT_ARCHON, curr + 1);
		}
		else if(round == 5) {
			rc.broadcast(CHANNEL_COUNT_ARCHON, 0);
		}
	}
	private static void gardenerUpdate(int round) throws GameActionException {
		if(round == 1) {
			int curr = rc.readBroadcast(CHANNEL_COUNT_GARDENER);
			rc.broadcast(CHANNEL_COUNT_GARDENER, curr + 1);
		}
		else if(round == 0) {
			rc.broadcast(CHANNEL_COUNT_GARDENER, 0);
		}
	}
	private static void lumberUpdate(int round) throws GameActionException {
		if(round == 2) {
			int curr = rc.readBroadcast(CHANNEL_COUNT_LUMBERJACK);
			rc.broadcast(CHANNEL_COUNT_LUMBERJACK, curr + 1);
		}
		else if(round == 1) {
			rc.broadcast(CHANNEL_COUNT_LUMBERJACK, 0);
		}
	}
	private static void soldierUpdate(int round) throws GameActionException {
		if(round == 3) {
			int curr = rc.readBroadcast(CHANNEL_COUNT_SOLDIER);
			rc.broadcast(CHANNEL_COUNT_SOLDIER, curr + 1);
		}
		else if(round == 2) {
			rc.broadcast(CHANNEL_COUNT_SOLDIER, 0);
		}
	}
	private static void tankUpdate(int round) throws GameActionException {
		if(round == 4) {
			int curr = rc.readBroadcast(CHANNEL_COUNT_TANK);
			rc.broadcast(CHANNEL_COUNT_TANK, curr + 1);
		}
		else if(round == 3) {
			rc.broadcast(CHANNEL_COUNT_TANK, 0);
		}
	}
	private static void scoutUpdate(int round) throws GameActionException {
		if(round == 5) {
			int curr = rc.readBroadcast(CHANNEL_COUNT_SCOUT);
			rc.broadcast(CHANNEL_COUNT_SCOUT, curr + 1);
		}
		else if(round == 4) {
			rc.broadcast(CHANNEL_COUNT_SCOUT, 0);
		}
	}
	private static void countArchons() throws GameActionException {
		GameState.archonCount = rc.readBroadcast(CHANNEL_COUNT_ARCHON);
	}
	private static void countGardeners() throws GameActionException {
		GameState.gardenerCount = rc.readBroadcast(CHANNEL_COUNT_GARDENER);
	}
	private static void countLumberjacks() throws GameActionException {
		GameState.lumberCount = rc.readBroadcast(CHANNEL_COUNT_LUMBERJACK);
	}
	private static void countSoldiers() throws GameActionException {
		GameState.soldierCount = rc.readBroadcast(CHANNEL_COUNT_SOLDIER);
	}
	private static void countTanks() throws GameActionException {
		GameState.tankCount = rc.readBroadcast(CHANNEL_COUNT_TANK);
	}
	private static void countScouts() throws GameActionException {
		GameState.scoutCount = rc.readBroadcast(CHANNEL_COUNT_SCOUT);
	}




}
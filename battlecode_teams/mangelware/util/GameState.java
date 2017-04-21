package mangelware.util;

import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class GameState {

	public static RobotController rc;
    public static MapLocation[] ourSpawns, enemySpawns;
    public static MapLocation ourCenter, enemyCenter, center, mySpawn, targetLocation;
    public static int startArchons, spawnRound, waveNumber;
    public static int archonCount, gardenerCount, scoutCount, soldierCount, tankCount, lumberCount;

    public static float bulletIncome() {
    	float flat = GameConstants.ARCHON_BULLET_INCOME;
    	float penalty = GameConstants.ARCHON_BULLET_INCOME * rc.getTeamBullets();
    	float auto = (flat - penalty < 0) ? 0 : flat - penalty;

    	float maxPerTree = GameConstants.BULLET_TREE_BULLET_PRODUCTION_RATE * GameConstants.BULLET_TREE_MAX_HEALTH;
    	float tree = rc.getTreeCount() * maxPerTree;
    	float estimate = tree * 0.85f;

    	return auto + estimate;
    }


    public static float maxShotCostsPerTurn() {
    	float scoutCost = scoutCount;
    	float soldierCost = GameConstants.TRIAD_SHOT_COST * soldierCount;
    	float tankCost = GameConstants.PENTAD_SHOT_COST * tankCount;
    	return scoutCost + soldierCost + tankCost;
    }


    public static boolean canWinByPoints() {
    	float vpCost = GameConstants.VP_BASE_COST + rc.getRoundNum() * GameConstants.VP_INCREASE_PER_ROUND;
    	float vpNeeded = GameConstants.VICTORY_POINTS_TO_WIN - rc.getTeamVictoryPoints();
    	float victoryCost = vpCost * vpNeeded;
    	return victoryCost <= rc.getTeamBullets();
    }

}

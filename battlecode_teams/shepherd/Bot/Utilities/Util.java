package shepherd.Bot.Utilities;


import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

public class Util {

	/*
	 * returns the robot with the lowest amount of health of the given type from given array
	 */
	public static RobotInfo getLowestOfTypeFromList(RobotType type, RobotInfo[] robots) {
		RobotInfo lowest = null;
		double minHP = Double.MAX_VALUE;
		for(RobotInfo robot : robots) {
			if(robot.getType() == type && robot.getHealth() < minHP) {
				lowest = robot;
				minHP = robot.getHealth();
			}
		}
		return lowest;
	}


	/*
	 * returns the maximum range of attacks in a turn: max(move + direct attack)
	 */
	private static float maxAttackRangePerTurn = Float.MIN_VALUE;
	public static float getMaxAttackRangePerTurn() {
		if(maxAttackRangePerTurn != Float.MIN_VALUE) return maxAttackRangePerTurn;

		float max = Float.MIN_VALUE, curr = Float.MIN_VALUE;
		for(RobotType type : RobotType.values()) {
			if(type.canAttack()) {
				curr = type.strideRadius + type.bodyRadius + GameConstants.BULLET_SPAWN_OFFSET + type.bulletSpeed;
				if(curr > max) max = curr;
			}
		}
		maxAttackRangePerTurn = max;

		return maxAttackRangePerTurn;
	}


	/*
	 * return the maximum bullet speed
	 * (in case specs change)
	 */
	private static float maxBulletSpeed = Float.MIN_VALUE;
	public static float getMaxBulletSpeed() {
		if(maxBulletSpeed != Float.MIN_VALUE) return maxBulletSpeed;

		float max = Float.MIN_VALUE, curr = Float.MIN_VALUE;
		for(RobotType type : RobotType.values()) {
			if(type.canAttack()) {
				curr = type.bulletSpeed;
				if(curr > max) max = curr;
			}
		}
		maxBulletSpeed = max;

		return maxBulletSpeed;
	}


	/*
	 * return the maximum stride radius
	 * (in case specs change)
	 */
	private static float maxStrideRadius = Float.MIN_VALUE;
	public static float getMaxStrideRadius() {
		if(maxStrideRadius != Float.MIN_VALUE) return maxStrideRadius;

		float max = Float.MIN_VALUE, curr = Float.MIN_VALUE;
		for(RobotType type : RobotType.values()) {
				curr = type.strideRadius;
				if(curr > max) max = curr;
		}
		maxStrideRadius = max;

		return maxStrideRadius;
	}

	/*
	 * return attack range of given unit type
	 */
	public static float getMaxAttackRange(RobotType type) {
		if(!type.canAttack()) return 0;
		if(type == RobotType.LUMBERJACK) return type.strideRadius + GameConstants.LUMBERJACK_STRIKE_RADIUS;
		return type.bodyRadius + type.strideRadius + type.bulletSpeed + GameConstants.BULLET_SPAWN_OFFSET;
	}



	/*
	 * returns two integers representing the maplocation's x and y location
	 */
	public static int[] getLocation(MapLocation location) {
		return new int[]{Float.floatToIntBits(location.x), Float.floatToIntBits(location.y)};
	}

	/*
	 * returns a maplocation from two given ints
	 */
	public static MapLocation getLocation(int x, int y) {
		return new MapLocation(Float.intBitsToFloat(x), Float.intBitsToFloat(y));
	}


	/*
	 * returns a maplocation from the first two ints of given int array
	 */
	public static MapLocation getLocation(int[] ints) {
		return getLocation(ints[0], ints[1]);
	}

}

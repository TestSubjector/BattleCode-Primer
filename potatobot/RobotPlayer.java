package potatobot;
import battlecode.common.*;

public class RobotPlayer extends Globals
{
	public static void run(RobotController rcinit)throws GameActionException
	{
		init(rcinit);
		updateLocation();
		switch (myType) 
		{
		    case ARCHON:
				ArchonBot.loop();
		        break;	
		    case GARDENER:
		    	GardenerBot.loop();
		    	break;
		    case SCOUT:
		    	ScoutBot.loop();
		    	break;
		   case SOLDIER:
		    	SoldierBot.loop();
		        break;
		    case TANK:
		    	TankBot.loop();
		    	break;
		    case LUMBERJACK:
		    	LumberjackBot.loop();
		    	break;
		}
	}
}
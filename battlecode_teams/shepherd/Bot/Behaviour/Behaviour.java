package shepherd.Bot.Behaviour;

import battlecode.common.GameActionException;
import shepherd.Bot.Bot;

public abstract class Behaviour {

	public Bot executer;

	public abstract void execute() throws GameActionException;

}

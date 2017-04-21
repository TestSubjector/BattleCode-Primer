package ebotplayer.units;

import battlecode.common.*;
import ebotplayer.units.base.BulletUnit;
/**
 * Created by ebot on 1/10/17.
 */
public class Scout extends BulletUnit {
    public Scout(RobotController rc) {
        super(rc);
        while(true) {
            try {
                c.vp();
                u.sendAliveSignal();
                a.bullet();
                c.shake();
                if (!rc.hasAttacked()) {
                    if(a.getLastDir() != null && rc.canMove(a.getLastDir())) {
                        m.moveTo(a.getLastDir());
                    }
                    else{
                        m.wander(30, 12);
                    }
                }
                c.vpEnd();
                Clock.yield();
            } catch (Exception e) {
                System.out.println("Scout Exception");
                e.printStackTrace();
            }
        }
    }
}

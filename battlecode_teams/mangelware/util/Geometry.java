package mangelware.util;

import battlecode.common.BodyInfo;
import battlecode.common.BulletInfo;
import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Geometry {

	public static RobotController rc;

	public static MapLocation centerOf(MapLocation... locations) {
		if(locations.length == 0) return null;
		if(locations.length == 1) return locations[0];
		float x = 0, y = 0;
		for(MapLocation location : locations) {
			x += location.x; y += location.y;
		}
		return new MapLocation(x/locations.length, y/locations.length);
	}

    /*
     * returns if given body is in specified rectangle
     *
     * @ param:
     * 		BodyInfo body: the body to be checked against the rectangle
     * 		Direction dir: the direction the rectangle is aligned with
     * 		MapLocation M: middle point of outer-most edge of rectangle
     * 		float h: half the rectangles height
     */
    public static boolean isInRectangle(BodyInfo body, Direction dir, MapLocation M, float h) {
    	 float alpha = Math.abs(M.directionTo(body.getLocation()).radiansBetween(dir));
    	 if(alpha >= Math.PI/2) return false;
    	 float d = M.distanceTo(body.getLocation());
    	 float x = (float)(d * Math.sin(alpha));
    	 return x - h <= body.getRadius();
    }


    /*
     * returns if given circle is in specified rectangle
     *
     * @ param:
     * 		BodyInfo body: the body to be checked against the rectangle
     * 		Direction dir: the direction the rectangle is aligned with
     * 		MapLocation M: middle point of outer-most edge of rectangle
     * 		float h: half the rectangles height
     */
    public static boolean isInRectangle(MapLocation C, float radius, Direction dir, MapLocation M, float h) {
    	 float alpha = Math.abs(M.directionTo(C).radiansBetween(dir));
    	 if(alpha >= Math.PI/2) return false;
    	 float d = M.distanceTo(C);
    	 float x = (float)(d * Math.sin(alpha));
    	 return x - h <= radius;
    }


    /*
     * returns intersection of ray with circle
     * returns null if ray does not intersect given circle
     */
    public static MapLocation intersection(MapLocation o, Direction d, MapLocation c, float r) {
    	Direction l = new Direction(o,c);
    	float ldx = l.getDeltaX(1), ldy = l.getDeltaY(1);
    	float s = ldx*d.getDeltaX(1) + ldy * d.getDeltaY(1);
    	float l2 = ldx * ldx + ldy * ldy;
    	float r2 = r*r;
    	if(s < 0 && l2 > r2) return null;
    	float m2 = l2 - s*s;
    	if(m2 > r2) return null;
    	float q = (float)Math.sqrt(r*r - m2);
    	float t = (l2 > r2) ? s - q : s + q;
    	return o.add(d, t);
    }
    public static MapLocation intersection(BulletInfo b, MapLocation c, float r) {
    	return intersection(b.getLocation(), b.getDir(), c, r);
    }



    /*
     * returns intersection of two lines, as represented by given bullets
     * returns null if A lies on B or if no intersection exists
     */
    public static MapLocation intersection(BulletInfo A, BulletInfo B) {
    	float x1 = A.getLocation().x, y1 = A.getLocation().y;
    	float x2 = B.getLocation().x, y2 = B.getLocation().y;
    	float dx1 = A.getDir().getDeltaX(1), dy1 = A.getDir().getDeltaY(1);
    	float dx2 = B.getDir().getDeltaX(1), dy2 = B.getDir().getDeltaY(1);

    	// check if same direction
    	float deg = A.getDir().degreesBetween(B.getDir());
    	if(deg == 0 || deg == 180 || deg == -180) {
    		return null;
    	}

    	float v = ((x2 - x1)*dy1 - y2*dx1 + y1*dx1) / (dy2*dx1 - dx2*dy1);
    	float x = x2 + v*dx2, y = y2 + v*dy2;
    	return new MapLocation(x,y);
    }
}

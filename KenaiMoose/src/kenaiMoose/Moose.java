package kenaiMoose;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.gis.Geography;
import repast.simphony.util.ContextUtils;

public class Moose {
	
	private String name;
	private Geometry boundary;
	private GeometryFactory geofac = new GeometryFactory();
	// TODO create and utilize energy value
	
	public Moose(String name, Geometry boundary) {
		this.name = name;
		this.boundary = boundary;
	}

	// Establishing random moves for Moose agents
	@ScheduledMethod(start = 1, interval = 1, priority = ScheduleParameters.FIRST_PRIORITY)
	public void step() {
		randomWalk();
	}
	
//	public void randomWalk() {
//		Context context = ContextUtils.getContext(this);
//		Geography<Moose> geography = (Geography)context.getProjection("Kenai");
//		
//		Coordinate prevLocation = geography.getGeometry(this).getCoordinate();
//		boolean notMoved = true;
//		do {
//			// Randomizing new coordinates
//			Coordinate coord = new Coordinate(prevLocation.x += RandomHelper.nextDoubleFromTo(-0.0005, 0.0005), 
//					prevLocation.y += RandomHelper.nextDoubleFromTo(-0.0005, 0.0005));
//			Point newPoint = geofac.createPoint(coord);
//			
//			// Checking for boundary and resetting coordinates if out of bounds
//			if (newPoint.within(boundary)) {
//				geography.move(this, newPoint);
//				notMoved = false;
//			}
//			
//			// Garbage collecting on failed attempt to move
//			else {
//				coord = null;
//				newPoint = null;
//			}
//			
//		} while (notMoved);
//		
//		//geography.moveByDisplacement(this, RandomHelper.nextDoubleFromTo(-0.0005, 0.0005), 
//				//RandomHelper.nextDoubleFromTo(-0.0005, 0.0005));
//	}
	
	
	// This randomWalk() seems to run more efficiently than the above, no hangs and excessive object creation.
	public void randomWalk() {
		Context context = ContextUtils.getContext(this);
		Geography<Moose> geography = (Geography)context.getProjection("Kenai");
		
		Coordinate prevLocation = geography.getGeometry(this).getCoordinate();
		Coordinate coord = new Coordinate(prevLocation.x += RandomHelper.nextDoubleFromTo(-0.0005, 0.0005), 
				prevLocation.y += RandomHelper.nextDoubleFromTo(-0.0005, 0.0005));
		Point newPoint = geofac.createPoint(coord);
		
		// Ensuring within bounds
		while (!newPoint.within(boundary)) {
			coord.x = prevLocation.x + RandomHelper.nextDoubleFromTo(-0.0005, 0.0005);
			coord.y = prevLocation.y + RandomHelper.nextDoubleFromTo(-0.0005, 0.0005);
			newPoint = geofac.createPoint(coord);
		}
		
		// Updating Moose location
		geography.move(this, newPoint);
	}
	
	public String getName() {
		return name;
	}
}

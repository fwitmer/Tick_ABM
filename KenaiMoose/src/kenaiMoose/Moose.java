package kenaiMoose;

import java.util.List;

import org.geotools.coverage.grid.GridCoverage2D;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.gis.Geography;
import repast.simphony.util.ContextUtils;

public class Moose extends Vector {

	public Moose(String name, Geometry boundary) {
		super(name, boundary);
		infection_radius = 500;
		
	}
	
	// init() called before first tick of model - add things that may require
	// functionality before Moose object is added to Context and Geography
	@ScheduledMethod(start = 0)
	public void init() {
		addBuffer(infection_radius);
	}

	// Establishing random moves for Moose agents
	@ScheduledMethod(start = 1, interval = 1, priority = ScheduleParameters.FIRST_PRIORITY)
	public void step() {
		walk();
		List<Tick> tickList = getTicks();
		processInfections(tickList);
	}
		
	// Logic for checking for proper bounds and raster data for each step
	protected void walk() {		
		Context context = ContextUtils.getContext(this);
		Geography geography = (Geography)context.getProjection("Kenai");
		Coordinate prevLocation = geography.getGeometry(this).getCoordinate(); // Saving previous location to revert back to if out of bounds
		
		// Attempting to create new random Coordinate and Point from previous location
		Coordinate coord = new Coordinate(prevLocation.x += RandomHelper.nextDoubleFromTo(-0.0005, 0.0005), 
				prevLocation.y += RandomHelper.nextDoubleFromTo(-0.0005, 0.0005));
		Point newPoint = geoFac.createPoint(coord);
		int stuck_in_water = 0;
		// Ensuring within bounds and not in water
		while (!newPoint.within(boundary) || isWater(coord)) {
			if (stuck_in_water > 1000) {
				System.out.println("Stuck in water! Moose created in 11 or 12.");
				break;
			}
			coord.x = prevLocation.x + RandomHelper.nextDoubleFromTo(-0.1000, 0.1000);
			coord.y = prevLocation.y + RandomHelper.nextDoubleFromTo(-0.1000, 0.1000);
			newPoint = geoFac.createPoint(coord);
			stuck_in_water++;
		}
		
		
		// Updating Moose location
		geography.move(this, newPoint);
		// Updating InfectionZone
		updateInfectionZone();
	}
	
}

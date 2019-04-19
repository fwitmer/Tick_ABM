package kenaiMoose;

import java.util.List;
import java.util.Random;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;

public class Moose extends Host {
	double direction;

	public Moose(String name) {
		super(name);
		infection_radius = 500;	
	}
	
	// init() called before first tick of model - add things that may require
	// functionality before Moose object is added to Context and Geography
	@Override
	@ScheduledMethod(start = 0)
	public void init() {
		super.init();		
		addBuffer(infection_radius);
		
		Random random = new Random();
		direction = Math.toRadians(random.nextInt(360)); // Assign random direction for travel
	}

	// Establishing random moves for Moose agents
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		walk();
		List<Tick> tickList = getTicks();
		processInfections(tickList);
	}
		
	// Logic for checking for proper bounds and raster data for each step
	protected void walk() {		
		Coordinate prevLocation = geography.getGeometry(this).getCoordinate(); // Saving previous location to revert back to if out of bounds
		
		// Attempting to create new random Coordinate and Point from previous location
		Coordinate coord = new Coordinate(prevLocation.x += RandomHelper.nextDoubleFromTo(-0.0050, 0.0050), 
				prevLocation.y += RandomHelper.nextDoubleFromTo(-0.0005, 0.0005));
		Point newPoint = geoFac.createPoint(coord);
		int stuck_in_water = 0;
		// Ensuring within bounds and not in water
		while (!newPoint.within(boundary) || isWater(coord)) {
			if (stuck_in_water > 1000) {
				System.out.println("Stuck in water! Moose created in 11 or 12.");
				break;
			}
			coord.x = prevLocation.x + RandomHelper.nextDoubleFromTo(-0.0050, 0.0050);
			coord.y = prevLocation.y + RandomHelper.nextDoubleFromTo(-0.0050, 0.0050);
			newPoint = geoFac.createPoint(coord);
			stuck_in_water++;
		}
		
		
		// Updating Moose location
		geography.move(this, newPoint);
		// Updating InfectionZone
		updateInfectionZone();
	}
	/*
	protected void directional_walk() {
		Coordinate prev_coord = geography.getGeometry(this).getCoordinate();
		Coordinate test_coord = prev_coord;
		Point test_point = geoFac.createPoint(prev_loc);
		geography.moveByVector(test_point, 50, direction);
		
		Random random = new Random();
		int x = random.nextInt(2); // Pick a direction
		while (!test_point.within(boundary) || isWater(prev_loc)) {
			
		}
		
		geography.moveByVector(this, 50, direction);
		updateInfectionZone();
	} */
	
}

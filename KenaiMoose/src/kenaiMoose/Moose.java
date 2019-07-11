package kenaiMoose;

import java.util.List;
import java.util.Random;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;

public class Moose extends Host {
	private double direction; // The mean direction for drawing Gaussian randoms

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
		directional_walk();
		List<Tick> tickList = getTicks();
		processInfections(tickList);
	}
		
	// Logic for checking for proper bounds and raster data for each step
	protected void walk() {		
		Coordinate prevLocation = geography.getGeometry(this).getCoordinate(); // Saving previous location to revert back to if out of bounds
		
		// Attempting to create new random Coordinate and Point from previous location
		Coordinate coord = new Coordinate(prevLocation.x += RandomHelper.nextDoubleFromTo(-0.0050, 0.0050), 
				prevLocation.y += RandomHelper.nextDoubleFromTo(-0.0005, 0.0005)); //change these values for Vole
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
	
	protected void directional_walk() {
		
		// Saving previous Coordinate and Point for reference in case of invalid move
		Coordinate prev_coord = getCoord();
		Point prev_point = getPoint();
		
		// Adding random wiggle
		Random random = new Random(); 
		direction = random.nextGaussian() * (Math.PI / 24) + direction; // Std. dev. of PI/24 and mean of current direction
		// Controlling for direction > 360º and < 0º
		if (direction > 2 * Math.PI) {
			direction = direction - (2 * Math.PI);
		}
		if (direction < 0) {
			direction = direction + (2 * Math.PI);
		}
		
		// Moving Moose and getting test_coord and test_point for checking validity of move
		geography.moveByVector(this, 50, direction);
		Coordinate test_coord = getCoord();
		Point test_point = getPoint();
		
		int x = 0; // Counter for processing behavioral attempts
		// Checking if we went out of bounds and adjusting
		if (!test_point.within(boundary)) {
			System.out.println("Boundary adjustment: " + this.name);
			geography.move(this, prev_point); // moving back to start
			System.out.println("\tCurrent Point: " + getPoint().toString());
			if (direction < Math.PI) {
				direction = direction + Math.PI;
			}
			else {
				direction = direction - Math.PI;
			}
			// TODO: determine distance between prev_coord and boundary to get more accurate bounce behavior
			//		 currently arbitrarily half of previous attempt to move that placed us out of bounds
			geography.moveByVector(this, 100, direction); 
			test_coord = getCoord();
			test_point = getPoint();
			System.out.println("\tDirection: " + direction);
			System.out.println("\tCoords: " + test_coord.toString());
			System.out.println("\tPoint: " + test_point.toString());
			System.out.println("\tOrigin: " + prev_coord.toString());
			
		}
		
		if (isWater(test_coord)) {
			System.out.println("Water adjustment: " + this.name);
			
			int left_or_right = random.nextInt(2); // Pick a direction
			
			System.out.println("\tOrigin coords: " + prev_coord.toString());
			System.out.println("\tStarting direction: " + Math.toDegrees(direction));
			
			switch (left_or_right) {
				case 0:
					System.out.print("\tWent left ");
					break;
				case 1:
					System.out.print("\tWent right ");
					break;
			}
			x = 0;
			
			// TODO: Look into teleporting water behavior and reflect 180º if proper solution can't be found.
			while (isWater(test_coord)) {
				geography.move(this, prev_point); // moving back to start
				switch (left_or_right) {
					case 0: // left
						direction = direction + (Math.PI/24); // 7.5º
						break;
					case 1: // right
						direction = direction - (Math.PI/24);
						break;
						
				}
				if (direction > 2 * Math.PI) {
					direction = direction - 2 * Math.PI;
				}
				if (direction < 0) {
					direction = direction + 2 * Math.PI;
				}
				
				geography.moveByVector(this, 50, direction);
				test_coord = getCoord();
				test_point = getPoint();
				x++;
			}
			System.out.println(x + " times.");
			System.out.println("\tEnding direction: " + Math.toDegrees(direction));
			System.out.println("\tEnding coord: " + getCoord());
			
		}
		
		geography.move(this, test_point);
		updateInfectionZone();
	} 
	
}

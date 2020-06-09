package kenaiMoose;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.DirectPosition2D;
import org.opengis.coverage.PointOutsideCoverageException;
import org.opengis.geometry.DirectPosition;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;

public class Moose extends Host {
	private double direction; // The mean direction for drawing Gaussian randoms
	protected static int travel_dist_meters = 500; // travel distance of 500 meters per day

	public Moose(String name) {
		super(name);
		infection_radius = 50;	
	}
	
	// init() called before first step of model - add things that may require
	// functionality before Moose object is added to Context and Geography
	@Override
	@ScheduledMethod(start = 0)
	public void init() {
		super.init();		
		addBuffer(infection_radius);
		
		Random random = new Random();
		direction = Math.toRadians(random.nextInt(360)); // Assign random direction for travel
	}

	// Moose actions every tick should be prioritized for adult Tick attachment behavior
	@ScheduledMethod(start = 1, interval = 1, priority = ScheduleParameters.FIRST_PRIORITY)
	public void step() {
		walk();
		List<Tick> tickList = getTicks();
		processInfections(tickList);
	}
	
	// Directional walk, Moose will move along a loose vector trajectory
	protected void walk() {
		
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
		geography.moveByVector(this, travel_dist_meters, direction);
		Coordinate test_coord = getCoord();
		Point test_point = getPoint();
		
		// Checking if we went out of bounds and adjusting
 /* Geometry checking method for boundary correction, very expensive!
		if (!test_point.within(boundary)) {
			//System.out.println("Boundary adjustment: " + this.name);
			geography.move(this, prev_point); // moving back to start
			//System.out.println("\tCurrent Point: " + getPoint().toString());
			ArrayList<Tick> tick_list_copy = tick_list;
			removeTicks(tick_list_copy);
			//System.out.println("\tTicks detached and deleted.");
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
			//System.out.println("\tDirection: " + direction);
			//System.out.println("\tCoords: " + test_coord.toString());
			//System.out.println("\tPoint: " + test_point.toString());
			//System.out.println("\tOrigin: " + prev_coord.toString());
			
		}
		*/

		
		if (!within_bound(test_coord)) {
			geography.move(this, prev_point); // moving back to start
			ArrayList<Tick> tick_list_copy = tick_list;
			removeTicks(tick_list_copy);
			//System.out.println("\tTicks detached and deleted.");
			if (direction < Math.PI) {
				direction = direction + Math.PI;
			}
			else {
				direction = direction - Math.PI;
			}
			// TODO: determine distance between prev_coord and boundary to get more accurate bounce behavior
			//		 currently arbitrarily half of previous attempt to move that placed us out of bounds
			geography.moveByVector(this, travel_dist_meters / 2, direction); 
			test_coord = getCoord();
			test_point = getPoint();
		}
		
		
		if (isWater(test_coord)) {
			//System.out.println("Water adjustment: " + this.name);
			geography.move(this,  prev_point);
			//System.out.println("\tCurrent Point: " + getPoint().toString());
			if (direction < Math.PI) {
				direction = direction + Math.PI;
			}
			else {
				direction = direction - Math.PI;
			}
			geography.moveByVector(this, travel_dist_meters / 2, direction);
			test_coord = getCoord();
			test_point = getPoint();
			//System.out.println("\tDirection: " + direction);
			//System.out.println("\tCoords: " + test_coord.toString());
			//System.out.println("\tPoint: " + test_point.toString());
			//System.out.println("\tOrigin: " + prev_coord.toString());
			
		}
		
		geography.move(this, test_point);
		updateInfectionZone();
	}
	
	// wrapper method for determining if passed Coordinate is within the boundary raster
	private boolean within_bound(Coordinate coord) {
		GridCoverage2D boundary_coverage = geography.getCoverage("Boundary Raster");
		DirectPosition position = new DirectPosition2D(geography.getCRS(), coord.x, coord.y);
	try {
		byte[] sample = (byte[]) boundary_coverage.evaluate(position);
		if (sample[0] == (byte) 1) return true;
		return false;
	} catch (PointOutsideCoverageException e) {
		e.printStackTrace();
		return false;
	}
	
	}
	
	// processes all Tick agents in a list and removes them in a manner safe for multithreading
	protected void removeTicks(ArrayList<Tick> ticks) {
			for (Iterator<Tick> iter = (Iterator)ticks.iterator(); iter.hasNext(); ) {
				Tick tick = (Tick)iter.next();
				if (tick.getHost() == this) {
					iter.remove();
					tick.detach();
					tick.die();
				}
			}
	}
	
}

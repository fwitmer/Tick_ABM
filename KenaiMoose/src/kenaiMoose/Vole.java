package kenaiMoose;

import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Point;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;

public class Vole extends Host {
	private double vole_range; //? how to determine voles range? What type should this be? Geometry? 
	
	public Vole(String name) {
		super(name); //calling parent constructor 
		vole_range = 100; 
	}

	@Override
	@ScheduledMethod(start=0) //need to call before first tick in model
	public void init() {
		super.init(); 
		addBuffer(vole_range); //TODO need to make a buffer for Vole

	}
	
	@Override
	@ScheduledMethod(start=1, interval=1)
	public void step() {
		vole_walk();
		List<Tick> tickList = getTicks();
		processInfections(tickList);
		
	}

	protected void vole_walk() {
		//do I need prev Coordinate AND prev Point? 
		//TODO change boundary behavior
		
		//save location in case we need to revert back from an invalid move 
		Coordinate prev_coord = getCoord();
		Point prev_point = getPoint();
		
		//create new location based off previous (play with these values)
		Coordinate new_coord = new Coordinate(prev_coord.x += RandomHelper.nextDoubleFromTo(-0.0050, 0.0050), 
				prev_coord.y += RandomHelper.nextDoubleFromTo(-0.0001, 0.0001));
		Point newPoint = geoFac.createPoint(new_coord);
		
		//check if we are in boundary and not in water 
		if(!newPoint.within(boundary) || isWater(new_coord)) {
			System.out.println("Invalid move");
		}
		
		// Create a new point to move to figure out appropriate value for nextDouble
		new_coord.x = prev_coord.x + RandomHelper.nextDoubleFromTo(-0.0010, 0.0010);
		new_coord.y = prev_coord.y + RandomHelper.nextDoubleFromTo(-0.0010, 0.0010);
		newPoint = geoFac.createPoint(new_coord);
		
		//if the new location is within the range, move it...I feel like this isn't right?
		if (new_coord.distance(prev_coord) <= vole_range) {
			geography.move(this, newPoint);
		}
		
	}
	
	@Override
	protected void walk() {
		// don't need this in Vole? Should it be removed from Host eventually?
		
	}

}

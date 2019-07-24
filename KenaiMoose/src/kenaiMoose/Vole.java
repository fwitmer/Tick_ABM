package kenaiMoose;

import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.gis.util.GeometryUtil;
import repast.simphony.random.RandomHelper;

public class Vole extends Host {
	private double vole_range; //? how to determine voles range? What type should this be? Geometry? 
	//Geometry voleBuffer; //do we need a Geometry object to create buffer?
	protected InfectionZone smHost_buffer;
	Geometry vole_home = GeometryUtil.generateBuffer(geography, geography.getGeometry(this), vole_range);
	
	
	
	public Vole(String name) {
		super(name); //calling parent constructor 
		vole_range = 100; 
	}

	@Override
	@ScheduledMethod(start=0) //need to call before first tick in model
	public void init() {
		super.init(); 
		//addBuffer(vole_range); //create buffer (also adds to context)
		create_home(vole_range); //same as addBuffer method...
	
	}
	
	protected Geometry create_home(double range) {
		//FIXME move this to host?
		//crate a Geometry representing buffer around smHost
		Geometry smHost_home = GeometryUtil.generateBuffer(geography, geography.getGeometry(this), vole_range);
		Geometry home_geom = geoFac.createGeometry(smHost_home);
		
		smHost_buffer = new InfectionZone();
		context.add(smHost_buffer);
		geography.move(smHost_buffer, home_geom);
		
		return smHost_home;
	}
	
	@Override
	@ScheduledMethod(start=1, interval=1)
	public void step() {
		walk();
		List<Tick> tickList = getTicks();
		processInfections(tickList);
		
	}

	@Override
	protected void walk() {
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
		
		
		//FIXME want to somehow get range of buffer so agent can't move outside of it
		// How to get buffer....return from 
		if (!newPoint.within(vole_home)) {
			System.out.println("SmHost tried to move outside of home range");
			
		}
		
//		//if the new location is within the range, move it...I feel like this isn't right?
//		if (new_coord.distance(prev_coord) <= vole_range) {
//			geography.move(this, newPoint);
//		}
		
			//move the agent and update it's buffer
		geography.move(this, newPoint);	
		updateInfectionZone();
		
	}

}

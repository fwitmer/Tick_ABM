package kenaiMoose;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.gis.util.GeometryUtil;
import repast.simphony.random.RandomHelper;

public class SmHost extends Host {
	private double smHost_range;
	Geometry smHost_home;
	
	
	public SmHost(String name) {
		super(name); 
		smHost_range = 565; // circle with radius 565m = 1.003km^2
	}

	@Override
	@ScheduledMethod(start=0) //need to call before first tick in model
	public void init() {
		super.init(); 
		smHost_home = addBuffer(smHost_range); //create buffer for limiting agent movement
	
	}
	
	@ScheduledMethod(start=1, interval=1, priority = ScheduleParameters.LAST_PRIORITY)
	public void step() {
		walk();
		List<Tick> tickList = getTicks(); //use overridden method below
		processInfections(tickList); 
		
	}

	@Override
	//SmHost needs it's own method here b/c the parent class method moves the geometry and we don't want it to move
	protected List<Tick> getTicks(){
		Envelope infection_envelope = smHost_home.getEnvelopeInternal();
		
		Iterable<Tick> infectingTicks = geography.getObjectsWithin(infection_envelope, IxPacificus.class);
		List<Tick> tickList = new ArrayList();
		
		for (Tick tick : infectingTicks) {
			if(!tick.isAttached()) {
				tickList.add((Tick)tick);
			}
		}
		return tickList;
	}
	
	protected void walk() {
		Coordinate coord;
		Point pt;
		//Generate a point within the agents home range and check if it's a valid move
		do {
			List<Coordinate> test_coords = GeometryUtil.generateRandomPointsInPolygon(smHost_home, 1);
			coord = test_coords.get(0);
			pt = geoFac.createPoint(coord); //Create a point based off the coordinate so we can check boundary 
		} while (isWater(coord) || !pt.within(boundary));
		
		geography.move(this, pt);	//move agent
	}

}


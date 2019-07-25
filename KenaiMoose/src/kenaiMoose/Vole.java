package kenaiMoose;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.gis.util.GeometryUtil;
import repast.simphony.random.RandomHelper;

public class Vole extends Host {
	private double vole_range;
	//protected InfectionZone smHost_buffer;
	Geometry vole_home;
	
	
	
	public Vole(String name) {
		super(name); //calling parent constructor 
		vole_range = 565; // circle with radius 565m = 1.003km^2
	}

	@Override
	@ScheduledMethod(start=0) //need to call before first tick in model
	public void init() {
		super.init(); 
		vole_home = addBuffer(vole_range); //create buffer (also adds to context)
		//create_home(vole_range); //same as addBuffer method...
	
	}
	
	@Override
	@ScheduledMethod(start=1, interval=1)
	public void step() {
		walk();
		List<Tick> tickList = getTicks();
		processInfections(tickList); //TODO change this, will move buffer
		
	}

	@Override
	//Vole needs it's own method here b/c the parent class method moves the geometry and we don't want it to move
	protected List<Tick> getTicks(){
		Envelope infection_envelope = vole_home.getEnvelopeInternal();
		
		Iterable<Tick> infectingTicks = geography.getObjectsWithin(infection_envelope, IxPacificus.class);
		List<Tick> tickList = new ArrayList();
		
		for (Tick tick : infectingTicks) {
			if(!tick.isAttached()) {
				tickList.add((Tick)tick);
			}
		}
		return tickList;
	}
	
	@Override
	protected void walk() {
		Coordinate coord;
		Point pt;
		//save location in case we need to revert back from an invalid move 
//		Coordinate prev_coord = getCoord();
//		Point prev_point = getPoint();
		
		
		
		do {
			List<Coordinate> test_coords = GeometryUtil.generateRandomPointsInPolygon(vole_home, 1);
			coord = test_coords.get(0);
			pt = geoFac.createPoint(coord); //Create a point based off the coordinate so we can check boundary 
		} while (isWater(coord) || !pt.within(boundary));
		
		geography.move(this, pt);	//move agent
	}

}


package kenaiMoose;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;

public class Tick {
	
	private String name;
	private Geometry boundary;
	private GeometryFactory geoFac = new GeometryFactory();
	private int latchCount = 0;
	private Moose ride;
	
	@ScheduledMethod(start = 1, interval = 1, priority = ScheduleParameters.FIRST_PRIORITY)
	public void step() {
		checkForVector();
	}
	// Logic checking for nearby vectors within specified range for latching
	public void checkForVector() {
		
	}
	// Logic for associating with vector
	public void latch() {
		
	}

}

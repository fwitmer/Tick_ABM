package kenaiMoose;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.gis.Geography;
import repast.simphony.util.ContextUtils;

public class Tick {
	
	private String name;
	private static Geometry boundary;
	private GeometryFactory geoFac = new GeometryFactory();
	private int latchCount = 0;
	private Moose ride;
	private Context context;
	private Geography<Tick> geography;
	
	public Tick(String name, Geometry boundary) {
		this.name = name;
		this.boundary = boundary;
		Context context = ContextUtils.getContext(this);
		Geography<Tick> geography = (Geography)context.getProjection("Kenai");
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = ScheduleParameters.FIRST_PRIORITY)
	public void step() {
		checkForVector();
	}
	
	public Geography getGeo() {
		return geography;
	}
	// Logic checking for nearby vectors within specified range for latching
	public void checkForVector() {
		
	}
	// Logic for associating with vector
	public void latch() {
		
	}
	// Logic for disassociating with vector
	public void detach() {
		
	}

}

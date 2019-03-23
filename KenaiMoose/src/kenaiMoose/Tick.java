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
	private GeometryFactory geoFac = new GeometryFactory();
	private int attach_count = 0;
	private Vector host_vector;
	
	public Tick(String name) {
		this.name = name;
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = ScheduleParameters.FIRST_PRIORITY)
	public void step() {
		checkForVector();
	}
	
	public Geography getGeo() {
		Context context = ContextUtils.getContext(this);
		Geography geography = (Geography)context.getProjection("Kenai");
		return geography;
	}
	
	// Logic checking for nearby vectors within specified range for latching
	public void checkForVector() {
		
	}
	
	// Logic for attaching to vector
	public void attach() {
		
	}
	
	// Logic for detaching from vector
	public void detach() {
		
	}

}

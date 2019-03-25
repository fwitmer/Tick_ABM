package kenaiMoose;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.gis.Geography;
import repast.simphony.util.ContextUtils;

public class Tick {
	
	private String name;
	private boolean attached;
	private GeometryFactory geoFac = new GeometryFactory();
	private int attach_count = 0;
	private final int ATTACH_LENGTH = 7;
	private Vector host_vector;
	
	public Tick(String name) {
		this.name = name;
	}
	
	@ScheduledMethod(start = 0)
	public void init() {
		attached = false;
		host_vector = null;
	}
	
	@ScheduledMethod(start = 1, interval = 1, priority = ScheduleParameters.LAST_PRIORITY)
	public void step() {
		if(attached) {
			if (attach_count >= ATTACH_LENGTH) {
				detach();
				return;
			}
			Coordinate newPosition = host_vector.getCoord();
			Point newPoint = geoFac.createPoint(newPosition);
			Geography geography = getGeo();
			geography.move(this, newPoint);
		}
		attach_count++;
	}
	
	public Geography getGeo() {
		Context context = ContextUtils.getContext(this);
		Geography geography = (Geography)context.getProjection("Kenai");
		return geography;
	}
	
	public boolean isAttached() {
		return attached;
	}
	
	
	// Logic for attaching to vector
	public void attach(Vector vector) {
		attached = true;
		host_vector = vector;
		System.out.println(name + " attached to " + host_vector.getName());
	}
	
	// Logic for detaching from vector
	public void detach() {
		attached = false;
		attach_count = 0;
		System.out.println(name + " detached from " + host_vector.getName());
		host_vector = null;
	}

}

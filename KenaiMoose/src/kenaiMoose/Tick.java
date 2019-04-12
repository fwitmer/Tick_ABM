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
	
	protected String name;
	protected Context context;
	protected Geography geography;
	protected boolean attached;
	protected GeometryFactory geoFac = new GeometryFactory();
	protected int attach_count = 0;
	protected final int ATTACH_LENGTH = 7;
	protected boolean delayed;
	protected int delay_count = 0;
	protected final int ATTACH_DELAY = 20;
	protected Vector host_vector;
	
	public Tick(String name) {
		this.name = name;
	}
	
	@ScheduledMethod(start = 0)
	public void init() {
		attached = false;
		host_vector = null;
		delayed = false;
		context = ContextUtils.getContext(this);
		geography = (Geography)context.getProjection("Kenai");
	}
	
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		if(attached) {
			if (attach_count >= ATTACH_LENGTH) {
				detach();
				return;
			}
			Coordinate newPosition = host_vector.getCoord();
			Point newPoint = geoFac.createPoint(newPosition);
			geography.move(this, newPoint);
			attach_count++;
		}
		else if(delayed) {
			if (delay_count >= ATTACH_DELAY) {
				delayed = false;
				return;
			}
			delay_count++;
		}
		
	}
	
	public Geography getGeo() {
		return geography;
	}
	
	public boolean isAttached() {
		return attached;
	}
	
	public boolean isDelayed() {
		return delayed;
	}
	
	
	// Logic for attaching to Vector
	public void attach(Vector vector) {
		if (!delayed) {
			attached = true;
			host_vector = vector;
			System.out.println(name + " attached to " + host_vector.getName());
		}
	}
	
	// Logic for detaching from Vector
	public void detach() {
		attached = false;
		delayed = true;
		delay_count = 0;
		attach_count = 0;
		host_vector.decreaseNumTicks();
		System.out.println(name + " detached from " + host_vector.getName());
		host_vector = null;
	}

}

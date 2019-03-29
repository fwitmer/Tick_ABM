package kenaiMoose;

import java.util.ArrayList;
import java.util.List;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.DirectPosition2D;
import org.opengis.geometry.DirectPosition;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.gis.util.GeometryUtil;
import repast.simphony.space.gis.Geography;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.collections.IndexedIterable;

public abstract class Vector {
	
	/* Private Variables
	 * 		name - String for simple names of Vector agents
	 * 		boundary - Geometry containing boundary file for agents to move within (STATIC)
	 * 		landuse_coverage - GridCoverage2D representing landuse raster data (STATIC)
	 * 		geofac - GeometryFactory for generating Geometry objects for use by Vector agents
	 * 		context - storage variable for obtaining the Context the Vector agent was added to
	 * 		geography - gets the Geography projection object for the Vector agent
	 */
	protected String name;
	protected boolean is_infected;
	protected static Geometry boundary;
	protected GeometryFactory geoFac = new GeometryFactory();
	protected double infection_radius;
	protected Envelope infection_area;
	protected InfectionZone infection_zone;
	protected int num_infecting_ticks;
	
	public Vector() {
		this.name = "No name";
		this.boundary = null;	
		this.num_infecting_ticks = 0;
		this.is_infected = false;
	}
	
	// Overloaded constructor for passing in name, boundary Geometry, and landuse_coverage GridCoverage2D
	public Vector(String name, Geometry boundary) {
		this.name = name;
		this.boundary = boundary;
		this.num_infecting_ticks = 0;
		this.is_infected = false;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isInfected() {
		return is_infected;
	}
	
	public void setInfected(boolean infected) {
		this.is_infected = infected;
	}
	
	public int getNumTicks() {
		return num_infecting_ticks;
	}
	
	public void decreaseNumTicks () {
		if (num_infecting_ticks > 0) {
			num_infecting_ticks--;
			return;
		}
		else {
			System.out.println(name + ": Tried to decrement number of ticks below 0!");
		}
	}
	
	// TODO: See if possible to access individual elements added to Context to find boundary
	//		 without having to pass in through constructor
	// *** CURRENTLY DOES NOTHING! ***
	private Geometry getBoundary() {
		Context context = ContextUtils.getContext(this);
		Geography geography = (Geography)context.getProjection("Kenai");
		IndexedIterable objects = context.getObjects(Geometry.class);
		return null;
	}
	
	// Return Coordinate of Vector agent used for attaching other agents
	public Coordinate getCoord() {
		Context context = ContextUtils.getContext(this);
		Geography geography = (Geography)context.getProjection("Kenai");
		return geography.getGeometry(this).getCoordinate();
	}
	
	// Return Geography of Vector agent
	public Geography getGeo() {
		Context context = ContextUtils.getContext(this);
		return (Geography)context.getProjection("Kenai");
	}
	
	// Generate InfectionZone agent around Vector
	protected void addBuffer(double infection_radius) {
		Context context = ContextUtils.getContext(this);
		Geography geography = (Geography)context.getProjection("Kenai");
		Geometry infection_buffer = GeometryUtil.generateBuffer(geography, geography.getGeometry(this), infection_radius);
		Geometry infection_geom = geoFac.createGeometry(infection_buffer);
		infection_zone = new InfectionZone();
		context.add(infection_zone);
		geography.move(infection_zone, infection_geom);
	}
	
	// Return List of Ticks found within infection_area Envelope
	protected List<Tick> getTicks() {
		Context context = ContextUtils.getContext(this);
		Geography geography = (Geography)context.getProjection("Kenai");
		Geometry infection_buffer = GeometryUtil.generateBuffer(geography, geography.getGeometry(this), infection_radius);
		Geometry infection_geom = geoFac.createGeometry(infection_buffer);
		Envelope infection_envelope = infection_geom.getEnvelopeInternal();
		
		Iterable<Tick> infectingTicks = geography.getObjectsWithin(infection_envelope, Tick.class);
		List<Tick> tickList = new ArrayList();
		
		for (Tick tick : infectingTicks) {
			if(!tick.isAttached()) {
				tickList.add((Tick)tick);
			}
		}
		return tickList;
	}
	
	// Check to see if area at coordinate is water in NLCD landuse raster
	public boolean isWater(Coordinate coord) {
		Context context = ContextUtils.getContext(this);
		Geography geography = (Geography)context.getProjection("Kenai");
		GridCoverage2D landuse_coverage = geography.getCoverage("NLCD Landuse");
		// Create DirectPosition used to evaluate GridCoverage2D
		DirectPosition position = new DirectPosition2D(geography.getCRS(), coord.x, coord.y);
		int[] sample = (int[]) landuse_coverage.evaluate(position);
		if (sample[0] == 11 || sample[0] == 12)
			return true;
		else
			return false;
	}
	
	// Move the associated InfectionZone to new Vector agent location after moving
	protected void updateInfectionZone() {
		Context context = ContextUtils.getContext(this);
		Geography geography = (Geography)context.getProjection("Kenai");
		
		Geometry infection_buffer = GeometryUtil.generateBuffer(geography, geography.getGeometry(this), infection_radius);
		Geometry infection_geom = geoFac.createGeometry(infection_buffer);
		
		geography.move(infection_zone, infection_geom);
	}
	
	protected void processInfections(List<Tick> tickList) {
		if (tickList.size() > 0) {
			is_infected = true;
			for (Tick tick : tickList) {
				tick.attach(this);
				num_infecting_ticks++;
			}
		}
		if (num_infecting_ticks > 0) {
			infection_zone.setInfected(true);
		}
		else if (num_infecting_ticks == 0) {
			infection_zone.setInfected(false);
		}
	}
	
	// Method to control actions performed in each step
	@ScheduledMethod(start = 1, interval = 1, priority = ScheduleParameters.FIRST_PRIORITY)
	public abstract void step();
	
	// Method to be defined on how the vector will walk at each step
	protected abstract void walk();
	

}

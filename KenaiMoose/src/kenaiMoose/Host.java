
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

public abstract class Host {
	
	/* Protected Variables
	 * 		name - String for simple names of Host agents
	 * 		landuse_coverage - GridCoverage2D representing landuse raster data (STATIC)
	 * 		geofac - GeometryFactory for generating Geometry objects for use by Host agents
	 * 		context - storage variable for obtaining the Context the Host agent was added to
	 * 		geography - gets the Geography projection object for the Host agent
	 */
	
	protected Context context;
	protected Geography geography;
	protected GeometryFactory geoFac = new GeometryFactory();
	
	protected String name;
	protected boolean is_infected;
	protected static Geometry boundary;
	protected double infection_radius;
	protected Envelope infection_area;
	protected InfectionZone infection_zone;
	protected int num_infecting_ticks;
	protected ArrayList<Tick> tick_list;
	
	public Host() {
		this.name = "No name";	
		this.num_infecting_ticks = 0;
		this.is_infected = false;
	}
	
	// Giving each Host a unique name for identification/tracking purposes
	public Host(String name) {
		this.name = name;
		this.num_infecting_ticks = 0;
		this.is_infected = false;
		tick_list = new ArrayList<Tick>();
	}
	
	@ScheduledMethod(start = 0)
	public void init() {
		context = ContextUtils.getContext(this);
		geography = (Geography)context.getProjection("Kenai");
	}
	
	public static void setBoundary(Geometry boundary) {
		Host.boundary = boundary;
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
		return tick_list.size();
	}
	
	public ArrayList<Tick> get_ticks() {
		return tick_list;
	}
	
	public void add_tick(Tick tick) {
		tick_list.add(tick);
	}
	
	public void remove_tick(Tick tick) {
		tick_list.remove(tick);
	}
	
	
	// TODO: See if possible to access individual elements added to Context to find boundary
	//		 without having to pass in through constructor
	// *** CURRENTLY DOES NOTHING! ***
	private Geometry getBoundary() {
		IndexedIterable objects = context.getObjects(Geometry.class);
		return null;
	}
	
	// Return Coordinate of Host agent used for attaching other agents
	public Coordinate getCoord() {
		Coordinate coord = new Coordinate(geography.getGeometry(this).getCoordinate());
		return coord;
	}
	
	public Point getPoint() {
		Point point = geoFac.createPoint(getCoord());
		return point;
	}
	
	// Return Geography of Host agent
	public Geography getGeo() {
		return geography;
	}
	
	// Generate InfectionZone agent around Host
	protected Geometry addBuffer(double infection_radius) {
		Geometry infection_buffer = GeometryUtil.generateBuffer(geography, geography.getGeometry(this), infection_radius);
		Geometry infection_geom = geoFac.createGeometry(infection_buffer);
		infection_zone = new InfectionZone();
		context.add(infection_zone);
		geography.move(infection_zone, infection_geom);
		
		return infection_geom;
	}
	
	// Return List of Ticks found within infection_area Envelope
	protected List<Tick> getTicks() {
		Geometry infection_buffer = GeometryUtil.generateBuffer(geography, geography.getGeometry(this), infection_radius);
		Geometry infection_geom = geoFac.createGeometry(infection_buffer);
		Envelope infection_envelope = infection_geom.getEnvelopeInternal();
		
		Iterable<Tick> infectingTicks = geography.getObjectsWithin(infection_envelope, IxPacificus.class);
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
		GridCoverage2D landuse_coverage = geography.getCoverage("NLCD Landuse");
		// Create DirectPosition used to evaluate GridCoverage2D
		DirectPosition position = new DirectPosition2D(geography.getCRS(), coord.x, coord.y);
		int[] sample = (int[]) landuse_coverage.evaluate(position);
		if (sample[0] == 11 || sample[0] == 12)
			return true;
		else
			return false;
	}
	
	// Move the associated InfectionZone to new Host agent location after moving
	protected void updateInfectionZone() {
		Geometry infection_buffer = GeometryUtil.generateBuffer(geography, geography.getGeometry(this), infection_radius);
		Geometry infection_geom = geoFac.createGeometry(infection_buffer);
		
		geography.move(infection_zone, infection_geom);
	}
	
	protected void processInfections(List<Tick> tickList) {
		if (tickList.size() > 0) {
			for (Tick tick : tickList) {
				if(tick.attach(this))
					num_infecting_ticks++;
			}
		}
		// Update color of InfectionZone based on infections
		if (num_infecting_ticks > 0) {
			is_infected = true;
			infection_zone.setInfected(true);
		}
		else if (num_infecting_ticks == 0) {
			is_infected = false;
			infection_zone.setInfected(false);
		}
	}
	
	// Method to control actions performed in each step
	public abstract void step();
	
	// Method to be defined on how the Host will walk at each step
	protected abstract void walk();
	

}


package kenaiMoose;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.DirectPosition2D;
import org.opengis.geometry.DirectPosition;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
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
	protected boolean isInfected = false;
	protected static Geometry boundary;
	protected static GridCoverage2D landuse_coverage;
	protected GeometryFactory geoFac = new GeometryFactory();
	
	public Vector() {
		name = "";
		boundary = null;
		landuse_coverage = null;	
	}
	
	// Overloaded constructor for passing in name, boundary Geometry, and landuse_coverage GridCoverage2D
	public Vector(String name, Geometry boundary, GridCoverage2D landuse_coverage) {
		this.name = name;
		this.boundary = boundary;
		this.landuse_coverage = landuse_coverage;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	// TODO: See if possible to access individual elements added to Context to find boundary
	//		 without having to pass in through constructor
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
	public Geography<Vector> getGeo() {
		Context context = ContextUtils.getContext(this);
		Geography geography = (Geography)context.getProjection("Kenai");
		return geography;
	}
	
	// Check to see if area at coordinate is water in NLCD landuse raster
	public boolean isWater(Coordinate coord) {
		Context context = ContextUtils.getContext(this);
		Geography geography = (Geography)context.getProjection("Kenai");
		// Create DirectPosition used to evaluate GridCoverage2D
		DirectPosition position = new DirectPosition2D(geography.getCRS(), coord.x, coord.y);
		int[] sample = (int[]) landuse_coverage.evaluate(position);
		if (sample[0] == 11 || sample[0] == 12)
			return true;
		else
			return false;
	}
	
	// Method to control actions performed in each step
	@ScheduledMethod(start = 1, interval = 1, priority = ScheduleParameters.FIRST_PRIORITY)
	public abstract void step();
	
	// Method to be defined on how the vector will walk at each step
	protected abstract void walk();
	

}

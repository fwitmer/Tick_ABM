package kenaiMoose;

import org.geotools.coverage.grid.GridCoverage2D;

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
	private String name;
	private static Geometry boundary;
	private static GridCoverage2D landuse_coverage;
	private GeometryFactory geoFac = new GeometryFactory();
	private Context context;
	private Geography<Vector> geography;
	
	public Vector() {
		name = "";
		boundary = null;
		landuse_coverage = null;
		context = ContextUtils.getContext(this);
		geography = (Geography)context.getProjection("Kenai");	
	}
	
	// Overloaded constructor for passing in name, boundary Geometry, and landuse_coverage GridCoverage2D
	public Vector(String name, Geometry boundary, GridCoverage2D landuse_coverage) {
		this.name = name;
		this.boundary = boundary;
		this.landuse_coverage = landuse_coverage;
		context = ContextUtils.getContext(this);
		geography = (Geography)context.getProjection("Kenai");
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
		IndexedIterable objects = context.getObjects(Geometry.class);
		return null;
	}
	
	// Return Coordinate of Vector agent used for attaching other agents
	public Coordinate getCoord() {
		return geography.getGeometry(this).getCoordinate();
	}
	
	// Return Geography of Vector agent
	public Geography<Vector> getGeo() {
		return geography;
	}
	
	// Method to control actions performed in each step
	@ScheduledMethod(start = 1, interval = 1, priority = ScheduleParameters.FIRST_PRIORITY)
	public abstract void step();

}

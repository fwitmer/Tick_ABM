package kenaiMoose;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.DirectPosition2D;
import org.opengis.geometry.DirectPosition;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.gis.Geography;
import repast.simphony.util.ContextUtils;

public class Moose {
	
	private String name;
	private static Geometry boundary;
	private static GridCoverage2D landuse_coverage;
	private GeometryFactory geofac = new GeometryFactory();
	private boolean isInfected = false;
	private Context context;
	private Geography<Moose> geography;
	// TODO create and utilize energy value
	
	public Moose(String name, Geometry boundary, GridCoverage2D landuse_coverage) {
		this.name = name;
		this.boundary = boundary;
		this.landuse_coverage = landuse_coverage;
		Context context = ContextUtils.getContext(this);
		Geography<Moose> geography = (Geography)context.getProjection("Kenai");
	}

	// Establishing random moves for Moose agents
	@ScheduledMethod(start = 1, interval = 1, priority = ScheduleParameters.FIRST_PRIORITY)
	public void step() {
		randomWalk();
	}
		
	// Logic for checking for proper bounds and raster data for each step
	public void randomWalk() {		
		Coordinate prevLocation = geography.getGeometry(this).getCoordinate(); // Saving previous location to revert back to if out of bounds
		
		// Attempting to create new random Coordinate and Point from previous location
		Coordinate coord = new Coordinate(prevLocation.x += RandomHelper.nextDoubleFromTo(-0.0005, 0.0005), 
				prevLocation.y += RandomHelper.nextDoubleFromTo(-0.0005, 0.0005));
		Point newPoint = geofac.createPoint(coord);
		
		// Ensuring within bounds and not in water
		while (!newPoint.within(boundary) || isWater(coord)) {
			coord.x = prevLocation.x + RandomHelper.nextDoubleFromTo(-0.0005, 0.0005);
			coord.y = prevLocation.y + RandomHelper.nextDoubleFromTo(-0.0005, 0.0005);
			newPoint = geofac.createPoint(coord);
		}
		
		
		// Updating Moose location
		geography.move(this, newPoint);
	}
	
	public String getName() {
		return name;
	}
	
	public Geography getGeo() {
		return geography;
	}
	
	private boolean isWater(Coordinate coord) {
		DirectPosition position = new DirectPosition2D(geography.getCRS(), coord.x, coord.y);
		int[] sample = (int[]) landuse_coverage.evaluate(position);
		if (sample[0] == 11 || sample[0] == 12)
			return true;
		else
			return false;
		
	}
}

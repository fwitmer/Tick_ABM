package kenaiMoose;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.formula.functions.T;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.GridFormatFinder;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geometry.DirectPosition2D;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.DirectPosition;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import repast.simphony.context.Context;
import repast.simphony.context.space.gis.GeographyFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.essentials.RepastEssentials;
import repast.simphony.gis.util.GeometryUtil;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.gis.GeographyParameters;
import repast.simphony.space.graph.Network;

public class ContextBuilder implements repast.simphony.dataLoader.ContextBuilder<T> {
	int numMoose = 100;
	int numTicks = 1000;
	int numVoles = 100;
	
	public Context build(Context context) {
		System.setProperty("org.geotools.referencing.forceXY", "true"); // suppress warnings caused by the visualized environment
		RunEnvironment.getInstance().endAt(1825); // scheduling runs to end after 5 years worth of ticks
		RunEnvironment.getInstance().getCurrentSchedule().getTickCount(); // use to get run's current tick count
		RepastEssentials.GetTickCount(); // another method of getting tick count
		// Creating Geography projection for Moose vectors
		GeographyParameters geoParams = new GeographyParameters();
		geoParams.setCrs("EPSG:4269"); // Setting NAD83 GCS (GCS of 3338 Alaska Albers PCS)
		Geography geography = GeographyFactoryFinder.createGeographyFactory(null).createGeography("Kenai", context, geoParams);
		//geography.setCRS("EPSG:4269"); // Alternate method of setting CRS of projection
		
		// Placeholder for infection Network
		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object>("infection network", context, true);
		Network network = netBuilder.buildNetwork();
		
		// Geometry factory
		GeometryFactory geoFac = new GeometryFactory();
		
		// Establishing Kenai boundary area from shapefile
		String boundaryFile = "./data/KenaiWatershed3D_projected.shp";
		List<SimpleFeature> features = loadFeaturesFromShapefile(boundaryFile);
		Geometry boundary = (MultiPolygon)features.iterator().next().getDefaultGeometry();
		
		// Creating random coords in Kenai boundary
		List<Coordinate> mooseCoords = GeometryUtil.generateRandomPointsInPolygon(boundary, numMoose);
		List<Coordinate> tickCoords = GeometryUtil.generateRandomPointsInPolygon(boundary, numTicks);
		List<Coordinate> voleCoords = GeometryUtil.generateRandomPointsInPolygon(boundary, numVoles);
		
		GridCoverage2D elev_coverage = null;
		GridCoverage2D landuse_coverage = null;
		
		// Try to load raster data and add as Coverage layer to geography projection
	try {
		elev_coverage = loadRaster("./data/CLIP_Alaska_NationalElevationDataset_60m_AKALB.tif", context);
		landuse_coverage = loadRaster("./data/nlcd_GCS_NAD83.tif", context);
		geography.addCoverage("AKALB Elevation", elev_coverage);
		geography.addCoverage("NLCD Landuse", landuse_coverage);
	} 
	catch (IOException e) {
		System.out.println("Error loading raster.");
	}
	
		/* // example of how RasterLayer would work if supported by context
		File file = new File(".data/nlcd_GCS_NAD83.tif");
		RasterLayer landuse_raster = new RasterLayer("NLCD Landuse", file);
		context.addValueLayer(landuse_raster); // Doens't currently support RasterLayer
		
		int landuse_sample = landuse_raster.getIntegerGridValue(x, y); 
		*/
		
		// Create Moose agents
			// Parameters params = RunEnvironment.getInstance().getParameters(); // get RunEnvironment specified params
			// int mooseCount = (Integer) params.getValue("moose_count"); // establish max Moose count from RunEnvironment
		System.out.println("Creating " + numMoose + " Moose agents...");
		int cnt = 0;
		for (Coordinate coord : mooseCoords) {
			Moose moose = new Moose("Moose " + cnt);
			context.add(moose);
			
			DirectPosition position = new DirectPosition2D(geography.getCRS(), coord.x, coord.y);
	        int[] sample = (int[]) landuse_coverage.evaluate(position);
	        sample = landuse_coverage.evaluate(position, sample);
			
	        // Checking for creation in water - if in water, keep creating new Coordinates until one is found not in water
	        while (sample[0] == 11 || sample[0] == 12) {
	        	List<Coordinate> new_coord = GeometryUtil.generateRandomPointsInPolygon(boundary, 1);
	        	coord = new_coord.get(0);
        		position = new DirectPosition2D(geography.getCRS(), coord.x, coord.y);
        		sample = landuse_coverage.evaluate(position, sample);
	        }	
			Point pnt = geoFac.createPoint(coord); // Create the Point geometry for Moose agent
			System.out.println("	Moose created at: " + coord.toString());
			System.out.println("		Landuse value: " + sample[0]);
			geography.move(moose, pnt); // Moving Moose agent to Point
			cnt++;
		}
		Host.setBoundary(boundary);
		System.out.println(cnt + " Moose agents created.");
		
		// Create Tick agents
			// Parameters params = RunEnvironment.getInstance().getParameters(); // get RunEnvironment specified params
			// int mooseCount = (Integer) params.getValue("moose_count"); // establish max Moose count from RunEnvironment
		cnt = 0;
		System.out.println();
		System.out.println("Creating " + numTicks + " Tick agents...");
		for (Coordinate coord : tickCoords) {
			IxPacificus tick = new IxPacificus("Tick " + cnt);
			context.add(tick);
			
			// Preparing to check for creation in water
			DirectPosition position = new DirectPosition2D(geography.getCRS(), coord.x, coord.y);
	        int[] sample = (int[]) landuse_coverage.evaluate(position);
	        sample = landuse_coverage.evaluate(position, sample);
			
	        // Checking for creation in water - if in water, keep creating new Coordinates until one is found not in water
	        while (sample[0] == 11 || sample[0] == 12) {
	        	List<Coordinate> new_coord = GeometryUtil.generateRandomPointsInPolygon(boundary, 1);
	        	coord = new_coord.get(0);
        		position = new DirectPosition2D(geography.getCRS(), coord.x, coord.y);
        		sample = landuse_coverage.evaluate(position, sample);
	        }	
			Point pnt = geoFac.createPoint(coord);
			System.out.println("	Tick created at: " + coord.toString());
			System.out.println("		Landuse value: " + sample[0]);
			geography.move(tick, pnt);
			cnt++;
		}
		System.out.println(cnt + " Tick agents created.");
		
		//creating SmHost agents 
		cnt = 0;
		for (Coordinate coord : voleCoords) {
			SmHost smHost = new SmHost("SmHost" + cnt);
			context.add(smHost); //add each new agent to the context
			
			// Preparing to check for creation in water
			DirectPosition position = new DirectPosition2D(geography.getCRS(), coord.x, coord.y);
	        int[] sample = (int[]) landuse_coverage.evaluate(position);
	        sample = landuse_coverage.evaluate(position, sample);
			
	        // Checking for creation in water - if in water, keep creating new Coordinates until one is found not in water
	        while (sample[0] == 11 || sample[0] == 12) {
	        	List<Coordinate> new_coord = GeometryUtil.generateRandomPointsInPolygon(boundary, 1);
	        	coord = new_coord.get(0);
        		position = new DirectPosition2D(geography.getCRS(), coord.x, coord.y);
        		sample = landuse_coverage.evaluate(position, sample);
	        }	
			
	      //making point geometry for agent and moving it there
			Point pnt = geoFac.createPoint(coord); 
			geography.move(smHost, pnt);
			cnt++;
			
			//TODO set boundary here? Same as moose boundary, what about buffer as a boundary?
		}
		

		
		// Loading shapefile features for visualization
		loadFeatures("data/KenaiWatershed3D_projected.shp", context, geography);
		
		
		return context;
	}
	
	// Load GeoTiff rasters and convert to a 2DGridCoverage to be returned
	// NOTE: May work with other filetypes with varying effect
	//       Rasters must contain appropriate worldfile for proper positioning in geography projection
	private GridCoverage2D loadRaster(String filename, Context context) throws IOException {
		File file = new File(filename);
		AbstractGridFormat format = GridFormatFinder.findFormat(file);
		GridCoverage2DReader reader = format.getReader(file);
		
		// Storing raster data as a GridCoverage2D object, adding it to Context, and returning object
		if (reader != null) {
			GridCoverage2D coverage = reader.read(null);
			context.add(reader);
			context.add(coverage);
			return coverage;
		}
		else {
			throw new IOException("No reader.");
		}
		
	}
	
	// Get features from a shapefile as a List for use in GIS logic
	// Inputs:
    //		filename - String representation of shapefile filename
	// Outputs:
	// 		List of SimpleFeatures containing each feature found in the shapefile
	private List<SimpleFeature> loadFeaturesFromShapefile(String filename) {
		
		// Establish filepath
		URL url = null;	
		try {
			url = new File(filename).toURL();
		} catch(MalformedURLException e1) {
			e1.printStackTrace();
		}
		
		List<SimpleFeature> features = new ArrayList<SimpleFeature>();
		
		// Attempt to load shapefile
		SimpleFeatureIterator featureIter = null;
		ShapefileDataStore store = null;
		store = new ShapefileDataStore(url);
		
		try {
			featureIter = store.getFeatureSource().getFeatures().features();
			
			while(featureIter.hasNext()) {
				features.add(featureIter.next());
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
		
		// Tidying up
		finally {
			featureIter.close();
			store.dispose();
		}
		
		// Returning features found in shapefile
		return features;
	}
	
	// Load features from a shapefile and establish them as an agent for GIS 3D visualization
	// Inputs:
    // 		filename - String for associated shapefile filename
	//		context - Context layer created further up in build()
    // Outputs:
    //      Nothing returned - adds BoundaryZone agents for each polygon found in shapefile to Geography and Context
	private void loadFeatures (String filename, Context context, Geography geography) {
		List<SimpleFeature> features = loadFeaturesFromShapefile(filename);
		
		for (SimpleFeature feature : features) {
			Geometry geom = (Geometry)feature.getDefaultGeometry();
			BoundaryZone boundary_zone = null;
			
			if (!geom.isValid()) {
				System.out.println("Invalid geometry: " + feature.getID());
			}
			
			if (geom instanceof Polygon) {
				System.out.println("Feature found! Polygon: " + feature.getID()); // Console output to confirm feature class
				Polygon p = (Polygon)feature.getDefaultGeometry();
				geom = (Polygon)p.getGeometryN(0);
				
				String name = (String)feature.getAttribute("name");
				/*
				agent = new BoundaryZone();
				Geometry buffer = GeometryUtil.generateBuffer(geography, geom, 100);
				context.add(agent);
				context.add(buffer);
				*/
			}
			
			// Got a MultiPolygon, get each Polygon contained and add it as a BoundaryZone
			if (geom instanceof MultiPolygon) {
				System.out.println("Feature found! MultiPolygon: " + feature.getID()); // Console output to confirm feature class
				MultiPolygon mp = (MultiPolygon)feature.getDefaultGeometry();
				for (int i = 0; i < mp.getNumGeometries(); i++) {
					geom = (Polygon)mp.getGeometryN(i);
					boundary_zone = new BoundaryZone();
					context.add(boundary_zone);
					geography.move(boundary_zone, geom);
				}
				//geom = (Polygon)mp.getGeometryN(0);
				
				//boundary_zone = new BoundaryZone();
				
				//Geometry buffer = GeometryUtil.generateBuffer(geography, geom, 100);
				//context.add(boundary_zone);
				//geography.move(boundary_zone, buffer);
				
			}
			// Reporting feature class found if none of the above
			else {
				System.out.println("Geometry found is: " + feature.getDefaultGeometry());
			}
		}
	}
	
	
}

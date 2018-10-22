package kenaiMoose;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.formula.functions.T;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import repast.simphony.context.Context;
import repast.simphony.context.space.gis.GeographyFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.gis.util.GeometryUtil;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.gis.GeographyParameters;
import repast.simphony.space.graph.Network;

public class ContextBuilder implements repast.simphony.dataLoader.ContextBuilder<T> {
	int numMoose = 10;
	
	public Context build(Context context) {
		
		// Creating Geography projection for Moose vectors
		GeographyParameters geoParams = new GeographyParameters();
		Geography geography = GeographyFactoryFinder.createGeographyFactory(null).createGeography("Kenai", context, geoParams);
		
		// Placeholder for infection Network
		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object>("infection network", context, true);
		Network network = netBuilder.buildNetwork();
		
		// Geometry factory
		GeometryFactory geoFac = new GeometryFactory();
		
		// Establishing Kenai boundary area from shapefile
		String boundaryFile = "./data/SCTC_watersheds.shp";
		List<SimpleFeature> features = loadFeaturesFromShapefile(boundaryFile);
		Geometry boundary = (MultiPolygon)features.iterator().next().getDefaultGeometry();
		
		// Creating random coords in Kenai boundary
		List<Coordinate> mooseCoords = GeometryUtil.generateRandomPointsInPolygon(boundary, numMoose);
		
		// Create Moose agents
			// Parameters params = RunEnvironment.getInstance().getParameters(); // get RunEnvironment specified params
			// int mooseCount = (Integer) params.getValue("moose_count"); // establish max Moose count from RunEnvironment
		int cnt = 0;
		for (Coordinate coord : mooseCoords) {
			Moose moose = new Moose("Moose " + cnt);
			context.add(moose);
			
			Point pnt = geoFac.createPoint(coord);
			geography.move(moose, pnt);
			cnt++;
		}
		
		loadFeatures("data/SCTC_watersheds.shp", context, geography);
		return context;
	}
	
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
	
	private void loadFeatures (String filename, Context context, Geography geography) {
		List<SimpleFeature> features = loadFeaturesFromShapefile(filename);
		
		for (SimpleFeature feature : features) {
			Geometry geom = (Geometry)feature.getDefaultGeometry();
			Object agent = null;
			
			if (!geom.isValid()) {
				System.out.println("Invalid geometry: " + feature.getID());
			}
			
			if (geom instanceof Polygon) {
				Polygon p = (Polygon)feature.getDefaultGeometry();
				geom = (Polygon)p.getGeometryN(0);
				
				String name = (String)feature.getAttribute("name");
				
				agent = new BoundaryZone(name);
			}
		}
	}
	
	
}

package kenaiMoose;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.formula.functions.T;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.GridCoverage2DReader;
import org.geotools.coverage.grid.io.GridFormatFinder;
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
import repast.simphony.space.grid.Grid;

public class ContextBuilder implements repast.simphony.dataLoader.ContextBuilder<T> {
	int numMoose = 10;
	
	public Context build(Context context) {
		
		// Creating Geography projection for Moose vectors
		GeographyParameters geoParams = new GeographyParameters();
		geoParams.setCrs("EPSG:4269"); // Setting NAD83 GCS (GCS of 3338 Alaska Albers PCS)
		Geography geography = GeographyFactoryFinder.createGeographyFactory(null).createGeography("Kenai", context, geoParams);
		System.out.println(geography.getCRS());
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
		
		// Create Moose agents
			// Parameters params = RunEnvironment.getInstance().getParameters(); // get RunEnvironment specified params
			// int mooseCount = (Integer) params.getValue("moose_count"); // establish max Moose count from RunEnvironment
		int cnt = 0;
		for (Coordinate coord : mooseCoords) {
			System.out.println(coord.toString());
			Moose moose = new Moose("Moose " + cnt, boundary);
			context.add(moose);
			
			Point pnt = geoFac.createPoint(coord);
			geography.move(moose, pnt);
			cnt++;
		}
		
		// Loading shapefile features for visualization
		loadFeatures("data/KenaiWatershed3D_projected.shp", context, geography);
		
		try {
			GridCoverage2D elev_coverage = loadRaster("./data/CLIP_Alaska_NationalElevationDataset_60m_AKALB.tif", context);
			GridCoverage2D landuse_coverage = loadRaster("./data/ak_nlcd_2011_landcover_CLIP.tif", context);
		} catch (IOException e) {
			System.out.println("Error loading raster.");
		}
		
		
		return context;
	}
	
	private GridCoverage2D loadRaster(String filename, Context context) throws IOException {
		File file = new File(filename);
		AbstractGridFormat format = GridFormatFinder.findFormat(file);
		GridCoverage2DReader reader = format.getReader(file);
		
		// Storing raster data as a GridCoverage2D object, adding it to Context, and returning object
		if (reader != null) {
			GridCoverage2D coverage = reader.read(null);
			//GridSampleDimension[] gsd = coverage.getSampleDimensions();
			//System.out.println(gsd.length);
			context.add(reader);
			context.add(coverage);
			return coverage;
		}
		else {
			throw new IOException("No reader.");
		}
		
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
				System.out.println("Feature found! Polygon: " + feature.getID()); // Console output to confirm feature class
				Polygon p = (Polygon)feature.getDefaultGeometry();
				geom = (Polygon)p.getGeometryN(0);
				
				String name = (String)feature.getAttribute("name");
				
				agent = new BoundaryZone(name);
				Geometry buffer = GeometryUtil.generateBuffer(geography, geom, 100);
				context.add(agent);
				context.add(buffer);
			}
			
			if (geom instanceof MultiPolygon) {
				System.out.println("Feature found! MultiPolygon: " + feature.getID()); // Console output to confirm feature class
				MultiPolygon mp = (MultiPolygon)feature.getDefaultGeometry();
				geom = (Polygon)mp.getGeometryN(0);
				
				String name = (String)feature.getAttribute("name");
				
				agent = new BoundaryZone(name);
				
				Geometry buffer = GeometryUtil.generateBuffer(geography, geom, 100);
				context.add(agent);
				geography.move(agent, buffer);
			}
			// Reporting feature class found if none of the above
			else {
				System.out.println("Geometry found is: " + feature.getDefaultGeometry());
			}
		}
	}
	
	
}

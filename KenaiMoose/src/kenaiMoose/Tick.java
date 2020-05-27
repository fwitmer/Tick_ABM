package kenaiMoose;

import java.util.Random;

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
import repast.simphony.space.gis.Geography;
import repast.simphony.util.ContextUtils;

public abstract class Tick {
	
	// variables for holding the framework objects
	protected static Context context;
	protected static Geography geography;
	protected GeometryFactory geoFac = new GeometryFactory();
	protected static GridCoverage2D suitability_raster;
	
	
	// variables for behavioral functions
	protected String name;
	protected boolean attached;
	protected int attach_count;
	protected int attach_length; // must be defined by derived class
	protected int attach_delay; // must be defined by derived class
	protected Host host;
	protected static double habitat_sample;
	
	// life cycle variables
	protected static String START_LIFE_CYCLE; // static variable for defining what stage Ticks should start at during init
	protected boolean female;  // true if tick is female
	protected boolean laying_eggs;
	protected int eggs_remaining;
	protected int child_count;
	protected String life_stage; // holder state in life stage
	protected static int EGG_LENGTH; // average length of time before egg hatches
	protected static int LARVA_LENGTH; // average length of time before larval mortality
	protected static int LARVA_FEED_LENGTH; // larval length of attachment for feeding
	protected static int NYMPH_LENGTH; // average length of time before nympth mortality
	protected static int NYMPH_FEED_LENGTH; // nymphal length of attachment for feeding
	protected static int ADULT_LENGTH; // average length of time before adult mortality
	protected static int ADULT_FEED_LENGTH; // adult length of attachment for feeding (females only)
	protected static int EGG_COUNT;
	protected int lifecycle_counter; // basic counter used to count steps in all stages of lifecycle behaviors
	protected boolean has_fed; // marker for whether or not tick has successfully fed at current life stage
	
	// mating behaviors are species specific and should be implemented individually in the child classes
	protected abstract void mate();
	// Abstract methods to force setting ATTACH_LENGTH specific to species
	protected abstract void set_attach_length(String lifecycle);
	
	public Tick(String name) {
		this.name = name;
		determine_sex();
		lifecycle_counter = 0;
		attach_count = 0;
		child_count = 0;
		attached = false;
		host = null;
		has_fed = false;
		laying_eggs = false;
		life_stage = START_LIFE_CYCLE;
	}
	
	// additional constructor for defining life stage
	public Tick(String name, String life_stage) {
		this.name = name;
		this.life_stage = life_stage;
		determine_sex();
		lifecycle_counter = 0;
		attach_count = 0;
		child_count = 0;
		attached = false;
		host = null;
		has_fed = false;
		laying_eggs = false;
	}
	// initial method for establishing the context and geography static variables for the class
	@ScheduledMethod(start = 0)
	public void init() {
		context = ContextUtils.getContext(this);
		geography = (Geography)context.getProjection("Kenai");
		System.out.println(this.name + " habitat sample: " + habitat_sample());
	}
	
	// set the static suitability raster layer for the class
	public static void setSuitability(GridCoverage2D raster) {
		suitability_raster = raster;
		return;
	}
	
	public boolean is_laying_eggs() {
		return laying_eggs;
	}
	
	// returns the Coordinate for the current position of the agent
	public Coordinate getCoord() {
		Geometry geo_geom = geography.getGeometry(this);
		System.out.println("\t Geometry: " + geo_geom.toString());
		Coordinate geo_coord = geo_geom.getCoordinate();
		System.out.println("\t Coordinate: " + geo_coord.x + "," + geo_coord.y);
		return geo_coord;
		//return new Coordinate(geography.getGeometry(this).getCoordinate());
	}
	
	// Get lat and long for data sets
	public double getLong() {
		System.out.println(name + " getting Coordinate for longitude (reporting):");
		Coordinate coord = getCoord();
		return coord.x;
	}
	public double getLat() {
		System.out.println(name + " getting Coordinate for latitude (reporting):");
		Coordinate coord = getCoord();
		return coord.y;
	}
	
	public String getLifestate() {
		return this.life_stage;
	}
	
	public Geography getGeo() {
		return geography;
	}
	
	public boolean isAttached() {
		return attached;
	}
	
	public boolean isFemale() {
		return female;
	}
	
	public Host getHost() {
		return host;
	}
	
	
	// primary method for Tick agents, executed every step
	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		// if Tick is attached, update position to Host's new position
		if(attached) {
			Coordinate newPosition = host.getCoord();
			Point newPoint = geoFac.createPoint(newPosition);
			geography.move(this, newPoint);
			// Tick has been riding Host for specified amount of time
			// 	 (been attached long enough)  &&    (not an adult) - adult detachment behavior handled in lifecycle()
			if (attach_count >= attach_length && !life_stage.equals("adult") ) {
				detach();
			}
			else
				attach_count++;
		}
		lifecycle();
		
	}
	
	// this method runs every 90 steps to skip the simulation forward 275 days
	// associated habitat suitability deaths and lifecycle behaviors are also advanced 275 days
	@ScheduledMethod(start = 90, interval = 90)
	public void skip_inactive_period() {
		double prob_death = 1 - habitat_sample(); 
		double prob_death_per_day = prob_death / 365;
		
		if (Math.random() < (prob_death_per_day * 275) ) {
			System.out.println(name + " dying from habitat sampling (275 day skip):");
			System.out.println("\tHabitat Sample: " + habitat_sample());
			die();
			return;
		}
		
		// kill any adult females that didn't lay their eggs before winter
		if (this.laying_eggs) {
			die();
			return;
		}
		lifecycle_counter += 275;
		return;
	}
	
	
	// Logic for attaching to Host, expected to be called by the Host to be infected
	public boolean attach(Host host) {
		// only adults and nymphs should attach
		if (this.life_stage.equals("adult")) {
			int num_ticks = host.tick_list.size();
			double prob = 1.0 / (num_ticks + 2);
			if (Math.random() < prob) {
				attached = true;
				this.host = host;
				host.add_tick(this);
				return true;
			}
		}
		return false;
	}
	
	// Logic for detaching from Host
	public void detach() {
		attached = false;
		attach_count = 0;
		host.remove_tick(this);
		//System.out.println(name + " detached from " + host.getName());
		host = null;
	}
	
	// set male or female
	private void determine_sex() {
		Random rnd = new Random();
		switch(rnd.nextInt(2)) {
			case 0: // female
				female = true;
				break;
			case 1: // male
				female = false;
				break;
		}
	}
	
	// this method handles the bulk of the lifecycle behaviors and is split between the different lifestates
	// egg, larva and nymph lifestates are fairly simple, attempting to survive long enough to transform
	// adult lifestates are more complex due to mating behaviors and have additional helper methods
	private void lifecycle() {
		lifecycle_counter++;
		double prob_death = 1 - habitat_sample(); 
		double prob_death_per_day = prob_death / 365;
		if (Math.random() < prob_death_per_day) {
			System.out.println(name + " dying from habitat sampling (active 90 days):");
			System.out.println("\tHabitat Sample: " + habitat_sample());
			die();
			return;
		}
		
		switch (life_stage) {
			case "egg":
				if (lifecycle_counter > EGG_LENGTH)
					hatch();
				break;
			case "larva":
				if (lifecycle_counter > LARVA_LENGTH) {
					molt();
					break;
				}
				break;
			case "nymph":
				if (lifecycle_counter > NYMPH_LENGTH) {
					molt();
					break;
				}
				break;
			case "adult": 
				// female behaviors are fairly simple - just need to check for mortality
				if(female) {
					if (lifecycle_counter > ADULT_LENGTH) {
						System.out.println(name + " dying from being too old (adult female):");
						System.out.println("\t Lifecycle Counter: " + lifecycle_counter);
						die();
						return;
					}
					if (laying_eggs) {
						lay_eggs();
					}
				}
				// male behaviors - if attached, search for a viable mate
				else {
					if (lifecycle_counter > ADULT_LENGTH) {
						System.out.println(name + " dying from being too old (adult male):");
						System.out.println("\t Lifecycle Counter: " + lifecycle_counter);
						die();
						return;
					}
					if (attached) {
						for(Tick tick : host.tick_list) {
							  if (tick.female) {
							    mate();
							    tick.mate();
							    break;
							  }
						}
						break;
					}
				}
				break;
			default:
				System.out.println("\tLife cycle error: " + name + " has invalid life stage. Removing agent.");
				die();
				return;
		}
	}
	
	// wrapper for hatching behavior transitioning egg to larva
	private void hatch() {
		lifecycle_counter = 0;
		has_fed = false;
		life_stage = "larva";
		set_attach_length(life_stage);
		return;
	}
	
	// wrapper for molting behavior, transitioning larva and nymphs to their next lifestages
	private void molt() {
		lifecycle_counter = 0;
		has_fed = false;
		switch(life_stage) {
			case "larva":
				life_stage = "nymph";
				set_attach_length(life_stage);
				break;
			case "nymph":
				life_stage = "adult";
				set_attach_length(life_stage);
				break;
		}
	}
	
	// egg laying behavior - eggs are laid over a period of days allowing female adults the possibility of
	// dying to the habitat suitability layer during the egg laying process
	protected void lay_eggs() {
		
		if (eggs_remaining > 0) {
			System.out.println(name + " getting Coordinate for egg laying:");
			Coordinate coord = getCoord();
			for (int i = 0; i < 100 && eggs_remaining > 0; i++) {
				IxPacificus new_tick = new IxPacificus("Child " + child_count + " of " + name, "egg");
				child_count++;
				eggs_remaining--;
				Point curr_loc = geoFac.createPoint(coord);
				context.add(new_tick);
				geography.move(new_tick, curr_loc);
			}
			//System.out.println(name + " has " + eggs_remaining + " eggs left.");
		}
		else {
			System.out.println(name + " dying from laying all eggs:");
			die();
		}
	}
	
	// this method handles the death process of Tick agents, detaching them from hosts and removing 
	// all references to the contextual framework
	public void die() {
		if (attached) {
			System.out.println("\tHost: " + host.getName());
			detach();
		}
		context.remove(this);
		System.out.println("\tSuccessfully removed from context.");
		return;
	}
	
	// method for setting the static habitat_sample variable for the class
	public static void set_habitat_sample(double value) {
		habitat_sample = value;
	}
	
	// returns the habitat sample value for the Tick agent
	// if the habitat_sample variable is > 0, returns the constant parameterized value for the run
	// otherwise, samples the habitat suitability raster at the corresponding location of the Tick agents
	public double habitat_sample() {
		// if habitat_sample > 0, we're using a constant parameterized habitat sample value
		if (habitat_sample > 0)
			return habitat_sample;
		// using habitat suitability raster
		else {
			Coordinate coord = new Coordinate(geography.getGeometry(this).getCoordinate());
			DirectPosition position = new DirectPosition2D(geography.getCRS(), coord.x, coord.y);
			double[] sample = (double[]) suitability_raster.evaluate(position);
			return sample[0];
		}
	}
}

package kenaiMoose;

import java.util.Random;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.geometry.DirectPosition2D;
import org.opengis.geometry.DirectPosition;

import com.vividsolutions.jts.geom.Coordinate;
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
	
	@ScheduledMethod(start = 0)
	public void init() {
		context = ContextUtils.getContext(this);
		geography = (Geography)context.getProjection("Kenai");
		System.out.println(this.name + " habitat sample: " + habitat_sample());
	}
	
	public static void setSuitability(GridCoverage2D raster) {
		suitability_raster = raster;
		return;
	}
	
	public boolean is_laying_eggs() {
		return laying_eggs;
	}
	
	
	public Coordinate getCoord() {
		return new Coordinate(geography.getGeometry(this).getCoordinate());
	}
	
	//Get lat and long for data sets
	
	public double getLong() {
		Coordinate coord = getCoord();
		return coord.x;
	}

	public double getLat() {
		Coordinate coord = getCoord();
		return coord.y;
	}
	
	public String getLifestate() {
		return this.life_stage;
	}
	
	
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
	
	@ScheduledMethod(start = 90, interval = 90)
	public void skip_inactive_period() {
		double prob_death = 1 - habitat_sample(); 
		double prob_death_per_day = prob_death / 365;
		
		if (Math.random() < (prob_death_per_day * 275) )
			die();
		lifecycle_counter += 275;
		return;
	}
	
	// Abstract methods to force setting ATTACH_LENGTH and ATTACH_DELAY - protected
	protected abstract void set_attach_length(String lifecycle);
	
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
	
	public static void setStartStage(String stage) {
		START_LIFE_CYCLE = stage;
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
	
	// determine what to do and update lifecycle counter
	private void lifecycle() {
		lifecycle_counter++;
		double prob_death = 1 - habitat_sample(); 
		double prob_death_per_day = prob_death / 365;
		if (Math.random() < prob_death_per_day) 
			die();
		
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
		}
	}
	
	private void hatch() {
		lifecycle_counter = 0;
		has_fed = false;
		life_stage = "larva";
		set_attach_length(life_stage);
		return;
	}
	
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
	
	// TODO: implement this
	protected abstract void mate();
	protected void lay_eggs() {
		
		if (eggs_remaining > 0) {
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
			//System.out.println(name + " has layed all their eggs.");
			die();
		}
	}
	
	public void die() {
		if (attached) {
			detach();
		}
		context.remove(this);
		return;
	}
	
	public double habitat_sample() {
		Coordinate coord = new Coordinate(geography.getGeometry(this).getCoordinate());
		DirectPosition position = new DirectPosition2D(geography.getCRS(), coord.x, coord.y);
		double[] sample = (double[]) suitability_raster.evaluate(position);
		return sample[0];
	}
}

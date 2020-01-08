package kenaiMoose;

import java.util.Random;

public class IxPacificus extends Tick {
	
	/*
	 * IxPacificus should use TickStyle.java for visualization
	 */

	public IxPacificus(String name) {
		super(name);
		set_attach_length(life_stage);

	}

	
	public IxPacificus(String name, String life_stage) {
		super(name, life_stage);
		set_attach_length(life_stage);
	}
	
	// Padgett & Lane (2001) used for rough mortality length numbers
	public void init() {
		super.init();
		EGG_LENGTH = 60;
		LARVA_LENGTH = 450;
		LARVA_FEED_LENGTH = 4;
		NYMPH_LENGTH = 450;
		NYMPH_FEED_LENGTH = 7;
		ADULT_LENGTH = 365;
	}
	
	
	// explicit method for updating attach_length variable during lifecycle changes
	protected void set_attach_length(String lifecycle) {
		switch(lifecycle) {
			case "larva":
				attach_delay = LARVA_FEED_LENGTH;
				break;
			case "nymph":
				attach_delay = NYMPH_FEED_LENGTH;
				break;
			case "adult":
				attach_delay = ADULT_FEED_LENGTH;
				break;
			default:
				break;
		}
	}

}

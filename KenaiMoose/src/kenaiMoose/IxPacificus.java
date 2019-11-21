package kenaiMoose;

import java.util.Random;

public class IxPacificus extends Tick {
	
	/*
	 * IxPacificus should use TickStyle.java for visualization
	 */

	public IxPacificus(String name) {
		super(name);
		EGG_LENGTH = 55;
		LARVA_LENGTH = 365;
		LARVA_FEED_LENGTH = 7;
		set_attach_length(life_stage);
		NYMPH_LENGTH = 270;
		ADULT_LENGTH = 90;
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

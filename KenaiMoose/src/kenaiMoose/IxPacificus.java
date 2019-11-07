package kenaiMoose;

import java.util.Random;

public class IxPacificus extends Tick {
	
	/*
	 * IxPacificus should use TickStyle.java for visualization
	 */

	public IxPacificus(String name) {
		super(name);
		set_attach_delay(20);
		EGG_LENGTH = 55;
		LARVA_LENGTH = 365;
		LARVA_FEED_LENGTH = 7;
		set_attach_length(LARVA_FEED_LENGTH);
		NYMPH_LENGTH = 270;
		ADULT_LENGTH = 90;
	}
	
	// explicit method for updating attach_length variable during lifecycle changes
	protected void set_attach_length(int length) {
		this.attach_length = length;
	}

	protected void set_attach_delay(int delay) {
		this.attach_delay = delay;
	}

}

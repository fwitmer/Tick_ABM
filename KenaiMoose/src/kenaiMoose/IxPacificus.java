package kenaiMoose;

import java.util.Random;

public class IxPacificus extends Tick {
	
	/*
	 * IxPacificus should use TickStyle.java for visualization
	 */

	public IxPacificus(String name) {
		super(name);
		set_attach_length(7);
		set_attach_delay(20);
		
		// Randomly decide sex upon creation, currently set to 50/50 until data found
		Random rnd = new Random();
		switch(rnd.nextInt(2)) {
			case 0:
				female = true;
			case 1:
				female = false;
		}
	}

	protected void set_attach_length(int length) {
		this.attach_length = length;
	}

	protected void set_attach_delay(int delay) {
		this.attach_delay = delay;
	}

}

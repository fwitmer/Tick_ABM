package kenaiMoose;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduleParameters;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.gis.Geography;
import repast.simphony.util.ContextUtils;

public class Moose {
	
	private String name;
	// TODO create and utilize energy value
	
	public Moose(String name) {
		this.name = name;
	}

	// Establishing random moves for Moose agents
	@ScheduledMethod(start = 1, interval = 1, priority = ScheduleParameters.FIRST_PRIORITY)
	public void step() {
		randomWalk();
	}
	
	public void randomWalk() {
		Context context = ContextUtils.getContext(this);
		Geography<Moose> geography = (Geography)context.getProjection("Kenai");
		
		geography.moveByDisplacement(this, RandomHelper.nextDoubleFromTo(-0.0005, 0.0005), 
				RandomHelper.nextDoubleFromTo(-0.0005, 0.0005));
	}
	
	public String getName() {
		return name;
	}
}

package kenaiMoose;

public class IxPacificus extends Tick {

	public IxPacificus(String name) {
		super(name);
		setATTACH_LENGTH(7);
		setATTACH_DELAY(20);
	}

	@Override
	protected void setATTACH_LENGTH(int length) {
		this.ATTACH_LENGTH = length;
	}

	@Override
	protected void setATTACH_DELAY(int delay) {
		this.ATTACH_DELAY = delay;
	}

}

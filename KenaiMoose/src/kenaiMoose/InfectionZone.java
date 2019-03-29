package kenaiMoose;

public class InfectionZone {
	
	protected boolean visible = true;
	protected boolean infected = false;

	
	public boolean isVisible() {
		return visible;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	public boolean isInfected() {
		return infected;
	}
	
	public void setInfected(boolean infected) {
		this.infected = infected;
	}

}

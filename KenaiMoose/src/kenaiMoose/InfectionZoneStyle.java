package kenaiMoose;

import java.awt.Color;

import kenaiMoose.InfectionZone;
import gov.nasa.worldwind.render.SurfacePolygon;
import gov.nasa.worldwind.render.SurfaceShape;
import repast.simphony.visualization.gis3D.style.SurfaceShapeStyle;

public class InfectionZoneStyle implements SurfaceShapeStyle<InfectionZone> {

	@Override
	public SurfaceShape getSurfaceShape(InfectionZone object, SurfaceShape shape) {
		return new SurfacePolygon();
	}

	@Override
	public Color getFillColor(InfectionZone zone) {
		return Color.CYAN;
	}

	@Override
	public double getFillOpacity(InfectionZone zone) {
		if(!zone.isVisible()) return 0;
		
		return 0.15;
	}

	@Override
	public Color getLineColor(InfectionZone zone) {
		return Color.GREEN;
	}

	@Override
	public double getLineOpacity(InfectionZone zone) {
		if(!zone.isVisible()) return 0;
		
		return 0.15;
	}

	@Override
	public double getLineWidth(InfectionZone obj) {
		return 3;
	}

}

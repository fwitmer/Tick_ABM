package kenaiMoose;

import java.awt.Color;

import kenaiMoose.BoundaryZone;
import gov.nasa.worldwind.render.SurfacePolygon;
import gov.nasa.worldwind.render.SurfaceShape;
import repast.simphony.visualization.gis3D.style.SurfaceShapeStyle;

public class BoundaryZoneStyle implements SurfaceShapeStyle<BoundaryZone> {

	@Override
	public SurfaceShape getSurfaceShape(BoundaryZone object, SurfaceShape shape) {
		return new SurfacePolygon();
	}

	@Override
	public Color getFillColor(BoundaryZone zone) {
		return Color.CYAN;
	}

	@Override
	public double getFillOpacity(BoundaryZone obj) {
		return 0;
	}

	@Override
	public Color getLineColor(BoundaryZone zone) {
		return Color.RED;
	}

	@Override
	public double getLineOpacity(BoundaryZone obj) {
		return 1.0;
	}

	@Override
	public double getLineWidth(BoundaryZone obj) {
		return 3;
	}

}

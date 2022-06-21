package org.matsim.prepare.homework1;

import org.locationtech.jts.geom.Geometry;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.gis.ShapeFileReader;

import java.util.Collection;
import java.util.List;

public class CoordinateGeometryUtils {
	public static final CoordinateTransformation TRANSFORMATION_UMWELTZONE = TransformationFactory.getCoordinateTransformation("EPSG:31468", "EPSG:25833");
	private final CoordinateTransformation transformation;

	public CoordinateGeometryUtils(CoordinateTransformation transformation) {
		this.transformation = transformation;
	}

	public boolean isActivityInGeometry(Activity activity, Geometry geometry) {
		var coord = activity.getCoord();
		return isCoordInGeometry(coord, geometry);
	}

	public boolean isCoordInGeometry(Coord coord, Geometry geometry) {
		var geoToolsPoint = MGC.coord2Point(transformation.transform(coord));
		return geometry.contains(geoToolsPoint);
	}

	public long countTripsFromTo(Collection<TripStructureUtils.Trip> trips, Geometry from, Geometry to) {
		return trips.stream()
				.filter(trip -> isActivityInGeometry(trip.getOriginActivity(), from) && isActivityInGeometry(trip.getDestinationActivity(), to))
				.count();
	}

	public long countActivitiesInGeometry(Geometry geometry, List<Activity> activities) {
		return activities.stream()
				.filter(activity -> isActivityInGeometry(activity, geometry))
				.count();
	}

	public static Geometry getUmweltzone() {
		var shapeFileName = "shapes/Umweltzone.shp";
		return (Geometry) ShapeFileReader.getAllFeatures(shapeFileName).stream().findFirst().get().getDefaultGeometry();
	}
}

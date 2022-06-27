package org.matsim.prepare.homework1;

import org.locationtech.jts.geom.Geometry;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.gis.ShapeFileReader;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CoordinateGeometryUtils {
	public static final CoordinateTransformation TRANSFORMATION_UMWELTZONE = TransformationFactory.getCoordinateTransformation("EPSG:31468", "EPSG:25833");
	private final CoordinateTransformation transformation;
	private final Map<Id<Link>, ? extends Link> links;

	public CoordinateGeometryUtils(CoordinateTransformation transformation) {
		this.transformation = transformation;
		this.links = Collections.emptyMap();
	}

	public CoordinateGeometryUtils(CoordinateTransformation transformation, Network network) {
		this.transformation = transformation;
		this.links = network.getLinks();
	}


	public boolean isActivityInGeometry(Activity activity, Geometry geometry) {
		var coord = activity.getCoord();
		return isCoordInGeometry(coord, geometry);
	}

	public boolean isCoordInGeometry(Coord coord, Geometry geometry) {
		var geoToolsPoint = MGC.coord2Point(transformation.transform(coord));
		return geometry.contains(geoToolsPoint);
	}

	public boolean isLinkInGeometry(Link link, Geometry geometry) {
		return isCoordInGeometry(link.getCoord(), geometry);
	}

	public boolean isLegInGeometry(Leg leg, Geometry geometry) {
		return isLinkInGeometry(links.get(leg.getRoute().getStartLinkId()), geometry)
			|| isLinkInGeometry(links.get(leg.getRoute().getEndLinkId()), geometry);
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

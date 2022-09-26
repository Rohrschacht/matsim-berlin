package org.matsim.prepare.homework1;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;
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
	public static final CoordinateTransformation TRANSFORMATION_UMWELTZONE_BACK = TransformationFactory.getCoordinateTransformation("EPSG:25833", "EPSG:31468");
	private static Geometry _umweltzone;
	private final CoordinateTransformation transformation;
	private final CoordinateTransformation inverseTransformation;
	private final Map<Id<Link>, ? extends Link> links;

	public CoordinateGeometryUtils(CoordinateTransformation transformation, CoordinateTransformation inverseTransformation) {
		this.transformation = transformation;
		this.inverseTransformation = inverseTransformation;
		this.links = Collections.emptyMap();
	}

	public CoordinateGeometryUtils(CoordinateTransformation transformation, CoordinateTransformation inverseTransformation, Network network) {
		this.transformation = transformation;
		this.inverseTransformation = inverseTransformation;
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
		return isCoordInGeometry(link.getCoord(), geometry)
			|| isCoordInGeometry(link.getFromNode().getCoord(), geometry)
			|| isCoordInGeometry(link.getToNode().getCoord(), geometry)
		;
	}

	public boolean isLegInGeometry(Leg leg, Geometry geometry) {
		if (leg.getRoute() == null) {
			return false;
		}
		return isLinkInGeometry(links.get(leg.getRoute().getStartLinkId()), geometry)
			|| isLinkInGeometry(links.get(leg.getRoute().getEndLinkId()), geometry);
	}

	public boolean isBeelineInGeometry(Coord point1, Coord point2, Geometry geometry) {
		var p1 = MGC.coord2Point(transformation.transform(point1));
		var p2  = MGC.coord2Point(transformation.transform(point2));
		LineString line = new LineString(new CoordinateArraySequence(new Coordinate[]{p1.getCoordinate(), p2.getCoordinate()}), geometry.getFactory());
		return geometry.intersects(line);
	}

	public Coord getActivityIntersectionCoords(Coord start, Coord end, Geometry geometry) {
		var p1 = MGC.coord2Point(transformation.transform(start));
		var p2  = MGC.coord2Point(transformation.transform(end));
		LineString line = new LineString(new CoordinateArraySequence(new Coordinate[]{p1.getCoordinate(), p2.getCoordinate()}), geometry.getFactory());
		LineString intersectingLine = new LineString(new CoordinateArraySequence(geometry.intersection(line).getCoordinates()), geometry.getFactory());
		//geometry.getBoundary().intersection(line).getCoordinate();
		if (intersectingLine.getStartPoint().getCoordinate().equals(p1.getCoordinate()) || intersectingLine.getStartPoint().getCoordinate().equals(p2.getCoordinate())) {
			return inverseTransformation.transform(MGC.point2Coord(intersectingLine.getEndPoint()));
		} else {
			return inverseTransformation.transform(MGC.point2Coord(intersectingLine.getStartPoint()));
		}
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
		if (_umweltzone == null) {
			var shapeFileName = "shapes/Umweltzone_test.shp";
			_umweltzone = (Geometry) ShapeFileReader.getAllFeatures(shapeFileName).stream().findFirst().get().getDefaultGeometry();
		}
		return _umweltzone;
	}
}

package org.matsim.prepare.homework1;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;

import java.util.HashSet;
import java.util.Set;

import static org.matsim.prepare.homework1.CoordinateGeometryUtils.getUmweltzone;

public class PrepareNetworkCarfreeRing {

	public static final String ATTRIBUTE_EXCLUDE_NAME = "umweltzone";
	public static final String ATTRIBUTE_EXCLUDE_VALUE = "no_car";

	/**
	 * A set of links that are not directly within the Umweltzone shapefile, but only reachable from within.
	 * They in turn also need to be forbidden
	 */
	private static Set<Id<Link>> excludedLinks = Set.of(
		// Spandauer Damm - Westend
		Id.createLinkId(38708)
		,Id.createLinkId(38757)
		,Id.createLinkId(128599)
		,Id.createLinkId(125768)
		,Id.createLinkId(118582)
		,Id.createLinkId(47451)
		,Id.createLinkId(68015)
		// Sonnenallee
		,Id.createLinkId(129504)
		,Id.createLinkId(71660)
		,Id.createLinkId(5305)
		,Id.createLinkId(11760)
		,Id.createLinkId(12586)
		,Id.createLinkId(129499)
		// Ringbahn - Neues Ufer
		,Id.createLinkId(119213)
		,Id.createLinkId(119212)
		// A100 Schmargendorf
		,Id.createLinkId(60892)
		// Detmolder Straße
		,Id.createLinkId(82600)
		,Id.createLinkId(28652)
		,Id.createLinkId(137869)
		,Id.createLinkId(79235)
		// A100 - Abfahrt Detmolder Str.
		,Id.createLinkId(120962)
		,Id.createLinkId(130696)
		,Id.createLinkId(86275)
		// A100 - Auffahrt Detmolder Str.
		,Id.createLinkId(86274)
		,Id.createLinkId(138535)
		,Id.createLinkId(40245)
		// Friedrich-Krause-Ufer
		,Id.createLinkId(137696)
		,Id.createLinkId(137697)
		,Id.createLinkId(137698)
		,Id.createLinkId(137699)
		,Id.createLinkId(111843)
		,Id.createLinkId(111844)
		// An der Putlitzbrücke - Westhafen
		,Id.createLinkId(92030)
		,Id.createLinkId(111993)
		,Id.createLinkId(65598)
		,Id.createLinkId(65599)
		,Id.createLinkId(118917)
		,Id.createLinkId(111962)
		,Id.createLinkId(92031)
		,Id.createLinkId(91746)
		);

	public static void main(String[] args) {

		var networkFileName = "scenarios/berlin-v5.5-1pct/output-berlin-v5.5-1pct/berlin-v5.5-1pct.output_network.xml.gz";
		var networkOutfileName = "scenarios/berlin-v5.5-1pct/output-berlin-v5.5-1pct/berlin-v5.5-1pct.carfree_ring_network.xml.gz";

		var network = NetworkUtils.readNetwork(networkFileName);
		makeLinksInRingCarfree(network);
		NetworkUtils.writeNetwork(network, networkOutfileName);
	}

	public static void makeLinksInRingCarfree(Network network) {
		var umweltzone = getUmweltzone();
		var coordinateUtils = new CoordinateGeometryUtils(CoordinateGeometryUtils.TRANSFORMATION_UMWELTZONE);

		for (var link : network.getLinks().values()) {
			if (coordinateUtils.isCoordInGeometry(link.getCoord(), umweltzone)) {
				if (link.getAllowedModes().contains(TransportMode.car)) {
					link.setCapacity(0.01);
				}
			}
		}
	}

	public static void removeAllowedMode(Link link, String mode) {
		if (link.getAllowedModes().contains(mode)) {
			Set<String> allowedModes = new HashSet<>(link.getAllowedModes());
			allowedModes.remove(mode);
			link.setAllowedModes(allowedModes);
		}
	}

	public static void removeCarsFromAllowedModesInRing(Network network) {
		var umweltzone = getUmweltzone();
		var coordinateUtils = new CoordinateGeometryUtils(CoordinateGeometryUtils.TRANSFORMATION_UMWELTZONE);

		for (var link : network.getLinks().values()) {
			// check if link is manually excluded
			if (excludedLinks.contains(link.getId())) {
				link.getAttributes().putAttribute(ATTRIBUTE_EXCLUDE_NAME, ATTRIBUTE_EXCLUDE_VALUE);
				removeAllowedMode(link, TransportMode.car);
			} else if (coordinateUtils.isCoordInGeometry(link.getCoord(), umweltzone)) {
				removeAllowedMode(link, TransportMode.car);
			}
		}
	}
}

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
		,Id.createLinkId(120552)
		,Id.createLinkId(120486)
		,Id.createLinkId(120492)
		// Ringbahn - Neues Ufer
		,Id.createLinkId(119213)
		,Id.createLinkId(119212)
		// A100 Schmargendorf
		,Id.createLinkId(60892)
		,Id.createLinkId(40246)
		,Id.createLinkId(41727)
		,Id.createLinkId(77691)
		// An der Putlitzbrücke - Westhafen
		,Id.createLinkId(92030)
		,Id.createLinkId(111993)
		,Id.createLinkId(65598)
		,Id.createLinkId(65599)
		,Id.createLinkId(118917)
		,Id.createLinkId(111962)
		,Id.createLinkId(92031)
		,Id.createLinkId(91746)
		// A100 - Schmargendorf
		,Id.createLinkId(120900)
		,Id.createLinkId(40233)
		,Id.createLinkId(40232)
		,Id.createLinkId(47307)
		,Id.createLinkId(40243)
		// Hermannstraße
		,Id.createLinkId(108714)
		,Id.createLinkId(120107)
		,Id.createLinkId(120119)
		,Id.createLinkId(136723)
		// Tempelhof
		,Id.createLinkId(90841)
		,Id.createLinkId(109644)
		,Id.createLinkId(109646)
		,Id.createLinkId(109659)
		,Id.createLinkId(28823)
		,Id.createLinkId(90840)
		// Frankfurter Allee
		,Id.createLinkId(127552)
		,Id.createLinkId(3501)
		// Landsberger Allee
		,Id.createLinkId(147644)
		,Id.createLinkId(147631)
		// Kniprodestraße
		,Id.createLinkId(142802)
		,Id.createLinkId(83447)
		,Id.createLinkId(3246)
		,Id.createLinkId(148145)
		// Greifswalder Str
		,Id.createLinkId(62189)
		,Id.createLinkId(147027)
		// Schönhauser Allee
		,Id.createLinkId(102314)
		,Id.createLinkId(156529)
		// Ostpreußenbrücke - Neue Kantstraße
		,Id.createLinkId(151256)
		,Id.createLinkId(149708)
		,Id.createLinkId(151264)
		,Id.createLinkId(149700)
		,Id.createLinkId(13579)
		,Id.createLinkId(149698)
		,Id.createLinkId(149699)
		,Id.createLinkId(9636)
		// Kurfürstendamm
		,Id.createLinkId(149582)
		,Id.createLinkId(69947)
		// Hohenzollerndamm
		,Id.createLinkId(141914)
		,Id.createLinkId(149408)
		,Id.createLinkId(147670)
		,Id.createLinkId(113525)
		,Id.createLinkId(113473)
		,Id.createLinkId(149413)
		,Id.createLinkId(50144)
		,Id.createLinkId(141906)
		,Id.createLinkId(134865)
		,Id.createLinkId(113467)
		,Id.createLinkId(13913)
		,Id.createLinkId(149473)
		,Id.createLinkId(147669)
		,Id.createLinkId(113528)
		,Id.createLinkId(50143)
		,Id.createLinkId(134841)
		,Id.createLinkId(149471)
		,Id.createLinkId(13915)
		// Bundesplazu Tunnel
		,Id.createLinkId(152708)
		,Id.createLinkId(71225)
		,Id.createLinkId(71226)
		,Id.createLinkId(71232)
		,Id.createLinkId(71233)
		,Id.createLinkId(71280)
		,Id.createLinkId(71282)
		,Id.createLinkId(98343)
		,Id.createLinkId(62495)
		,Id.createLinkId(152713)
		,Id.createLinkId(71239)
		,Id.createLinkId(71240)
		,Id.createLinkId(71242)
		,Id.createLinkId(98340)
		,Id.createLinkId(62509)
		// Beusselstraße
		,Id.createLinkId(63454)
		,Id.createLinkId(104461)
		// Kärntener Straße (Enklave)
		,Id.createLinkId(87443)
		,Id.createLinkId(87444)
		,Id.createLinkId(87449)
		,Id.createLinkId(87450)
		// Sachsendamm
		,Id.createLinkId(91062)
		,Id.createLinkId(91037)
		,Id.createLinkId(114751)
		,Id.createLinkId(77116)
		// Kaiserdamm
		,Id.createLinkId(149709)
		,Id.createLinkId(138052)
		,Id.createLinkId(22145)
		,Id.createLinkId(149711)
		// Spiegelweg
		,Id.createLinkId(40651)
		// Gesundbrunnen
		,Id.createLinkId(27010)
		,Id.createLinkId(2878)
		,Id.createLinkId(67518)
		,Id.createLinkId(27011)

	);

	public static void main(String[] args) {

		var networkFileName = "scenarios/homework2-1pct/input/berlin-v5.5-network.xml.gz";
		var networkOutfileName = "scenarios/homework2-1pct/input/berlin-v5.5-1pct_network-edited.xml.gz";

		var network = NetworkUtils.readNetwork(networkFileName);
		removeCarsFromAllowedModesInRing(network);
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

package org.matsim.prepare.homework1;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;

import java.util.HashSet;

import static org.matsim.prepare.homework1.CoordinateGeometryUtils.getUmweltzone;

public class PrepareNetworkCarfreeRing {
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
}

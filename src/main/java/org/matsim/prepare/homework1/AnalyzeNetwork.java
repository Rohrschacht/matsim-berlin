package org.matsim.prepare.homework1;

import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.NetworkUtils;

public class AnalyzeNetwork {

	public static void main(String[] args) {
		Network network = NetworkUtils.readNetwork("scenarios/berlin-v5.5-1pct/output-berlin-v5.5-1pct/berlin-v5.5-1pct.output_network.xml.gz");
		int walkLinkCount = 0;
		for (var link : network.getLinks().values()) {
			if (link.getAllowedModes().contains(TransportMode.walk) || link.getAllowedModes().contains(TransportMode.non_network_walk) || link.getAllowedModes().contains(TransportMode.transit_walk)) {
				walkLinkCount++;
			}
		}
		System.out.println(walkLinkCount);
	}
}

package org.matsim.analysis;

import org.locationtech.jts.geom.Geometry;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.prepare.homework1.CoordinateGeometryUtils;

import java.util.HashSet;
import java.util.Set;

public class AffectedAgents {

	/**
	 * Return all persons, which have a plan within the given geometry.
	 */
	public static Set<Person> fromGeometry(Population population, Network network, Geometry geometry, String... transportModes) {
		var coordinateUtils = new CoordinateGeometryUtils(CoordinateGeometryUtils.TRANSFORMATION_UMWELTZONE, network);

		var affectedPersons = new HashSet<Person>();

		persons:
		for (Person person : population.getPersons().values()) {
			for (Plan plan : person.getPlans()) {
				for (Leg leg : TripStructureUtils.getLegs(plan.getPlanElements())) {
					if (coordinateUtils.isLegInGeometry(leg, geometry)) {
						if (transportModes.length == 0) { // default all
							affectedPersons.add(person);
							continue persons; // this person is affected, continue with next
						} else {
							for (String mode : transportModes) {
								if (leg.getMode().equals(mode)) {
									affectedPersons.add(person);
									continue persons; // this person is affected, continue with next
								}
							}
						}
					}
				}
			}
		}

		return affectedPersons;
	}

	/**
	 * Return all persons, which have a plan within the given geometry. And vehicle types.
	 */
	public static Set<Person> fromGeometrySelectedPlansOnly(Population population, Network network, Geometry geometry, String... transportModes) {
		var coordinateUtils = new CoordinateGeometryUtils(CoordinateGeometryUtils.TRANSFORMATION_UMWELTZONE, network);

		var affectedPersons = new HashSet<Person>();

		persons:
		for (Person person : population.getPersons().values()) {
			for (Leg leg : TripStructureUtils.getLegs(person.getSelectedPlan().getPlanElements())) {
				if (coordinateUtils.isLegInGeometry(leg, geometry)) {
					if (transportModes.length == 0) { // default all
						affectedPersons.add(person);
						continue persons; // this person is affected, continue with next
					} else {
						for (String mode : transportModes) {
							if (leg.getMode().equals(mode)) {
								affectedPersons.add(person);
								continue persons; // this person is affected, continue with next
							}
						}
					}
				}
			}
		}

		return affectedPersons;
	}

}

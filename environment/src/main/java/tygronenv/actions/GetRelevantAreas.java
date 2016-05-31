package tygronenv.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.vividsolutions.jts.geom.MultiPolygon;

import eis.eis2java.exception.TranslationException;
import eis.eis2java.translation.Translator;
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Parameter;
import eis.iilang.ParameterList;
import eis.iilang.Percept;
import nl.tytech.core.client.event.EventManager;
import nl.tytech.core.event.Event;
import nl.tytech.core.event.EventListenerInterface;
import nl.tytech.core.net.serializable.MapLink;
import nl.tytech.core.net.serializable.PolygonItem;
import nl.tytech.core.structure.ItemMap;
import nl.tytech.data.engine.item.Building;
import nl.tytech.data.engine.item.Stakeholder;
import nl.tytech.util.logger.TLogger;
import tygronenv.TygronEntity;

/**
 * Creates a list of areas that can be used with the corresponding actionType.
 * Possible actionTypes are "build", "demolish", and none more at the moment (it's a WIP).
 * It is also possible to specify a filter, with zone,<id1>,<id2> or stakeholder,<id1>,<id2> pairs, filtering
 * only on pieces of land that are owned by the specified stakeholder, or are in the specified zone.
 * @author Max Groenenboom
 */
public class GetRelevantAreas implements CustomAction, EventListenerInterface {

	private static final Translator TRANSLATOR = Translator.getInstance();

	private ItemMap<Building> buildings;

	/**
	 * Constructor for this CustomAction. It adds an eventListener for Buildings and Lands.
	 */
	public GetRelevantAreas() {
		EventManager.addListener(this, MapLink.BUILDINGS, MapLink.LANDS);
	}

	@Override
	public Percept call(final TygronEntity caller, final LinkedList<Parameter> parameters) {
		Percept result = new Percept("relevant_areas");

		// Get parameters.
		Iterator<Parameter> params = parameters.iterator();
		Number callID = ((Numeral) params.next()).getValue();
		String actionType = ((Identifier) params.next()).getValue();
		ParameterList filters = null;
		if (params.hasNext()) {
			Parameter filterParam = params.next();
			// If the filter parameter is an Identifier, it is probably "[]", or someone called it wrong.
			// Thus, there are no (valid) filters specified.
			if (filterParam instanceof ParameterList) {
				filters = (ParameterList) filterParam;
			} else {
				filters = new ParameterList();
			}
		} else {
			filters = new ParameterList();
		}

		// Get multiPolygons.
		List<PolygonItem> items = getUsableArea(caller, actionType);

		// Filter resulting multiPolygons.
		filterPolygons(items, filters);

		// Create result parameters.
		result.addParameter(new Numeral(callID));
		ParameterList areas = new ParameterList();
		try {
			for (PolygonItem item : items) {
				MultiPolygon polygon = item.getQTMultiPolygons()[0];
				areas.add(new ParameterList(
						TRANSLATOR.translate2Parameter(polygon)[0],
						new Numeral(polygon.getArea())
				));
			}
		} catch (TranslationException e) {
			TLogger.exception(e);
			e.printStackTrace();
		}
		result.addParameter(areas);
		System.out.println("Resulting Percept:");
		final int maxAmountOfPolys = 50;
		if (areas.size() > maxAmountOfPolys) {
			System.out.println("<<snipped, too large (" + areas.size() + " MultiPolygons)>>");
		} else {
			System.out.println(result);
		}

		return result;
	}

	/**
	 * Creates a list of MultiPolygons that can be used for the specified actionType, by the specified
	 * stakeholder.
	 * @param caller The tygronEntity that called the action.
	 * @param actionType The type of the action. Can be "build" or "demolish".
	 * @return A list of MultiPolygons the stakeholder can use.
	 */
	private List<PolygonItem> getUsableArea(final TygronEntity caller, final String actionType) {
		Stakeholder stakeholder = caller.getStakeholder();
		switch (actionType) {
		case "build":
			return getBuildableArea(stakeholder);
		case "demolish":
			return getDemolishableArea(stakeholder);
		default:
			return new ArrayList<PolygonItem>();
		}
	}

	/**
	 * Returns all of the MultiPolygons the specified stakeholder can build on.
	 * @param stakeholder The stakeholder to compile a list for.
	 * @return The list of MultiPolygons.
	 */
	private List<PolygonItem> getBuildableArea(final Stakeholder stakeholder) {
		// TODO Create this method //
		return new ArrayList<PolygonItem>();
	}

	/**
	 * Returns all of the MultiPolygons the specified stakeholder can demolish.
	 * @param stakeholder The stakeholder to compile a list for.
	 * @return The list of MultiPolygons.
	 */
	private List<PolygonItem> getDemolishableArea(final Stakeholder stakeholder) {
		List<PolygonItem> polygons = new ArrayList<PolygonItem>();
		for (Building building : buildings) {
			if (building.getOwner().getID() == stakeholder.getID()) {
				polygons.add(building);
			}
		}
		return polygons;
	}

	/**
	 * Filters all polygons according the the specified filters.
	 * @param polygons The list of polygons to filter on.
	 * @param filters The filters.
	 */
	private void filterPolygons(final List<PolygonItem> polygons, final ParameterList filters) {
		// TODO Create this method //
	}

	@Override
	public String getName() {
		return "get_relevant_areas";
	}

	@Override
	public void notifyListener(final Event event) {
		switch ((MapLink) event.getType()) {
		case BUILDINGS:
			buildings = event.<ItemMap<Building>>getContent(MapLink.COMPLETE_COLLECTION);
		default:
			break;
		}
	}
}

package edu.ualberta.med.biobank.common.wrappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import edu.ualberta.med.biobank.common.peer.RequestPeer;
import edu.ualberta.med.biobank.common.peer.RequestSpecimenPeer;
import edu.ualberta.med.biobank.common.util.RequestSpecimenState;
import edu.ualberta.med.biobank.common.util.RequestState;
import edu.ualberta.med.biobank.common.wrappers.base.RequestBaseWrapper;
import edu.ualberta.med.biobank.model.Request;
import edu.ualberta.med.biobank.model.RequestSpecimen;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.WritableApplicationService;
import gov.nih.nci.system.query.hibernate.HQLCriteria;

public class RequestWrapper extends RequestBaseWrapper {

    private static final String NON_PROCESSED_ALIQUOTS_KEY = "nonProcessedRequestSpecimenCollection";

    private static final String PROCESSED_ALIQUOTS_KEY = "processedRequestSpecimens";

    private static final String UNAVAILABLE_ALIQUOTS_KEY = "unavailableRequestSpecimens";

    public RequestWrapper(WritableApplicationService appService) {
        super(appService);
    }

    public RequestWrapper(WritableApplicationService appService, Request request) {
        super(appService, request);
    }

    public void receiveAliquots(List<SpecimenWrapper> aliquots)
        throws Exception {
        List<RequestSpecimenWrapper> flagged = new ArrayList<RequestSpecimenWrapper>();
        List<RequestSpecimenWrapper> ras = getNonProcessedRequestSpecimenCollection();
        for (RequestSpecimenWrapper r : ras)
            for (SpecimenWrapper a : aliquots)
                if (r.getSpecimen().getInventoryId().equals(a.getInventoryId())) {
                    flagged.add(r);
                }
        flagAliquots(flagged);
    }

    public void flagAliquots(List<RequestSpecimenWrapper> scanned)
        throws Exception {
        for (RequestSpecimenWrapper a : scanned) {
            a.setState(RequestSpecimenState.PROCESSED_STATE.getId());
            a.persist();
        }
        propertiesMap.put(NON_PROCESSED_ALIQUOTS_KEY, null);
        propertiesMap.put(PROCESSED_ALIQUOTS_KEY, null);
    }

    public void receiveAliquot(String text) throws Exception {
        List<RequestSpecimenWrapper> ras = getNonProcessedRequestSpecimenCollection();
        for (RequestSpecimenWrapper r : ras)
            if (r.getSpecimen().getInventoryId().equals(text)) {
                flagAliquots(Arrays.asList(r));
                return;
            }
        throw new Exception("Aliquot " + text
            + " is not in the non-processed list.");

    }

    public List<RequestSpecimenWrapper> getNonProcessedRequestSpecimenCollection() {
        return getRequestSpecimenCollectionWithState(
            NON_PROCESSED_ALIQUOTS_KEY, true,
            RequestSpecimenState.NONPROCESSED_STATE);
    }

    public List<RequestSpecimenWrapper> getProcessedRequestSpecimenCollection() {
        return getRequestSpecimenCollectionWithState(PROCESSED_ALIQUOTS_KEY,
            true, RequestSpecimenState.PROCESSED_STATE);
    }

    @SuppressWarnings("unchecked")
    private List<RequestSpecimenWrapper> getRequestSpecimenCollectionWithState(
        String mapKey, boolean sort, RequestSpecimenState... states) {
        List<RequestSpecimenWrapper> dsaCollection = (List<RequestSpecimenWrapper>) propertiesMap
            .get(mapKey);
        if (dsaCollection == null) {
            Collection<RequestSpecimenWrapper> children = getRequestSpecimenCollection(sort);
            if (children != null) {
                dsaCollection = new ArrayList<RequestSpecimenWrapper>();
                for (RequestSpecimenWrapper dsa : children) {
                    boolean hasState = false;
                    for (RequestSpecimenState state : states) {
                        if (state.getId().equals(dsa.getState())) {
                            hasState = true;
                            break;
                        }
                    }
                    if (hasState)
                        dsaCollection.add(dsa);
                }
                propertiesMap.put(mapKey, dsaCollection);
            }
            if ((dsaCollection != null) && sort)
                Collections.sort(dsaCollection);
        }
        return dsaCollection;
    }

    public List<RequestSpecimenWrapper> getUnavailableRequestSpecimenCollection() {
        return getRequestSpecimenCollectionWithState(UNAVAILABLE_ALIQUOTS_KEY,
            true, RequestSpecimenState.UNAVAILABLE_STATE);
    }

    public void resetStateLists() {
        propertiesMap.put(UNAVAILABLE_ALIQUOTS_KEY, null);
        propertiesMap.put(PROCESSED_ALIQUOTS_KEY, null);
        propertiesMap.put(NON_PROCESSED_ALIQUOTS_KEY, null);
    }

    public RequestSpecimenWrapper getRequestSpecimen(String inventoryId) {
        for (RequestSpecimenWrapper dsa : getRequestSpecimenCollection(false)) {
            if (dsa.getSpecimen().getInventoryId().equals(inventoryId))
                return dsa;
        }
        return null;
    }

    private static final String REQUEST_BY_NUMBER_QRY = "from "
        + Request.class.getName() + " where " + RequestPeer.ID.getName() + "=?";

    public static List<RequestWrapper> getRequestByNumber(
        WritableApplicationService appService, String requestNumber)
        throws ApplicationException {
        HQLCriteria criteria = new HQLCriteria(REQUEST_BY_NUMBER_QRY,
            Arrays.asList(new Object[] { Integer.parseInt(requestNumber) }));
        List<Request> shipments = appService.query(criteria);
        List<RequestWrapper> wrappers = new ArrayList<RequestWrapper>();
        for (Request s : shipments) {
            wrappers.add(new RequestWrapper(appService, s));
        }
        return wrappers;
    }

    private static final String IS_ALL_PROCESSED_QRY = "select count(*) from "
        + RequestSpecimen.class.getName() + " as ra where ra."
        + RequestSpecimenPeer.STATE.getName() + "=?" + " and ra."
        + Property.concatNames(RequestSpecimenPeer.REQUEST, RequestPeer.ID)
        + "=?";

    public boolean isAllProcessed() {
        // using the collection was too slow
        List<Object> results = null;
        HQLCriteria c = new HQLCriteria(IS_ALL_PROCESSED_QRY,
            Arrays.asList(new Object[] {
                RequestSpecimenState.NONPROCESSED_STATE.getId(), getId() }));
        try {
            results = appService.query(c);
        } catch (ApplicationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return 0 == (Long) results.get(0);
    }

    public void setState(RequestState state) {
        setState(state.getId());
    }

}

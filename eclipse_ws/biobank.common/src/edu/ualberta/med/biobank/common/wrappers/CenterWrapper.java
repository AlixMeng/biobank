package edu.ualberta.med.biobank.common.wrappers;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import edu.ualberta.med.biobank.common.exception.BiobankException;
import edu.ualberta.med.biobank.common.exception.BiobankQueryResultSizeException;
import edu.ualberta.med.biobank.common.peer.AddressPeer;
import edu.ualberta.med.biobank.common.peer.CenterPeer;
import edu.ualberta.med.biobank.common.peer.CollectionEventPeer;
import edu.ualberta.med.biobank.common.peer.ProcessingEventPeer;
import edu.ualberta.med.biobank.common.util.DateCompare;
import edu.ualberta.med.biobank.common.wrappers.internal.AddressWrapper;
import edu.ualberta.med.biobank.model.Center;
import edu.ualberta.med.biobank.model.CollectionEvent;
import edu.ualberta.med.biobank.model.ProcessingEvent;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.WritableApplicationService;
import gov.nih.nci.system.query.hibernate.HQLCriteria;

public abstract class CenterWrapper<E extends Center> extends ModelWrapper<E> {

    public CenterWrapper(WritableApplicationService appService) {
        super(appService);
    }

    public CenterWrapper(WritableApplicationService appService, E c) {
        super(appService, c);
    }

    public String getName() {
        return getProperty(CenterPeer.NAME);
    }

    public void setName(String name) {
        setProperty(CenterPeer.NAME, name);
    }

    public String getNameShort() {
        return getProperty(CenterPeer.NAME_SHORT);
    }

    public void setNameShort(String name) {
        setProperty(CenterPeer.NAME_SHORT, name);
    }

    public AddressWrapper getAddress() {
        return getWrappedProperty(CenterPeer.ADDRESS, AddressWrapper.class);
    }

    private AddressWrapper initAddress() {
        AddressWrapper address = getAddress();
        if (address == null) {
            address = new AddressWrapper(appService);
            setAddress(address);
        }
        return address;
    }

    public void setAddress(AddressWrapper address) {
        setWrappedProperty(CenterPeer.ADDRESS, address);
    }

    public String getStreet1() {
        return getProperty(getAddress(), AddressPeer.STREET1);
    }

    public void setStreet1(String street1) {
        setProperty(initAddress(), AddressPeer.STREET1, street1);
    }

    public String getStreet2() {
        return getProperty(getAddress(), AddressPeer.STREET2);
    }

    public void setStreet2(String street2) {
        setProperty(initAddress(), AddressPeer.STREET2, street2);
    }

    public String getCity() {
        return getProperty(getAddress(), AddressPeer.CITY);
    }

    public void setCity(String city) {
        setProperty(initAddress(), AddressPeer.CITY, city);
    }

    public String getProvince() {
        return getProperty(getAddress(), AddressPeer.PROVINCE);
    }

    public void setProvince(String province) {
        setProperty(initAddress(), AddressPeer.PROVINCE, province);
    }

    public String getPostalCode() {
        return getProperty(getAddress(), AddressPeer.POSTAL_CODE);
    }

    public void setPostalCode(String postalCode) {
        setProperty(initAddress(), AddressPeer.POSTAL_CODE, postalCode);
    }

    public ActivityStatusWrapper getActivityStatus() {
        return getWrappedProperty(CenterPeer.ACTIVITY_STATUS,
            ActivityStatusWrapper.class);
    }

    public void setActivityStatus(ActivityStatusWrapper activityStatus) {
        setWrappedProperty(CenterPeer.ACTIVITY_STATUS, activityStatus);
    }

    public String getComment() {
        return getProperty(CenterPeer.COMMENT);
    }

    public void setComment(String comment) {
        setProperty(CenterPeer.COMMENT, comment);
    }

    public Collection<DispatchWrapper> getSrcDispatchCollection(boolean sort) {
        return getWrapperCollection(CenterPeer.SRC_DISPATCH_COLLECTION,
            DispatchWrapper.class, sort);
    }

    public void setSrcDispatchCollection(Collection<DispatchWrapper> collection) {
        setWrapperCollection(CenterPeer.SRC_DISPATCH_COLLECTION, collection);
    }

    public Collection<DispatchWrapper> getDstDispatchCollection(boolean sort) {
        return getWrapperCollection(CenterPeer.DST_DISPATCH_COLLECTION,
            DispatchWrapper.class, sort);
    }

    public void setDstDispatchCollection(Collection<DispatchWrapper> collection) {
        setWrapperCollection(CenterPeer.DST_DISPATCH_COLLECTION, collection);
    }

    public Collection<RequestWrapper> getRequestCollection(boolean sort) {
        return getWrapperCollection(CenterPeer.REQUEST_COLLECTION,
            RequestWrapper.class, sort);
    }

    public void setRequestCollection(Collection<RequestWrapper> collection) {
        setWrapperCollection(CenterPeer.REQUEST_COLLECTION, collection);
    }

    public static final String PROCESSING_EVENT_COUNT_QRY = "select count(proc) from "
        + ProcessingEvent.class.getName()
        + " as proc where "
        + Property.concatNames(ProcessingEventPeer.CENTER, CenterPeer.ID)
        + " = ?";

    public long getProcessingEventCount() throws ApplicationException,
        BiobankException {
        return getProcessingEventCount(false);
    }

    /**
     * fast = true will execute a hql query. fast = false will call the
     * getShipmentCollection().size method
     */
    public long getProcessingEventCount(boolean fast)
        throws ApplicationException, BiobankException {
        if (fast) {
            HQLCriteria criteria = new HQLCriteria(PROCESSING_EVENT_COUNT_QRY,
                Arrays.asList(new Object[] { getId() }));
            List<Long> results = appService.query(criteria);
            if (results.size() != 1) {
                throw new BiobankQueryResultSizeException();
            }
            return results.get(0);
        }
        List<CollectionEventWrapper> list = getCollectionEventCollection();
        if (list == null) {
            return 0;
        }
        return list.size();
    }

    public List<ProcessingEventWrapper> getProcessingEventCollection(
        boolean sort) {
        return getWrapperCollection(CenterPeer.PROCESSING_EVENT_COLLECTION,
            ProcessingEventWrapper.class, sort);
    }

    public List<ProcessingEventWrapper> getProcessingEventCollection() {
        return getProcessingEventCollection(true);
    }

    public void addProcessingEvents(
        List<ProcessingEventWrapper> newProcessingEvents) {
        addToWrapperCollection(CenterPeer.PROCESSING_EVENT_COLLECTION,
            newProcessingEvents);
    }

    public void removeProcessingEvents(List<ProcessingEventWrapper> removedPEs) {
        removeFromWrapperCollection(CenterPeer.PROCESSING_EVENT_COLLECTION,
            removedPEs);
    }

    public static final String COLLECTION_EVENT_COUNT_QRY = "select count(source) from "
        + CollectionEvent.class.getName()
        + " as source where "
        + Property
            .concatNames(CollectionEventPeer.SOURCE_CENTER, CenterPeer.ID)
        + " = ?";

    public long getCollectionEventCount() throws ApplicationException,
        BiobankException {
        return getCollectionEventCount(false);
    }

    /**
     * fast = true will execute a hql query. fast = false will call the
     * getShipmentCollection().size method
     */
    public long getCollectionEventCount(boolean fast)
        throws ApplicationException, BiobankException {
        if (fast) {
            HQLCriteria criteria = new HQLCriteria(COLLECTION_EVENT_COUNT_QRY,
                Arrays.asList(new Object[] { getId() }));
            List<Long> results = appService.query(criteria);
            if (results.size() != 1) {
                throw new BiobankQueryResultSizeException();
            }
            return results.get(0);
        }
        List<CollectionEventWrapper> list = getCollectionEventCollection();
        if (list == null) {
            return 0;
        }
        return list.size();
    }

    public List<CollectionEventWrapper> getCollectionEventCollection(
        boolean sort) {
        return getWrapperCollection(CenterPeer.COLLECTION_EVENT_COLLECTION,
            CollectionEventWrapper.class, sort);
    }

    public List<CollectionEventWrapper> getCollectionEventCollection() {
        return getCollectionEventCollection(true);
    }

    public void addCollectionEvents(
        List<CollectionEventWrapper> newCollectionEvents) {
        addToWrapperCollection(CenterPeer.COLLECTION_EVENT_COLLECTION,
            newCollectionEvents);
    }

    public void removeCollectionEvents(List<CollectionEventWrapper> removedCEs) {
        removeFromWrapperCollection(CenterPeer.COLLECTION_EVENT_COLLECTION,
            removedCEs);
    }

    /**
     * Search for a source in the center with the given date received
     */
    public CollectionEventWrapper getCollectionEvent(Date dateReceived) {
        List<CollectionEventWrapper> sources = getCollectionEventCollection();
        if (sources != null) {
            for (CollectionEventWrapper ship : sources) {
                if (DateCompare.compare(ship.getDateReceived(), dateReceived) == 0)
                    return ship;
            }
        }
        return null;
    }

    /**
     * Search for a source in the center with the given date received and
     * patient number.
     */
    public CollectionEventWrapper getCollectionEvent(Date dateReceived,
        String patientNumber) {
        List<CollectionEventWrapper> sources = getCollectionEventCollection();
        if (sources != null)
            for (CollectionEventWrapper source : sources)
                if (DateCompare.compare(source.getDateReceived(), dateReceived) == 0) {
                    List<PatientWrapper> patients = source
                        .getPatientCollection();
                    for (PatientWrapper p : patients)
                        if (p.getPnumber().equals(patientNumber))
                            return source;
                }
        return null;
    }

}

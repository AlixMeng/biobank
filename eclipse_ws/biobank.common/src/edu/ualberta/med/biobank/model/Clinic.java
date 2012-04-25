package edu.ualberta.med.biobank.model;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

import edu.ualberta.med.biobank.validator.constraint.NotUsed;
import edu.ualberta.med.biobank.validator.group.PreDelete;

/**
 * A collection site that collects biospecimens and transports them to a
 * repository site. Biospecimens are collected from patients that are
 * participating in a study.
 * 
 * NCI Term: Collecting laboratory. The laboratory that collects specimens from
 * a study subject.
 */
@Entity
@DiscriminatorValue("Clinic")
@NotUsed.List({
    @NotUsed(by = Study.class, property = "contacts.clinic", groups = PreDelete.class),
    @NotUsed(by = OriginInfo.class, property = "center", groups = PreDelete.class)
})
public class Clinic extends Center {
    private static final long serialVersionUID = 1L;

    private boolean sendsShipments = false;
    private Set<Contact> contacts = new HashSet<Contact>(0);

    @Column(name = "SENDS_SHIPMENTS")
    // TODO: rename to isSendsShipments
    public boolean getSendsShipments() {
        return this.sendsShipments;
    }

    public void setSendsShipments(boolean sendsShipments) {
        this.sendsShipments = sendsShipments;
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "clinic")
    public Set<Contact> getContacts() {
        return this.contacts;
    }

    public void setContacts(Set<Contact> contacts) {
        this.contacts = contacts;
    }
}
package edu.ualberta.med.biobank.model;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "SHIPMENT_INFO")
public class ShipmentInfo extends AbstractBiobankModel {
    private static final long serialVersionUID = 1L;

    private Date receivedAt;
    private Date packedAt;
    private String waybill;
    private String boxNumber;
    private ShippingMethod shippingMethod;

    @Column(name = "RECEIVED_AT")
    public Date getReceivedAt() {
        return this.receivedAt;
    }

    public void setReceivedAt(Date receivedAt) {
        this.receivedAt = receivedAt;
    }

    @NotNull(message = "{edu.ualberta.med.biobank.model.ShipmentInfo.packedAt.NotNull}")
    @Column(name = "PACKED_AT", nullable = false)
    public Date getPackedAt() {
        return this.packedAt;
    }

    public void setPackedAt(Date packedAt) {
        this.packedAt = packedAt;
    }

    @Column(name = "WAYBILL")
    public String getWaybill() {
        return this.waybill;
    }

    public void setWaybill(String waybill) {
        this.waybill = waybill;
    }

    @Column(name = "BOX_NUMBER")
    public String getBoxNumber() {
        return this.boxNumber;
    }

    public void setBoxNumber(String boxNumber) {
        this.boxNumber = boxNumber;
    }

    @NotNull(message = "{edu.ualberta.med.biobank.model.ShipmentInfo.shippingMethod.NotNull}")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SHIPPING_METHOD_ID", nullable = false)
    public ShippingMethod getShippingMethod() {
        return this.shippingMethod;
    }

    public void setShippingMethod(ShippingMethod shippingMethod) {
        this.shippingMethod = shippingMethod;
    }
}
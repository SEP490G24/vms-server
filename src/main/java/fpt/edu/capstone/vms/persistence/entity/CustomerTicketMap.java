package fpt.edu.capstone.vms.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fpt.edu.capstone.vms.constants.Constants;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(schema = "vms", name = "customer_ticket_map")
public class CustomerTicketMap extends AbstractBaseEntity<CustomerTicketMapPk> {

    @EmbeddedId
    private CustomerTicketMapPk customerTicketMapPk;

    @ManyToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    private Customer customerEntity;

    @ManyToOne
    @JoinColumn(name = "ticket_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    private Ticket ticketEntity;

    @Column(name = "reason_id")
    private Integer reasonId;

    @OneToOne
    @JoinColumn(name = "reason_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    private Reason reason;
    @Column(name = "reason_note")
    private String reasonNote;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Constants.StatusCustomerTicket status;

    @Column(name = "check_in_code", unique = true, updatable = false)
    private String checkInCode;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    @Column(name = "check_out", nullable = false, columnDefinition = "boolean default false")
    private boolean checkOut;

    @Column(name = "send_email", columnDefinition = "boolean default false")
    private boolean sendMail;
    @Column(name = "card_id")
    private String cardId;

    public CustomerTicketMap update(CustomerTicketMap customerTicketMap) {
        if (customerTicketMap.getCreatedBy() != null) this.setCreatedBy(customerTicketMap.getCreatedBy());
        if (customerTicketMap.getCreatedOn() != null) this.setCreatedOn(customerTicketMap.getCreatedOn());
        if (customerTicketMap.status != null) this.status = customerTicketMap.status;
        if (customerTicketMap.reason != null) this.reason = customerTicketMap.reason;
        if (customerTicketMap.reasonNote != null) this.reasonNote = customerTicketMap.reasonNote;
        if (customerTicketMap.cardId != null) this.cardId = customerTicketMap.cardId;
        return this;
    }

    @Override
    public void setId(CustomerTicketMapPk id) {
        this.customerTicketMapPk = id;
    }

    @Override
    public CustomerTicketMapPk getId() {
        return this.customerTicketMapPk;
    }

    @Override
    public String toString() {
        return "CustomerTicketMap{" +
            "customerTicketMapPk=" + customerTicketMapPk +
            ", reasonId=" + reasonId +
            ", reasonNote='" + reasonNote + '\'' +
            ", status=" + status +
            ", checkInCode='" + checkInCode + '\'' +
            ", checkInTime=" + checkInTime +
            ", checkOutTime=" + checkOutTime +
            ", checkOut=" + checkOut +
            ", cardId='" + cardId + '\'' +
            '}';
    }
}

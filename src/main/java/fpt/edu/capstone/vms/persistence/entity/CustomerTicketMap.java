package fpt.edu.capstone.vms.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fpt.edu.capstone.vms.constants.Constants;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.UUID;

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
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Customer customerEntity;

    @ManyToOne
    @JoinColumn(name = "ticket_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Ticket ticketEntity;

    @Column(name = "reason_id")
    private UUID reasonId;

    @OneToOne
    @JoinColumn(name = "reason_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    private Reason reason;

    @Column(name = "reason_note")
    private String reasonNote;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Constants.StatusTicket status;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    public CustomerTicketMap update(CustomerTicketMap customerTicketMap) {
        if (customerTicketMap.getCreatedBy() != null) this.setCreatedBy(customerTicketMap.getCreatedBy());
        if (customerTicketMap.getCreatedOn() != null) this.setCreatedOn(customerTicketMap.getCreatedOn());
        if (customerTicketMap.status != null) this.status = customerTicketMap.status;
        if (customerTicketMap.reason != null) this.reason = customerTicketMap.reason;
        if (customerTicketMap.reasonNote != null) this.reasonNote = customerTicketMap.reasonNote;
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
}

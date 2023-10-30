package fpt.edu.capstone.vms.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

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

    public CustomerTicketMap update(CustomerTicketMap customerTicketMap) {
        if (customerTicketMap.getCreatedBy() != null) this.setCreatedBy(customerTicketMap.getCreatedBy());
        if (customerTicketMap.getCreatedOn() != null) this.setCreatedOn(customerTicketMap.getCreatedOn());
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

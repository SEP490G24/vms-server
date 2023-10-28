package fpt.edu.capstone.vms.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class CustomerTicketMapPk implements Serializable {

    @Column(name = "ticket_id")
    private UUID ticketId;

    @Column(name = "customer_id")
    private UUID customerId;
}

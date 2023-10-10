package fpt.edu.capstone.vms.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(schema = "vms", name = "attachment")
@EqualsAndHashCode(callSuper = true)
public class Attachment extends AbstractBaseEntity<UUID> {

    @Id
    @Column(name = "id", length = 64)
    @GeneratedValue
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "code", length = 64)
    private String code;

    @Column(name = "type", length = 64)
    private String type;

    @Column(name = "description")
    private String description;

    @Column(name = "size")
    private String size;

    @Column(name = "type_file", length = 64)
    private String typeFile;

    @Column(name = "ticket_id")
    private UUID ticketId;

    @ManyToOne
    @JoinColumn(name = "ticket_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    private Ticket ticket;

    @Column(name = "companion_id")
    private UUID companionId;

    @ManyToOne
    @JoinColumn(name = "companion_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    private Companion companion;

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }
}

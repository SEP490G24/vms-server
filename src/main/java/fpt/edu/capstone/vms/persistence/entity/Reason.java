package fpt.edu.capstone.vms.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fpt.edu.capstone.vms.constants.Constants;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(schema = "vms", name = "reason")
@Builder
@EqualsAndHashCode(callSuper = true)
public class Reason extends AbstractBaseEntity<UUID> {

    @Id
    @Column(name = "id", length = 64)
    @GeneratedValue
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "code")
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private Constants.Reason type;

    @Column(name = "description")
    private String description;

    @Column(name = "enable")
    private Boolean enable;

    @Column(name = "site_id")
    private UUID siteId;

    @ManyToOne
    @JoinColumn(name = "site_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    private Site site;

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }

    public Reason update(Reason room) {
        if (room.name != null) this.name = room.name;
        if (room.code != null) this.code = room.code;
        if (room.description != null) this.description = room.description;
        if (room.enable != null) this.enable = room.enable;
        if (room.getCreatedBy() != null) this.setCreatedBy(room.getCreatedBy());
        if (room.getCreatedOn() != null) this.setCreatedOn(room.getCreatedOn());
        return this;
    }
}

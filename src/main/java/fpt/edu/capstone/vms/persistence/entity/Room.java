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
import lombok.Builder;
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
@Table(schema = "vms", name = "room_site")
@Builder
@EqualsAndHashCode(callSuper = true)
public class Room extends AbstractBaseEntity<UUID> {

    @Id
    @Column(name = "id", length = 64)
    @GeneratedValue
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "code", unique = true, updatable = false, nullable = false)
    private String code;

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

    public Room update(Room room) {
        if (room.name != null) this.name = room.name;
        if (room.description != null) this.description = room.description;
        if (room.enable != null) this.enable = room.enable;
        if (room.getCreatedBy() != null) this.setCreatedBy(room.getCreatedBy());
        if (room.getCreatedOn() != null) this.setCreatedOn(room.getCreatedOn());
        return this;
    }
}

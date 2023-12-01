package fpt.edu.capstone.vms.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
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

    @Column(name = "code", updatable = false, nullable = false)
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

    @Column(name = "device_id")
    private Integer deviceId;

    @OneToOne
    @JoinColumn(name = "device_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Device device;

    @Column(name = "is_security")
    private boolean isSecurity;

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
        if (room.deviceId != null) this.deviceId = room.deviceId;
        if (room.getCreatedBy() != null) this.setCreatedBy(room.getCreatedBy());
        if (room.getCreatedOn() != null) this.setCreatedOn(room.getCreatedOn());
        return this;
    }

    @Override
    public String toString() {
        return "Room{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", code='" + code + '\'' +
            ", description='" + description + '\'' +
            ", enable=" + enable +
            ", siteId=" + siteId +
            ", deviceId=" + deviceId +
            ", isSecurity=" + isSecurity +
            '}';
    }
}

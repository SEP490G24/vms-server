package fpt.edu.capstone.vms.persistence.entity;

import fpt.edu.capstone.vms.constants.Constants;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(schema = "vms", name = "reason")
@Builder
@EqualsAndHashCode(callSuper = true)
public class Reason extends AbstractBaseEntity<Integer> {

    @Id
    @Column(name = "id", length = 64)
    @GeneratedValue
    private Integer id;

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

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
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

    @Override
    public String toString() {
        return "Reason{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", code='" + code + '\'' +
            ", type=" + type +
            ", description='" + description + '\'' +
            ", enable=" + enable +
            '}';
    }
}

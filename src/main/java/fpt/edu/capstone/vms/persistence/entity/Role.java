package fpt.edu.capstone.vms.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.util.UUID;


@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(schema = "vms", name = "role")
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Role extends AbstractBaseEntity<UUID> {

    @Id
    @Column(name = "id", length = 64)
    @GeneratedValue
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "code")
    private String code;

    @Column(name = "description")
    private String description;

    @Column(name = "enable")
    private Boolean enable;

    @Column(name = "is_static_role")
    private Boolean isStaticRole;

    @Column(name = "site_id")
    private UUID siteId;

    @ManyToOne
    @JoinColumn(name = "site_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    private Site site;

    public Role update(Role roleEntity) {
        if (roleEntity.name != null) this.name = roleEntity.name;
        if (roleEntity.code != null) this.code = roleEntity.code;
        if (roleEntity.isStaticRole != null) this.isStaticRole = roleEntity.isStaticRole;
        if (roleEntity.description != null) this.description = roleEntity.description;
        if (roleEntity.enable != null) this.enable = roleEntity.enable;
        if (roleEntity.getCreatedBy() != null) this.setCreatedBy(roleEntity.getCreatedBy());
        if (roleEntity.getCreatedOn() != null) this.setCreatedOn(roleEntity.getCreatedOn());
        return this;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }
}


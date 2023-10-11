package fpt.edu.capstone.vms.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
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
@Table(schema = "vms", name = "organization")
@EqualsAndHashCode(callSuper = true)
public class Organization extends AbstractBaseEntity<UUID> {

    @Id
    @Column(name = "id", length = 64)
    @GeneratedValue
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "code")
    private String code;

    @Column(name = "website")
    private String website;

    @Column(name = "representative")
    private String representative;

    @Column(name = "description")
    private String description;

    @Column(name = "enable")
    private Boolean enable;

    public Organization update(Organization organizationEntity) {
        if (organizationEntity.name != null) this.name = organizationEntity.name;
        if (organizationEntity.code != null) this.code = organizationEntity.code;
        if (organizationEntity.representative != null) this.representative = organizationEntity.representative;
        if (organizationEntity.website != null) this.website = organizationEntity.website;
        if (organizationEntity.description != null) this.description = organizationEntity.description;
        if (organizationEntity.enable != null) this.enable = organizationEntity.enable;
        if (organizationEntity.getCreatedBy() != null) this.setCreatedBy(organizationEntity.getCreatedBy());
        if (organizationEntity.getCreatedOn() != null) this.setCreatedOn(organizationEntity.getCreatedOn());
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

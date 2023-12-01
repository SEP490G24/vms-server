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
@Table(schema = "vms", name = "template")
@EqualsAndHashCode(callSuper = true)
@Builder
public class Template extends AbstractBaseEntity<UUID> {

    @Id
    @Column(name = "id", length = 64)
    @GeneratedValue
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "code", updatable = false, nullable = false)
    private String code;

    @Column(name = "subject")
    private String subject;

    @Column(name = "description")
    private String description;

    @Column(name = "body", columnDefinition = "text", length = 10000)
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private Constants.TemplateType type;

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

    public Template update(Template template) {
        if (template.name != null) this.name = template.name;
        if (template.subject != null) this.subject = template.subject;
        if (template.body != null) this.body = template.body;
        if (template.type != null) this.type = template.type;
        if (template.description != null) this.description = template.description;
        if (template.enable != null) this.enable = template.enable;
        if (template.getCreatedBy() != null) this.setCreatedBy(template.getCreatedBy());
        if (template.getCreatedOn() != null) this.setCreatedOn(template.getCreatedOn());
        return this;
    }

    @Override
    public String toString() {
        return "Template{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", code='" + code + '\'' +
            ", subject='" + subject + '\'' +
            ", description='" + description + '\'' +
            ", body='" + body + '\'' +
            ", type=" + type +
            ", enable=" + enable +
            ", siteId=" + siteId +
            '}';
    }
}

package fpt.edu.capstone.vms.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
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
@Table(schema = "vms", name = "template_site_map")
public class TemplateSiteMap extends AbstractBaseEntity<TemplateSiteMapPk> {

    @EmbeddedId
    private TemplateSiteMapPk templateSiteMapPk;

    @ManyToOne
    @JoinColumn(name = "template_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    private Template templateEntity;

    @ManyToOne
    @JoinColumn(name = "site_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    private Site siteEntity;

    @Column(name = "description")
    private String description;

    @Override
    public void setId(TemplateSiteMapPk id) {
        this.templateSiteMapPk = id;
    }

    @Override
    public TemplateSiteMapPk getId() {
        return this.templateSiteMapPk;
    }
}

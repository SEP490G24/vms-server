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
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(schema = "vms", name = "price_package_site_map")
public class PricePackageSiteMap extends AbstractBaseEntity<PricePackageSiteMapPk> {

    @EmbeddedId
    private PricePackageSiteMapPk pricePackageSiteMapPk;

    @ManyToOne
    @JoinColumn(name = "price_package_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PricePackage pricePackageEntity;

    @ManyToOne
    @JoinColumn(name = "site_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Site siteEntity;

    @Column(name = "description")
    private String description;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "status")
    private Boolean status;
    @Override
    public void setId(PricePackageSiteMapPk id) {
        this.pricePackageSiteMapPk = id;
    }

    @Override
    public PricePackageSiteMapPk getId() {
        return this.pricePackageSiteMapPk;
    }
}

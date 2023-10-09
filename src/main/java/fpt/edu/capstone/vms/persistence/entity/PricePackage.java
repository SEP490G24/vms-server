package fpt.edu.capstone.vms.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MapKey;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(schema = "vms", name = "price_package")
@EqualsAndHashCode(callSuper = true)
public class PricePackage extends AbstractBaseEntity<UUID> {

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

    @Column(name = "number_user")
    private String numberUser;

    @Column(name = "number_ticket")
    private String numberTicket;

    @Column(name = "price")
    private Double price;

    @Column(name = "duration")
    private String duration;

    @Column(name = "status")
    private Boolean status;

    @Column(name = "type_card")
    private Boolean typeCard;

    @OneToMany(mappedBy = "pricePackageEntity", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @MapKey(name = "pricePackageSiteMapPk.pricePackageId")
    private Map<UUID, PricePackageSiteMap> pricePackageSiteMaps;

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }
}

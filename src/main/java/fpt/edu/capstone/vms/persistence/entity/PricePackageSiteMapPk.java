package fpt.edu.capstone.vms.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class PricePackageSiteMapPk implements Serializable {

    @Column(name = "price_package_id")
    private UUID pricePackageId;

    @Column(name = "site_id")
    private UUID siteId;
}

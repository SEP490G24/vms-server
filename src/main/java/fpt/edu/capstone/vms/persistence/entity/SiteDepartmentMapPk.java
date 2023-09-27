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
public class SiteDepartmentMapPk implements Serializable {

    @Column(name = "department_id")
    private String departmentId;

    @Column(name = "site_id")
    private String siteId;
}

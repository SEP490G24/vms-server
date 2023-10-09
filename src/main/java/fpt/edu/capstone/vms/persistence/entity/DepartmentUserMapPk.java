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
public class DepartmentUserMapPk implements Serializable {

    @Column(name = "department_id")
    private UUID departmentId;

    @Column(name = "username")
    private String username;
}

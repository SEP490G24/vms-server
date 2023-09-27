package fpt.edu.capstone.vms.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
public class Organization extends AbstractBaseEntity implements ModelBaseInterface<String> {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Column(name = "name")
    private String name;

    @Column(name = "code")
    private String code;

    @Column(name = "website")
    private String website;

    @Column(name = "representative")
    private String representative;

    @Column(name = "enable")
    private Boolean enable;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }
}

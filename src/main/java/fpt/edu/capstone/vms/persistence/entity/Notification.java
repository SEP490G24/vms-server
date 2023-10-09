package fpt.edu.capstone.vms.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(schema = "vms", name = "notification")
@EqualsAndHashCode(callSuper = true)
public class Notification extends AbstractBaseEntity<UUID> {

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

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }
}

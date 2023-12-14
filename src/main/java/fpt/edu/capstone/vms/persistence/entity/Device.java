package fpt.edu.capstone.vms.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fpt.edu.capstone.vms.constants.Constants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
@Table(schema = "vms", name = "device")
@Builder
@EqualsAndHashCode(callSuper = true)
public class Device extends AbstractBaseEntity<Integer> {

    @Column(name = "id")
    @Id
    @GeneratedValue
    private Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "code")
    private String code;

    @Column(name = "description")
    private String description;

    @Column(name = "enable")
    private Boolean enable;

    @Column(name = "site_id")
    private UUID siteId;

    @Column(name = "mac_ip")
    private String macIp;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type")
    private Constants.DeviceType deviceType;

    @ManyToOne
    @JoinColumn(name = "site_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    private Site site;


    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public Integer getId() {
        return id;
    }

    public Device update(Device device) {
        if (device.getName() != null) {
            this.name = device.getName();
        }
        if (device.getCode() != null) {
            this.code = device.getCode();
        }
        if (device.getDescription() != null) {
            this.description = device.getDescription();
        }
        if (device.getMacIp() != null) {
            this.macIp = device.getMacIp();
        }
        if (device.getCreatedBy() != null) {
            this.setCreatedBy(device.getCreatedBy());
        }
        if (device.getLastUpdatedBy() != null) {
            this.setLastUpdatedBy(device.getLastUpdatedBy());
        }
        if (device.getDeviceType() != null) {
            this.deviceType = device.getDeviceType();
        }
        if (device.getEnable() != null) {
            this.enable = device.getEnable();
        }
        return this;
    }

    @Override
    public String toString() {
        return "Device{" +
            "id=" + id +
            ", name='" + name + '\'' +
            ", code='" + code + '\'' +
            ", description='" + description + '\'' +
            ", enable=" + enable +
            ", siteId=" + siteId +
            ", macIp='" + macIp + '\'' +
            ", deviceType=" + deviceType +
            '}';
    }
}

package fpt.edu.capstone.vms.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fpt.edu.capstone.vms.constants.Constants;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(schema = "vms", name = "ticket")
@EqualsAndHashCode(callSuper = true)
public class Ticket extends AbstractBaseEntity<UUID> {

    @Id
    @Column(name = "id", length = 64)
    @GeneratedValue
    private UUID id;

    @Column(name = "visitor_name")
    private String visitor_name;

    @Column(name = "code")
    private String code;

    @Min(value = 1)
    @Max(value = 12)
    @Column(name = "identification_number")
    private Integer identificationNumber;

    @Column(name = "license_plate_number", length = 64)
    private String licensePlateNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "gender")
    private Boolean gender;

    @Column(name = "purpose")
    private String purpose;

    @Column(name = "purpose_other")
    private String purposeOther;

    @Column(name = "expected_date")
    private LocalDateTime expectedDate;

    @Column(name = "expected_time")
    private String expectedTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "comment")
    private String comment;

    @Column(name = "promise")
    private Boolean promise;

    @Column(name = "privacy")
    private Boolean privacy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Constants.StatusTicket status;

    @Column(name = "is_bookmark")
    private String isBookmark;

    @Column(name = "username")
    private String username;

    @ManyToOne
    @JoinColumn(name = "username", referencedColumnName = "username", insertable = false, updatable = false)
    @JsonIgnore
    private User user;

    @Column(name = "room_id")
    private UUID roomId;

    @ManyToOne
    @JoinColumn(name = "room_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    private Room room;

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }
}

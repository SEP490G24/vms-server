package fpt.edu.capstone.vms.persistence.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import fpt.edu.capstone.vms.constants.Constants;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.UUID;


@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(schema = "vms", name = "card")
@EqualsAndHashCode(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Card extends AbstractBaseEntity<UUID> {

    @Id
    @Column(name = "id", length = 64)
    @GeneratedValue
    private UUID id;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "meeting_id")
    private UUID meetingId;


    @Column(name = "recording_time")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATETIME_PATTERN)
    private LocalDateTime recordingTime;

    @Column(name = "reading_time")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATETIME_PATTERN)
    private LocalDateTime readingTime;

    @Column(name = "site_id")
    private UUID siteId;

    @ManyToOne
    @JoinColumn(name = "site_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    private Site site;

//    public Card update(Card roleEntity) {
//        if (roleEntity.name != null) this.name = roleEntity.name;
//        if (roleEntity.code != null) this.code = roleEntity.code;
//        if (roleEntity.isStaticRole != null) this.isStaticRole = roleEntity.isStaticRole;
//        if (roleEntity.description != null) this.description = roleEntity.description;
//        if (roleEntity.enable != null) this.enable = roleEntity.enable;
//        if (roleEntity.getCreatedBy() != null) this.setCreatedBy(roleEntity.getCreatedBy());
//        if (roleEntity.getCreatedOn() != null) this.setCreatedOn(roleEntity.getCreatedOn());
//        return this;
//    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }
}


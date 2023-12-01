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

    @Column(name = "card_id")
    private String cardId;

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

    @Column(name = "enable")
    private Boolean enable;

    @Column(name = "site_id")
    private UUID siteId;

    @ManyToOne
    @JoinColumn(name = "site_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    private Site site;

    public Card update(Card cardEntity) {
        if (cardEntity.cardId != null) this.cardId = cardEntity.cardId;
        if (cardEntity.customerId != null) this.customerId = cardEntity.customerId;
        if (cardEntity.meetingId != null) this.meetingId = cardEntity.meetingId;
        if (cardEntity.readingTime != null) this.readingTime = cardEntity.readingTime;
        if (cardEntity.recordingTime != null) this.recordingTime = cardEntity.recordingTime;
        if (cardEntity.enable != null) this.enable = cardEntity.enable;
        if (cardEntity.getCreatedBy() != null) this.setCreatedBy(cardEntity.getCreatedBy());
        if (cardEntity.getCreatedOn() != null) this.setCreatedOn(cardEntity.getCreatedOn());
        return this;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Card{" +
            "id=" + id +
            ", cardId='" + cardId + '\'' +
            ", customerId=" + customerId +
            ", meetingId=" + meetingId +
            ", recordingTime=" + recordingTime +
            ", readingTime=" + readingTime +
            ", enable=" + enable +
            ", siteId=" + siteId +
            '}';
    }
}


package fpt.edu.capstone.vms.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fpt.edu.capstone.vms.constants.Constants;
import jakarta.persistence.*;
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

    @Column(name = "code", unique = true, updatable = false, nullable = false)
    private String code;

    @Column(name = "name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose")
    private Constants.Purpose purpose;

    @Column(name = "purpose_note")
    private String purposeNote;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Constants.StatusTicket status;

    @Column(name = "is_bookmark", nullable = false, columnDefinition = "boolean default false")
    private boolean isBookmark;

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

    @Column(name = "settings", length = 500)
    private String settings;

    @Column(name = "site_id")
    private String siteId;

    @Column(name = "reason_id")
    private Integer reasonId;

    @OneToOne
    @JoinColumn(name = "reason_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    private Reason reason;

    @Column(name = "reason_note")
    private String reasonNote;

    public Ticket update(Ticket ticketEntity) {
        if (ticketEntity.name != null) this.name = ticketEntity.name;
        if (ticketEntity.code != null) this.code = ticketEntity.code;
        if (ticketEntity.purpose != null) this.purpose = ticketEntity.purpose;
        if (ticketEntity.purposeNote != null) this.purposeNote = ticketEntity.purposeNote;
        if (ticketEntity.startTime != null) this.startTime = ticketEntity.startTime;
        if (ticketEntity.endTime != null) this.endTime = ticketEntity.endTime;
        if (ticketEntity.description != null) this.description = ticketEntity.description;
        if (ticketEntity.roomId != null) this.roomId = ticketEntity.roomId;
        if (ticketEntity.reasonId != null) this.reasonId = ticketEntity.reasonId;
        if (ticketEntity.reasonNote != null) this.reasonNote = ticketEntity.reasonNote;
        if (ticketEntity.getCreatedBy() != null) this.setCreatedBy(ticketEntity.getCreatedBy());
        if (ticketEntity.getCreatedOn() != null) this.setCreatedOn(ticketEntity.getCreatedOn());
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
        return "Ticket{" +
            "id=" + id +
            ", code='" + code + '\'' +
            ", name='" + name + '\'' +
            ", purpose=" + purpose +
            ", purposeNote='" + purposeNote + '\'' +
            ", startTime=" + startTime +
            ", endTime=" + endTime +
            ", description='" + description + '\'' +
            ", status=" + status +
            ", isBookmark=" + isBookmark +
            ", username='" + username + '\'' +
            ", roomId=" + roomId +
            ", settings='" + settings + '\'' +
            ", siteId='" + siteId + '\'' +
            ", reasonId=" + reasonId +
            ", reasonNote='" + reasonNote + '\'' +
            '}';
    }
}

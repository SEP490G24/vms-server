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
import java.util.Map;
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

    @Column(name = "code")
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose")
    private Constants.Purpose purpose;

    @Column(name = "purpose_note")
    private String purposeNote;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "comment")
    private String comment;

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

    @Column(name = "settings", length = 500)
    private String settings;

    @Column(name = "template_id")
    private UUID templateId;

    @ManyToOne
    @JoinColumn(name = "template_id", referencedColumnName = "id", insertable = false, updatable = false)
    @JsonIgnore
    private Template template;

    @OneToMany(mappedBy = "ticketEntity", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @MapKey(name = "customerTicketMapPk.ticketId")
    @JsonIgnore
    private Map<UUID, CustomerTicketMap> customerTicketMaps;

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }
}

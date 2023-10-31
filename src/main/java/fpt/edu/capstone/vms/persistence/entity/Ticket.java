package fpt.edu.capstone.vms.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fpt.edu.capstone.vms.constants.Constants;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapKey;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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

    @Column(name = "is_bookmark", nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
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

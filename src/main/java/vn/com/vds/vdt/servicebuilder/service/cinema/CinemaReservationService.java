package vn.com.vds.vdt.servicebuilder.service.cinema;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.com.vds.vdt.servicebuilder.common.enums.DataType;
import vn.com.vds.vdt.servicebuilder.controller.dto.cinema.CinemaDtos.MovieResponse;
import vn.com.vds.vdt.servicebuilder.controller.dto.cinema.CinemaDtos.ReservationResponse;
import vn.com.vds.vdt.servicebuilder.controller.dto.cinema.CinemaDtos.ShowtimeResponse;
import vn.com.vds.vdt.servicebuilder.entity.AttributeDefinition;
import vn.com.vds.vdt.servicebuilder.entity.AttributeValue;
import vn.com.vds.vdt.servicebuilder.entity.EntityType;
import vn.com.vds.vdt.servicebuilder.entity.Instance;
import vn.com.vds.vdt.servicebuilder.entity.Relationship;
import vn.com.vds.vdt.servicebuilder.entity.RelationshipType;
import vn.com.vds.vdt.servicebuilder.repository.AttributeDefinitionRepository;
import vn.com.vds.vdt.servicebuilder.repository.AttributeValueRepository;
import vn.com.vds.vdt.servicebuilder.repository.EntityTypeRepository;
import vn.com.vds.vdt.servicebuilder.repository.InstanceRepository;
import vn.com.vds.vdt.servicebuilder.repository.RelationshipRepository;
import vn.com.vds.vdt.servicebuilder.repository.RelationshipTypeRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CinemaReservationService {
    public static final String ERR_BAD_REQUEST = "CINEMA400";
    public static final String ERR_NOT_FOUND = "CINEMA404";
    public static final String ERR_SOLD_OUT = "CINEMA409";

    private final EntityTypeRepository entityTypeRepository;
    private final AttributeDefinitionRepository attributeDefinitionRepository;
    private final AttributeValueRepository attributeValueRepository;
    private final InstanceRepository instanceRepository;
    private final RelationshipTypeRepository relationshipTypeRepository;
    private final RelationshipRepository relationshipRepository;

    @Transactional
    public List<MovieResponse> getMovies() {
        EntityType movieType = type(CinemaSeedService.MOVIE);
        EntityType showtimeType = type(CinemaSeedService.SHOWTIME);
        RelationshipType movieHasShowtime = relationshipTypeRepository.findByName("movie_has_showtime").orElseThrow();

        Map<Long, Map<String, Object>> showtimeAttrs = instances(showtimeType).stream()
                .collect(Collectors.toMap(Instance::getEntityId, this::attributes));

        return instances(movieType).stream()
                .map(movie -> {
                    Map<String, Object> attrs = attributes(movie);
                    List<ShowtimeResponse> showtimes = relationshipRepository
                            .findByRelationshipTypeIdAndFromEntityId(movieHasShowtime.getRelationshipTypeId(), movie.getEntityId())
                            .stream()
                            .map(Relationship::getToEntityId)
                            .map(showtimeId -> showtime(showtimeId, showtimeAttrs.get(showtimeId)))
                            .filter(Objects::nonNull)
                            .sorted(Comparator.comparing(ShowtimeResponse::getStartsAt))
                            .toList();
                    return MovieResponse.builder()
                            .id(movie.getEntityId())
                            .title(string(attrs, "title"))
                            .genre(string(attrs, "genre"))
                            .rating(string(attrs, "rating"))
                            .durationMinutes(integer(attrs, "duration_minutes"))
                            .synopsis(string(attrs, "synopsis"))
                            .posterUrl(string(attrs, "poster_url"))
                            .showtimes(showtimes)
                            .build();
                })
                .toList();
    }

    @Transactional
    public ReservationResponse reserve(Long showtimeId, Integer quantity) {
        if (showtimeId == null || quantity == null || quantity <= 0) {
            throw new CinemaReservationException(ERR_BAD_REQUEST, "Showtime and positive ticket quantity are required");
        }

        Instance showtime = instanceRepository.lockById(showtimeId)
                .orElseThrow(() -> new CinemaReservationException(ERR_NOT_FOUND, "Showtime not found: " + showtimeId));
        EntityType showtimeType = type(CinemaSeedService.SHOWTIME);
        if (!Objects.equals(showtime.getEntityTypeId(), showtimeType.getEntityTypeId())) {
            throw new CinemaReservationException(ERR_NOT_FOUND, "Showtime not found: " + showtimeId);
        }

        Map<String, Object> showtimeAttrs = attributes(showtime);
        int available = integer(showtimeAttrs, "available_capacity");
        if (quantity > available) {
            throw new CinemaReservationException(ERR_SOLD_OUT, "Not enough seats available");
        }

        List<Instance> seats = availableSeats(showtime, showtimeAttrs, quantity);
        if (seats.size() < quantity) {
            throw new CinemaReservationException(ERR_SOLD_OUT, "Not enough seats available");
        }

        int remaining = available - quantity;
        setValue(showtime, "available_capacity", remaining);

        Instance reservation = instanceRepository.save(Instance.builder()
                .entityTypeId(type(CinemaSeedService.RESERVATION).getEntityTypeId())
                .build());
        List<String> seatCodes = seats.stream().map(seat -> string(attributes(seat), "code")).toList();
        setValue(reservation, "showtime_id", showtimeId);
        setValue(reservation, "quantity", quantity);
        setValue(reservation, "allocated_seats", String.join(",", seatCodes));
        setValue(reservation, "status", "RESERVED");
        setValue(reservation, "reserved_at", LocalDateTime.now());

        relation("reservation_for_showtime", reservation, showtime);
        seats.forEach(seat -> relation("reservation_allocates_seat", reservation, seat));

        return ReservationResponse.builder()
                .reservationId(reservation.getEntityId())
                .showtimeId(showtimeId)
                .quantity(quantity)
                .allocatedSeats(seatCodes)
                .remainingCapacity(remaining)
                .status("RESERVED")
                .build();
    }

    private List<Instance> availableSeats(Instance showtime, Map<String, Object> showtimeAttrs, int quantity) {
        Long roomId = longValue(showtimeAttrs, "room_id");
        RelationshipType roomHasSeat = relationshipTypeRepository.findByName("room_has_seat").orElseThrow();
        RelationshipType reservationForShowtime = relationshipTypeRepository.findByName("reservation_for_showtime").orElseThrow();
        RelationshipType reservationAllocatesSeat = relationshipTypeRepository.findByName("reservation_allocates_seat").orElseThrow();

        Set<Long> reservedSeatIds = relationshipRepository
                .findByRelationshipTypeIdAndToEntityId(reservationForShowtime.getRelationshipTypeId(), showtime.getEntityId())
                .stream()
                .map(Relationship::getFromEntityId)
                .flatMap(reservationId -> relationshipRepository
                        .findByRelationshipTypeIdAndFromEntityId(reservationAllocatesSeat.getRelationshipTypeId(), reservationId)
                        .stream())
                .map(Relationship::getToEntityId)
                .collect(Collectors.toSet());

        return relationshipRepository.findByRelationshipTypeIdAndFromEntityId(roomHasSeat.getRelationshipTypeId(), roomId)
                .stream()
                .map(Relationship::getToEntityId)
                .filter(seatId -> !reservedSeatIds.contains(seatId))
                .map(instanceRepository::findById)
                .flatMap(java.util.Optional::stream)
                .sorted(Comparator
                        .comparing((Instance seat) -> string(attributes(seat), "row_label"))
                        .thenComparing(seat -> integer(attributes(seat), "seat_number")))
                .limit(quantity)
                .toList();
    }

    private ShowtimeResponse showtime(Long showtimeId, Map<String, Object> attrs) {
        if (attrs == null) {
            return null;
        }
        Long roomId = longValue(attrs, "room_id");
        return ShowtimeResponse.builder()
                .id(showtimeId)
                .movieId(longValue(attrs, "movie_id"))
                .roomId(roomId)
                .roomName(roomName(roomId))
                .startsAt((LocalDateTime) attrs.get("starts_at"))
                .endsAt((LocalDateTime) attrs.get("ends_at"))
                .price(doubleValue(attrs, "price"))
                .totalCapacity(integer(attrs, "total_capacity"))
                .availableCapacity(integer(attrs, "available_capacity"))
                .build();
    }

    private String roomName(Long roomId) {
        return instanceRepository.findById(roomId)
                .map(this::attributes)
                .map(attrs -> string(attrs, "name"))
                .orElse("Room " + roomId);
    }

    private List<Instance> instances(EntityType type) {
        return instanceRepository.findEntitiesByEntityTypeId(type.getEntityTypeId());
    }

    private EntityType type(String name) {
        return entityTypeRepository.findByName(name).orElseThrow();
    }

    private Map<String, Object> attributes(Instance instance) {
        Map<Long, AttributeDefinition> definitions = attributeDefinitionRepository.findByEntityTypeId(instance.getEntityTypeId())
                .stream()
                .collect(Collectors.toMap(AttributeDefinition::getAttributeDefinitionId, def -> def));
        Map<String, Object> attrs = new LinkedHashMap<>();
        for (AttributeValue value : attributeValueRepository.findByEntityId(instance.getEntityId())) {
            AttributeDefinition def = definitions.get(value.getAttributeDefinitionId());
            if (def != null) {
                attrs.put(def.getName(), typedValue(def, value));
            }
        }
        return attrs;
    }

    private Object typedValue(AttributeDefinition def, AttributeValue value) {
        return switch (DataType.valueOf(def.getDataType())) {
            case STRING, UUID -> value.getStringValue();
            case INTEGER -> value.getNumberValue() == null ? null : value.getNumberValue().intValue();
            case LONG -> value.getNumberValue() == null ? null : value.getNumberValue().longValue();
            case DOUBLE -> value.getNumberValue();
            case BOOLEAN -> value.getBooleanValue();
            case DATE, DATETIME -> value.getDateValue();
        };
    }

    private void setValue(Instance instance, String name, Object raw) {
        AttributeDefinition def = attributeDefinitionRepository
                .findByNameAndEntityTypeId(name, instance.getEntityTypeId())
                .orElseThrow();
        AttributeValue value = attributeValueRepository
                .findByEntityIdAndAttributeDefinitionId(instance.getEntityId(), def.getAttributeDefinitionId())
                .orElseGet(() -> AttributeValue.builder()
                        .entityId(instance.getEntityId())
                        .attributeDefinitionId(def.getAttributeDefinitionId())
                        .build());
        value.setStringValue(null);
        value.setNumberValue(null);
        value.setDateValue(null);
        value.setBooleanValue(null);
        switch (DataType.valueOf(def.getDataType())) {
            case STRING, UUID -> value.setStringValue(String.valueOf(raw));
            case INTEGER, LONG, DOUBLE -> value.setNumberValue(Double.valueOf(String.valueOf(raw)));
            case BOOLEAN -> value.setBooleanValue(Boolean.valueOf(String.valueOf(raw)));
            case DATE, DATETIME -> value.setDateValue((LocalDateTime) raw);
        }
        attributeValueRepository.save(value);
    }

    private void relation(String name, Instance from, Instance to) {
        RelationshipType type = relationshipTypeRepository.findByName(name).orElseThrow();
        relationshipRepository.findByRelationshipTypeIdAndFromEntityIdAndToEntityId(
                type.getRelationshipTypeId(),
                from.getEntityId(),
                to.getEntityId()
        ).orElseGet(() -> relationshipRepository.save(Relationship.builder()
                .relationshipTypeId(type.getRelationshipTypeId())
                .fromEntityId(from.getEntityId())
                .toEntityId(to.getEntityId())
                .build()));
    }

    private String string(Map<String, Object> attrs, String name) {
        return String.valueOf(attrs.getOrDefault(name, ""));
    }

    private Integer integer(Map<String, Object> attrs, String name) {
        Object value = attrs.get(name);
        return value instanceof Number number ? number.intValue() : Integer.valueOf(String.valueOf(value));
    }

    private Long longValue(Map<String, Object> attrs, String name) {
        Object value = attrs.get(name);
        return value instanceof Number number ? number.longValue() : Long.valueOf(String.valueOf(value));
    }

    private Double doubleValue(Map<String, Object> attrs, String name) {
        Object value = attrs.get(name);
        return value instanceof Number number ? number.doubleValue() : Double.valueOf(String.valueOf(value));
    }
}

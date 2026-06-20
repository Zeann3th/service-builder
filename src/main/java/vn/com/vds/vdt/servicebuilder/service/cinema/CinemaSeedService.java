package vn.com.vds.vdt.servicebuilder.service.cinema;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import vn.com.vds.vdt.servicebuilder.common.enums.DataType;
import vn.com.vds.vdt.servicebuilder.common.enums.RelationshipCardinality;
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
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.cinema.seed", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CinemaSeedService {
    public static final String MOVIE = "movie";
    public static final String ROOM = "cinema_room";
    public static final String SEAT = "seat";
    public static final String SHOWTIME = "showtime";
    public static final String RESERVATION = "ticket_reservation";

    private final EntityTypeRepository entityTypeRepository;
    private final AttributeDefinitionRepository attributeDefinitionRepository;
    private final AttributeValueRepository attributeValueRepository;
    private final InstanceRepository instanceRepository;
    private final RelationshipTypeRepository relationshipTypeRepository;
    private final RelationshipRepository relationshipRepository;

    @PostConstruct
    @Transactional
    public void seedOnStartup() {
        seed();
    }

    @Transactional
    public void seed() {
        Map<String, EntityType> types = seedTypes();
        seedRelationshipTypes(types);
        if (!instanceRepository.findEntitiesByEntityTypeId(types.get(MOVIE).getEntityTypeId()).isEmpty()) {
            return;
        }

        Instance roomA = create(types.get(ROOM), Map.of("name", "Room A", "row_count", 5, "seats_per_row", 8));
        Instance roomB = create(types.get(ROOM), Map.of("name", "Room B", "row_count", 4, "seats_per_row", 6));
        seedSeats(types, roomA, 5, 8);
        seedSeats(types, roomB, 4, 6);

        Instance dune = create(types.get(MOVIE), movie("Dune: Part Two", "Sci-Fi", "PG-13", 166,
                "Paul Atreides unites with Chani and the Fremen.", ""));
        Instance insideOut = create(types.get(MOVIE), movie("Inside Out 2", "Animation", "PG", 96,
                "Riley's emotions meet a new set of feelings.", ""));
        Instance oppenheimer = create(types.get(MOVIE), movie("Oppenheimer", "Drama", "R", 180,
                "The story of J. Robert Oppenheimer and the atomic bomb.", ""));

        LocalDateTime base = LocalDateTime.now().withSecond(0).withNano(0).plusHours(2);
        createShowtime(types, dune, roomA, base, 166, 95000D);
        createShowtime(types, dune, roomB, base.plusHours(3), 166, 85000D);
        createShowtime(types, insideOut, roomB, base.plusMinutes(30), 96, 70000D);
        createShowtime(types, oppenheimer, roomA, base.plusDays(1), 180, 90000D);
    }

    private Map<String, EntityType> seedTypes() {
        Map<String, EntityType> types = new LinkedHashMap<>();
        types.put(MOVIE, type(MOVIE, "Movie", Map.of(
                "title", DataType.STRING,
                "genre", DataType.STRING,
                "rating", DataType.STRING,
                "duration_minutes", DataType.INTEGER,
                "synopsis", DataType.STRING,
                "poster_url", DataType.STRING
        )));
        types.put(ROOM, type(ROOM, "Cinema Room", Map.of(
                "name", DataType.STRING,
                "row_count", DataType.INTEGER,
                "seats_per_row", DataType.INTEGER
        )));
        types.put(SEAT, type(SEAT, "Seat", Map.of(
                "room_id", DataType.LONG,
                "code", DataType.STRING,
                "row_label", DataType.STRING,
                "seat_number", DataType.INTEGER,
                "active", DataType.BOOLEAN
        )));
        types.put(SHOWTIME, type(SHOWTIME, "Showtime", Map.of(
                "movie_id", DataType.LONG,
                "room_id", DataType.LONG,
                "starts_at", DataType.DATETIME,
                "ends_at", DataType.DATETIME,
                "price", DataType.DOUBLE,
                "total_capacity", DataType.INTEGER,
                "available_capacity", DataType.INTEGER
        )));
        types.put(RESERVATION, type(RESERVATION, "Ticket Reservation", Map.of(
                "showtime_id", DataType.LONG,
                "quantity", DataType.INTEGER,
                "allocated_seats", DataType.STRING,
                "status", DataType.STRING,
                "reserved_at", DataType.DATETIME
        )));
        return types;
    }

    private EntityType type(String name, String displayName, Map<String, DataType> attributes) {
        EntityType type = entityTypeRepository.findByName(name)
                .orElseGet(() -> entityTypeRepository.save(EntityType.builder()
                        .name(name)
                        .displayName(displayName)
                        .isActive(true)
                        .schemaVersion(1L)
                        .build()));
        attributes.forEach((attrName, dataType) -> attributeDefinitionRepository
                .findByNameAndEntityTypeId(attrName, type.getEntityTypeId())
                .orElseGet(() -> attributeDefinitionRepository.save(AttributeDefinition.builder()
                        .entityTypeId(type.getEntityTypeId())
                        .name(attrName)
                        .displayName(toDisplayName(attrName))
                        .dataType(dataType.name())
                        .isRequired(true)
                        .build())));
        return type;
    }

    private void seedRelationshipTypes(Map<String, EntityType> types) {
        relationType("room_has_seat", types.get(ROOM), types.get(SEAT));
        relationType("movie_has_showtime", types.get(MOVIE), types.get(SHOWTIME));
        relationType("showtime_in_room", types.get(SHOWTIME), types.get(ROOM));
        relationType("reservation_for_showtime", types.get(RESERVATION), types.get(SHOWTIME));
        relationType("reservation_allocates_seat", types.get(RESERVATION), types.get(SEAT));
    }

    private RelationshipType relationType(String name, EntityType from, EntityType to) {
        return relationshipTypeRepository.findByName(name)
                .orElseGet(() -> relationshipTypeRepository.save(RelationshipType.builder()
                        .name(name)
                        .fromEntityTypeId(from.getEntityTypeId())
                        .toEntityTypeId(to.getEntityTypeId())
                        .cardinality(RelationshipCardinality.ONE_TO_MANY.name())
                        .isRequired(false)
                        .build()));
    }

    private void seedSeats(Map<String, EntityType> types, Instance room, int rows, int seatsPerRow) {
        RelationshipType roomHasSeat = relationshipTypeRepository.findByName("room_has_seat").orElseThrow();
        for (int rowIndex = 0; rowIndex < rows; rowIndex++) {
            String row = String.valueOf((char) ('A' + rowIndex));
            for (int seatNumber = 1; seatNumber <= seatsPerRow; seatNumber++) {
                Instance seat = create(types.get(SEAT), Map.of(
                        "room_id", room.getEntityId(),
                        "code", row + seatNumber,
                        "row_label", row,
                        "seat_number", seatNumber,
                        "active", true
                ));
                relation(roomHasSeat, room, seat);
            }
        }
    }

    private void createShowtime(Map<String, EntityType> types, Instance movie, Instance room, LocalDateTime startsAt,
                                int durationMinutes, Double price) {
        int capacity = getInt(room, "row_count") * getInt(room, "seats_per_row");
        Instance showtime = create(types.get(SHOWTIME), Map.of(
                "movie_id", movie.getEntityId(),
                "room_id", room.getEntityId(),
                "starts_at", startsAt,
                "ends_at", startsAt.plusMinutes(durationMinutes),
                "price", price,
                "total_capacity", capacity,
                "available_capacity", capacity
        ));
        relation(relationshipTypeRepository.findByName("movie_has_showtime").orElseThrow(), movie, showtime);
        relation(relationshipTypeRepository.findByName("showtime_in_room").orElseThrow(), showtime, room);
    }

    private Instance create(EntityType type, Map<String, Object> attributes) {
        Instance instance = instanceRepository.save(Instance.builder().entityTypeId(type.getEntityTypeId()).build());
        attributes.forEach((name, value) -> setValue(instance, name, value));
        return instance;
    }

    private void setValue(Instance instance, String name, Object raw) {
        AttributeDefinition def = attributeDefinitionRepository
                .findByNameAndEntityTypeId(name, instance.getEntityTypeId())
                .orElseThrow();
        AttributeValue attr = AttributeValue.builder()
                .entityId(instance.getEntityId())
                .attributeDefinitionId(def.getAttributeDefinitionId())
                .build();
        switch (DataType.valueOf(def.getDataType())) {
            case STRING, UUID -> attr.setStringValue(String.valueOf(raw));
            case INTEGER, LONG, DOUBLE -> attr.setNumberValue(Double.valueOf(String.valueOf(raw)));
            case BOOLEAN -> attr.setBooleanValue(Boolean.valueOf(String.valueOf(raw)));
            case DATE, DATETIME -> attr.setDateValue((LocalDateTime) raw);
        }
        attributeValueRepository.save(attr);
    }

    private void relation(RelationshipType type, Instance from, Instance to) {
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

    private Map<String, Object> movie(String title, String genre, String rating, int duration, String synopsis, String posterUrl) {
        return Map.of(
                "title", title,
                "genre", genre,
                "rating", rating,
                "duration_minutes", duration,
                "synopsis", synopsis,
                "poster_url", posterUrl
        );
    }

    private int getInt(Instance instance, String attribute) {
        AttributeDefinition def = attributeDefinitionRepository
                .findByNameAndEntityTypeId(attribute, instance.getEntityTypeId())
                .orElseThrow();
        return attributeValueRepository.findByEntityIdAndAttributeDefinitionId(instance.getEntityId(), def.getAttributeDefinitionId())
                .map(AttributeValue::getNumberValue)
                .map(Double::intValue)
                .orElse(0);
    }

    private String toDisplayName(String name) {
        String[] parts = name.split("_");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return builder.toString();
    }
}

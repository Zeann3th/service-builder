package vn.com.vds.vdt.servicebuilder.controller.dto.cinema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public final class CinemaDtos {
    private CinemaDtos() {
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MovieResponse {
        private Long id;
        private String title;
        private String genre;
        private String rating;
        private Integer durationMinutes;
        private String synopsis;
        private String posterUrl;
        private List<ShowtimeResponse> showtimes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShowtimeResponse {
        private Long id;
        private Long movieId;
        private Long roomId;
        private String roomName;
        private LocalDateTime startsAt;
        private LocalDateTime endsAt;
        private Double price;
        private Integer totalCapacity;
        private Integer availableCapacity;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReservationRequest {
        private Long showtimeId;
        private Integer quantity;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReservationResponse {
        private Long reservationId;
        private Long showtimeId;
        private Integer quantity;
        private List<String> allocatedSeats;
        private Integer remainingCapacity;
        private String status;
    }
}

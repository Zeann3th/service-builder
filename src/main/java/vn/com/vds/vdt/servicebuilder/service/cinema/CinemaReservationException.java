package vn.com.vds.vdt.servicebuilder.service.cinema;

import lombok.Getter;

@Getter
public class CinemaReservationException extends RuntimeException {
    private final String code;

    public CinemaReservationException(String code, String message) {
        super(message);
        this.code = code;
    }
}

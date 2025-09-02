package vn.com.vds.vdt.servicebuilder.common.base;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.vds.vdt.servicebuilder.common.constants.ErrorCodes;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseBuilder<T> {
    private String code;
    private String message;
    private T data;

    public static <T> ResponseBuilder<T> success(T data) {
        return new ResponseBuilder<>(ErrorCodes.ER0000, "Success", data);
    }

    public static <T> ResponseBuilder<T> error(String code, String message) {
        return new ResponseBuilder<>(code, message, null);
    }
}

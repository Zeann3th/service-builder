package vn.com.vds.vdt.servicebuilder.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.com.vds.vdt.servicebuilder.common.base.ResponseBuilder;
import vn.com.vds.vdt.servicebuilder.common.base.ResponseUtils;
import vn.com.vds.vdt.servicebuilder.common.constants.ErrorCodes;


@RestControllerAdvice
@SuppressWarnings("all")
public class GlobalExceptionHandler {

    @ExceptionHandler(CommandException.class)
    public ResponseBuilder<Void> handleCommandException(CommandException ex) {
        if (ResponseUtils.isResponseSuccess(ex.getCode())) {
            return ResponseBuilder.success(null);
        }
        return ResponseBuilder.error(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseBuilder<Void> handleGeneralException(Exception ex) {
        return ResponseBuilder.error(ErrorCodes.QS00003, ex.getMessage()); // System busy later, allow debug
    }
}


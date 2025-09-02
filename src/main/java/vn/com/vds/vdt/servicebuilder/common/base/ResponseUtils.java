package vn.com.vds.vdt.servicebuilder.common.base;

import lombok.NoArgsConstructor;
import vn.com.vds.vdt.servicebuilder.common.constants.ErrorCodes;

@NoArgsConstructor
public class ResponseUtils {
    public static Boolean isResponseSuccess(String code) {
        return ErrorCodes.ER0000.equals(code);
    }
}

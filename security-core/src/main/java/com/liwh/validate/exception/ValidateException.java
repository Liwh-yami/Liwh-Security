package com.liwh.validate.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * @author: Liwh
 * @ClassName: ValidataException
 * @Description:
 * @version: 1.0.0
 * @date: 2018-12-12 2:20 PM
 */
public class ValidateException extends AuthenticationException {
    public ValidateException(String msg) {
        super(msg);
    }
}

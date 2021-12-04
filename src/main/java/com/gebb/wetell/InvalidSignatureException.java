package com.gebb.wetell;

import java.security.GeneralSecurityException;

/**
 * This exception is thrown if a digital signature is invalid or the data was changed.
 */
public class InvalidSignatureException extends GeneralSecurityException {
    public InvalidSignatureException() {
        super();
    }

    public InvalidSignatureException(String msg) {
        super(msg);
    }

    public InvalidSignatureException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public InvalidSignatureException(Throwable cause) {
        super(cause);
    }
}

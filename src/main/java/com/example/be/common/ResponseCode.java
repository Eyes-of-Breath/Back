package com.example.be.common;

public interface ResponseCode {
    String SUCCESS = "SU";

    String VALIDATION_FAIL = "VF";
    String DUPLICATED_EMAIL = "DE";

    String SIGN_IN_FAIL = "SF";
    String CERTIFICATION_FAIL = "CF";

    String MAIL_FAIL = "MF";
    String DATABASE_ERROR = "DBE";

    String AUTHORIZATION_FAIL = "AF";
    String PASSWORD_MISMATCH = "PM";
}

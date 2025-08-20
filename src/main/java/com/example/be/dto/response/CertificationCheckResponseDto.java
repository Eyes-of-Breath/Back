package com.example.be.dto.response;

import com.example.be.common.ResponseCode;
import com.example.be.common.ResponseMessage;
import com.example.be.dto.ResponseDto;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public class CertificationCheckResponseDto extends ResponseDto {

    private CertificationCheckResponseDto() {
        super();
    }

    public static ResponseEntity<CertificationCheckResponseDto> success() {
        CertificationCheckResponseDto responseBody = new CertificationCheckResponseDto();
        return ResponseEntity.status(HttpStatus.OK).body(responseBody);
    }

    public static ResponseEntity<ResponseDto> certificationFail() {
        ResponseDto responseBody = new ResponseDto(ResponseCode.CERTIFICATION_FAIL, ResponseMessage.CERTIFICATION_FAIL);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseBody);
    }
}
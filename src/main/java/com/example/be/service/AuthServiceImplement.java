package com.example.be.service;

import com.example.be.common.CertificationNumber; // CertificationNumber 임포트 추가
import com.example.be.dto.ResponseDto;
import com.example.be.dto.request.*;
import com.example.be.dto.response.*;
import com.example.be.entity.Certification;
import com.example.be.entity.Member;
import com.example.be.jwt.EmailProvider;
import com.example.be.jwt.JwtProvider;
import com.example.be.repository.CertificationRepository;
import com.example.be.repository.MemberRepository;
import lombok.RequiredArgsConstructor; // 생성자 자동 생성을 위해 추가
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 만들어줍니다.
public class AuthServiceImplement implements AuthService {

    private final MemberRepository memberRepository;
    private final EmailProvider emailProvider;
    private final CertificationRepository certificationRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // @RequiredArgsConstructor 어노테이션이 아래 생성자 코드를 대체합니다.
    /*
    public AuthServiceImplement(MemberRepository memberRepository, EmailProvider emailProvider, CertificationRepository certificationRepository, JwtProvider jwtProvider) {
        this.memberRepository = memberRepository;
        this.emailProvider = emailProvider;
        this.certificationRepository = certificationRepository;
        this.jwtProvider = jwtProvider;
    }
    */

    @Override
    public ResponseEntity<? super EmailCheckResponseDto> emailCheck(EmailCheckRequestDto dto) {
        try {
            String email = dto.getEmail();
            if (memberRepository.existsByEmail(email)) {
                return EmailCheckResponseDto.duplicateEmail();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            return ResponseDto.databaseError();
        }
        return EmailCheckResponseDto.success();
    }


    @Override
    public ResponseEntity<? super EmailCertificationResponseDto> emailCertification(EmailCertificationRequestDto dto) {
        try {
            String email = dto.getEmail();
            if (memberRepository.existsByEmail(email)) {
                return EmailCertificationResponseDto.duplicateEmail();
            }

            String certificationNumber = CertificationNumber.getCertificationNumber();

            boolean isSucceed = emailProvider.sendCertificationEmail(email, certificationNumber);
            if (!isSucceed) {
                return EmailCertificationResponseDto.mailSendFail();
            }

            Certification certification = new Certification(email, certificationNumber);
            certificationRepository.save(certification);

        } catch (Exception exception) {
            exception.printStackTrace();
            return ResponseDto.databaseError();
        }
        return EmailCertificationResponseDto.success();
    }

    @Override
    public ResponseEntity<? super CertificationCheckResponseDto> certificationCheck(CertificationCheckRequestDto dto) {
        try {
            String email = dto.getEmail();
            String certificationNumber = dto.getCertificationNumber();

            Certification certification = certificationRepository.findByEmail(email).orElse(null);

            // 1. null 체크를 먼저 수행
            if (certification == null) {
                return CertificationCheckResponseDto.certificationFail();
            }

            // 2. 인증번호 일치 여부 확인
            boolean isMatched = certification.getEmail().equals(email) &&
                    certification.getCertificationNumber().equals(certificationNumber);

            if (!isMatched) {
                return CertificationCheckResponseDto.certificationFail();
            }

        } catch (Exception exception) {
            exception.printStackTrace();
            return ResponseDto.databaseError();
        }
        return CertificationCheckResponseDto.success();
    }

    // 중복된 signUp 메소드를 하나로 통합하고 정리
    @Override
    public ResponseEntity<? super SignUpResponseDto> signUp(SignUpRequestDto dto) {
        try {
            String email = dto.getEmail();
            if (memberRepository.existsByEmail(email)) {
                return SignUpResponseDto.duplicateEmail();
            }

            String certificationNumber = dto.getCertificationNumber();
            Certification certification = certificationRepository.findByEmail(email).orElse(null);

            if (certification == null || !certification.getCertificationNumber().equals(certificationNumber)) {
                return SignUpResponseDto.certificationFail();
            }

            String encodedPassword = passwordEncoder.encode(dto.getPassword());
            dto.setPassword(encodedPassword);

            Member member = new Member(dto);
            memberRepository.save(member);

            certificationRepository.deleteByEmail(email);

        } catch (Exception exception) {
            exception.printStackTrace();
            return ResponseDto.databaseError();
        }
        return SignUpResponseDto.success();
    }

    @Override
    public ResponseEntity<? super SignInResponseDto> signIn(SignInRequestDto dto) {
        String accessToken;
        Integer memberId;
        String nickname;
        try {
            String email = dto.getEmail();
            Member member = memberRepository.findByEmail(email).orElse(null);

            if (member == null) {
                return SignInResponseDto.signInFail();
            }

            String password = dto.getPassword();
            String encodedPassword = member.getPassword();

            boolean isMatched = passwordEncoder.matches(password, encodedPassword);
            if (!isMatched) {
                return SignInResponseDto.signInFail();
            }

            memberId = member.getId();
            nickname = member.getNickname();
            accessToken = jwtProvider.create(email);

        } catch (Exception exception) {
            exception.printStackTrace();
            return ResponseDto.databaseError();
        }
        return SignInResponseDto.success(accessToken, memberId, nickname);
    }
}
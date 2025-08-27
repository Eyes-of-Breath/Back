package com.example.be.service;

import com.example.be.dto.request.PasswordChangeRequestDto;
import com.example.be.dto.response.PasswordChangeResponseDto;
import com.example.be.dto.response.ResponseDto;
import com.example.be.entity.Member;
import com.example.be.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 읽기 전용 트랜잭션으로 설정
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 비밀번호 변경 서비스 메서드
     * @param dto 클라이언트로부터 받은 현재 비밀번호와 새 비밀번호 DTO
     * @param email JWT 토큰에서 추출한 현재 로그인된 사용자의 이메일
     * @return 성공 또는 실패 응답
     */
    @Transactional // 쓰기 작업이 필요하므로 @Transactional 어노테이션을 추가
    public ResponseEntity<?> changePassword(PasswordChangeRequestDto dto, String email) { // <--- 수정된 부분
        try {
            // 1. 이메일을 기반으로 회원 정보 조회
            Member member = memberRepository.findByEmail(email).orElse(null);
            if (member == null) {
                // 로그인한 사용자의 정보가 없을 경우 (이론상 발생하기 힘듦)
                return ResponseDto.authorizationFail();
            }

            // 2. 입력된 '현재 비밀번호'가 DB에 저장된 비밀번호와 일치하는지 확인
            if (!passwordEncoder.matches(dto.getCurrentPassword(), member.getPassword())) {
                // 비밀번호가 일치하지 않으면 '비밀번호 불일치' 응답 반환
                return ResponseDto.passwordMismatch();
            }

            // 3. '새 비밀번호'를 BCrypt 알고리즘으로 암호화
            String encodedNewPassword = passwordEncoder.encode(dto.getNewPassword());

            // 4. 회원의 비밀번호를 암호화된 새 비밀번호로 업데이트
            member.updatePassword(encodedNewPassword);

            // @Transactional 어노테이션에 의해 메서드가 종료될 때 변경된 member 객체가 자동으로 DB에 반영(저장)됩니다.

        } catch (Exception exception) {
            exception.printStackTrace();
            // 데이터베이스 오류 발생 시 에러 응답 반환
            return ResponseDto.databaseError();
        }

        // 5. 모든 과정이 성공적으로 끝나면 성공 응답 반환
        return PasswordChangeResponseDto.success();
    }
}
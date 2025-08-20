package com.example.be.entity;
import com.example.be.dto.request.SignUpRequestDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="members") // 데이터베이스 테이블 이름을 'members'로 지정
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Integer id;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "_password", nullable = false)
    private String password;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "email_verified")
    private boolean emailVerified;

    @CreationTimestamp // 엔티티가 처음 저장될 때 자동으로 현재 시간이 입력됩니다.
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp // 엔티티가 업데이트될 때마다 자동으로 현재 시간이 입력됩니다.
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 이메일/비밀번호 회원가입을 위한 생성자
    public Member(SignUpRequestDto dto) {
        this.email = dto.getEmail();
        this.password = dto.getPassword();
        this.nickname = dto.getNickname();
        this.emailVerified = true; // 이메일 인증을 통과했으므로 true
    }
}
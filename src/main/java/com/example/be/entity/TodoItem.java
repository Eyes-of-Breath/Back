package com.example.be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "todo_item")
public class TodoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "todo_id")
    private Integer todoId;

    // 어떤 사용자의 할 일인지 연결합니다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "content", nullable = false)
    private String content; // 할 일 내용

    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate; // 이 할 일이 속한 날짜

    @Column(name = "is_completed", nullable = false)
    @ColumnDefault("false") // 데이터베이스에 기본값으로 false를 지정
    private boolean isCompleted; // 완료 여부

    // 완료 상태를 변경하는 편의 메소드
    public void complete() {
        this.isCompleted = true;
    }

    public void incomplete() {
        this.isCompleted = false;
    }
}
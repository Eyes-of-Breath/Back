package com.example.be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "schedule_event")
public class ScheduleEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Integer eventId;

    // 어떤 사용자의 일정인지 연결합니다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "title", nullable = false)
    private String title; // 일정 제목 (예: "환자 CT 판독")

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime; // 일정 시작 시간

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime; // 일정 종료 시간 (시작 시간 + 소요 시간으로 계산)

    @Column(name = "memo", columnDefinition = "TEXT")
    private String memo; // 메모

    // 수정을 위한 편의 메소드
    public void update(String title, LocalDateTime startTime, LocalDateTime endTime, String memo) {
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.memo = memo;
    }
}
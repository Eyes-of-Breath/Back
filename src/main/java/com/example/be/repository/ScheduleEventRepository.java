package com.example.be.repository;

import com.example.be.entity.Member;
import com.example.be.entity.ScheduleEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleEventRepository extends JpaRepository<ScheduleEvent, Integer> {

    // 특정 사용자의 특정 기간 동안의 모든 일정을 조회
    List<ScheduleEvent> findAllByMemberAndStartTimeBetween(Member member, LocalDateTime start, LocalDateTime end);

    // 특정 사용자의 특정 일정을 조회 (소유권 확인용)
    Optional<ScheduleEvent> findByEventIdAndMember(Integer eventId, Member member);
}
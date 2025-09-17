package com.example.be.repository;

import com.example.be.entity.Member;
import com.example.be.entity.TodoItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TodoItemRepository extends JpaRepository<TodoItem, Integer> {

    // 특정 사용자의 특정 날짜의 모든 할 일을 조회
    List<TodoItem> findAllByMemberAndTargetDate(Member member, LocalDate date);

    // 특정 사용자의 특정 할 일을 조회 (소유권 확인용)
    Optional<TodoItem> findByTodoIdAndMember(Integer todoId, Member member);

    // 로그인 사용자 소유의 할 일만 조회 (권한 체크용)
    Optional<TodoItem> findByTodoIdAndMember_Id(Integer todoId, Integer memberId);
}
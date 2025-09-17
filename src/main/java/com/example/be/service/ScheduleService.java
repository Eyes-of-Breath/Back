package com.example.be.service;

import com.example.be.dto.request.CreateScheduleEventRequestDto;
import com.example.be.dto.request.CreateTodoItemRequestDto;
import com.example.be.dto.response.ScheduleEventDto;
import com.example.be.dto.response.TodoItemDto;
import com.example.be.entity.Member;
import com.example.be.entity.ScheduleEvent;
import com.example.be.entity.TodoItem;
import com.example.be.repository.MemberRepository;
import com.example.be.repository.ScheduleEventRepository;
import com.example.be.repository.TodoItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleService {

    private final MemberRepository memberRepository;
    private final ScheduleEventRepository scheduleEventRepository;
    private final TodoItemRepository todoItemRepository;

    // === 달력 일정 (ScheduleEvent) 관련 서비스 ===

    @Transactional(readOnly = true)
    public List<ScheduleEventDto> getScheduleEvents(LocalDate startDate, LocalDate endDate) {
        Member member = getCurrentMember();

        if (startDate == null || endDate == null) {
            LocalDate today = LocalDate.now();
            startDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            endDate = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        List<ScheduleEvent> events = scheduleEventRepository.findAllByMemberAndStartTimeBetween(member, startDateTime, endDateTime);

        return events.stream()
                .map(ScheduleEventDto::fromEntity)
                .collect(Collectors.toList());
    }

    public ScheduleEventDto createScheduleEvent(CreateScheduleEventRequestDto dto) {
        Member member = getCurrentMember();
        ScheduleEvent newEvent = ScheduleEvent.builder()
                .member(member)
                .title(dto.getTitle())
                .startTime(dto.getStartTime())
                .endTime(dto.getEndTime())
                .memo(dto.getMemo())
                .build();
        ScheduleEvent savedEvent = scheduleEventRepository.save(newEvent);
        return ScheduleEventDto.fromEntity(savedEvent);
    }

    public ScheduleEventDto updateScheduleEvent(Integer eventId, CreateScheduleEventRequestDto dto) {
        Member member = getCurrentMember();
        ScheduleEvent event = scheduleEventRepository.findByEventIdAndMember(eventId, member)
                .orElseThrow(() -> new SecurityException("수정 권한이 없거나 존재하지 않는 일정입니다."));

        event.update(dto.getTitle(), dto.getStartTime(), dto.getEndTime(), dto.getMemo());
        return ScheduleEventDto.fromEntity(event);
    }

    public void deleteScheduleEvent(Integer eventId) {
        Member member = getCurrentMember();
        ScheduleEvent event = scheduleEventRepository.findByEventIdAndMember(eventId, member)
                .orElseThrow(() -> new SecurityException("삭제 권한이 없거나 존재하지 않는 일정입니다."));
        scheduleEventRepository.delete(event);
    }

    // === 할 일 목록 (TodoItem) 관련 서비스 ===

    @Transactional(readOnly = true)
    public List<TodoItemDto> getTodoItems(LocalDate date) {
        Member member = getCurrentMember();
        List<TodoItem> items = todoItemRepository.findAllByMemberAndTargetDate(member, date);
        return items.stream()
                .map(TodoItemDto::fromEntity)
                .collect(Collectors.toList());
    }

    public TodoItemDto createTodoItem(CreateTodoItemRequestDto dto) {
        Member member = getCurrentMember();
        TodoItem newItem = TodoItem.builder()
                .member(member)
                .content(dto.getContent())
                .targetDate(dto.getTargetDate())
                .isCompleted(false)
                .build();
        TodoItem savedItem = todoItemRepository.save(newItem);
        return TodoItemDto.fromEntity(savedItem);
    }

    public TodoItemDto toggleTodoItem(Integer todoId) {
        Member member = getCurrentMember();
        TodoItem item = todoItemRepository.findByTodoIdAndMember(todoId, member)
                .orElseThrow(() -> new SecurityException("권한이 없거나 존재하지 않는 할 일입니다."));

        if (item.isCompleted()) {
            item.incomplete();
        } else {
            item.complete();
        }
        return TodoItemDto.fromEntity(item);
    }

    //할 일 삭제
    public void deleteTodo(Integer todoId) {
        Integer memberId = getCurrentMember().getId();
        var item = todoItemRepository.findByTodoIdAndMember_Id(todoId, memberId)
                .orElseThrow(() -> new SecurityException("삭제 권한이 없거나 존재하지 않는 할 일입니다."));
        todoItemRepository.delete(item);
    }

    // 현재 로그인한 사용자 정보를 가져오는 헬퍼 메소드
    private Member getCurrentMember() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("로그인한 사용자 정보를 찾을 수 없습니다."));
    }
}
package com.example.be.controller;

import com.example.be.dto.request.CreateScheduleEventRequestDto;
import com.example.be.dto.request.CreateTodoItemRequestDto;
import com.example.be.dto.response.ScheduleEventDto;
import com.example.be.dto.response.TodoItemDto;
import com.example.be.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    // === 달력 일정 (ScheduleEvent) API ===

    @GetMapping("/events")
    public ResponseEntity<List<ScheduleEventDto>> getScheduleEvents(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<ScheduleEventDto> events = scheduleService.getScheduleEvents(startDate, endDate);
        return ResponseEntity.ok(events);
    }

    @PostMapping("/events")
    public ResponseEntity<ScheduleEventDto> createScheduleEvent(@Valid @RequestBody CreateScheduleEventRequestDto requestBody) {
        ScheduleEventDto createdEvent = scheduleService.createScheduleEvent(requestBody);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    @PutMapping("/events/{eventId}")
    public ResponseEntity<ScheduleEventDto> updateScheduleEvent(
            @PathVariable Integer eventId,
            @Valid @RequestBody CreateScheduleEventRequestDto requestBody) {
        ScheduleEventDto updatedEvent = scheduleService.updateScheduleEvent(eventId, requestBody);
        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<Map<String, String>> deleteScheduleEvent(@PathVariable Integer eventId) {
        scheduleService.deleteScheduleEvent(eventId);
        return ResponseEntity.ok(Map.of("message", "일정이 성공적으로 삭제되었습니다."));
    }

    // === 할 일 목록 (TodoItem) API ===

    @GetMapping("/todos")
    public ResponseEntity<List<TodoItemDto>> getTodoItems(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<TodoItemDto> items = scheduleService.getTodoItems(date);
        return ResponseEntity.ok(items);
    }

    @PostMapping("/todos")
    public ResponseEntity<TodoItemDto> createTodoItem(@Valid @RequestBody CreateTodoItemRequestDto requestBody) {
        TodoItemDto createdItem = scheduleService.createTodoItem(requestBody);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
    }

    @PatchMapping("/todos/{todoId}/toggle")
    public ResponseEntity<TodoItemDto> toggleTodoItemCompletion(@PathVariable Integer todoId) {
        TodoItemDto updatedItem = scheduleService.toggleTodoItem(todoId);
        return ResponseEntity.ok(updatedItem);
    }
    @DeleteMapping("/todos/{todoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTodo(@PathVariable Integer todoId) {
        scheduleService.deleteTodo(todoId);
    }
}
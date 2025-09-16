package com.example.be.dto.response;

import com.example.be.entity.ScheduleEvent;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class ScheduleEventDto {
    private Integer eventId;
    private Integer memberId;
    private String title;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String memo;

    public static ScheduleEventDto fromEntity(ScheduleEvent entity) {
        return ScheduleEventDto.builder()
                .eventId(entity.getEventId())
                .memberId(entity.getMember().getId())
                .title(entity.getTitle())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .memo(entity.getMemo())
                .build();
    }
}
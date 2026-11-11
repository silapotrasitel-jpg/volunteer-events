package ru.student.volunteer_events.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.student.volunteer_events.dto.EventRequest;
import ru.student.volunteer_events.dto.EventResponse;
import ru.student.volunteer_events.enums.EventStatus;
import ru.student.volunteer_events.service.EventService;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<Page<EventResponse>> getEvents(
            @RequestParam(required = false) EventStatus status,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Long skillId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @PageableDefault(size = 10, sort = "startTime") Pageable pageable
    ) {
        return ResponseEntity.ok(eventService.searchEvents(status, location, skillId, from, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('COORDINATOR')")
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody EventRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.createEvent(request, authentication.getName()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('COORDINATOR')")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long id,
            @Valid @RequestBody EventRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(eventService.updateEvent(id, request, authentication.getName()));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('COORDINATOR')")
    public ResponseEntity<EventResponse> changeStatus(
            @PathVariable Long id,
            @RequestParam EventStatus status,
            Authentication authentication
    ) {
        return ResponseEntity.ok(eventService.changeStatus(id, status, authentication.getName()));
    }
}

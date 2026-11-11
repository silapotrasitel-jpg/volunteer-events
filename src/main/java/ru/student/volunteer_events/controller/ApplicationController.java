package ru.student.volunteer_events.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.student.volunteer_events.dto.ApplicationResponse;
import ru.student.volunteer_events.dto.ApplicationReviewRequest;
import ru.student.volunteer_events.service.ApplicationService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping("/events/{eventId}/apply")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ResponseEntity<ApplicationResponse> apply(
            @PathVariable Long eventId,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(applicationService.apply(eventId, authentication.getName()));
    }

    @GetMapping("/applications/my")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ResponseEntity<List<ApplicationResponse>> getMyApplications(Authentication authentication) {
        return ResponseEntity.ok(applicationService.getMyApplications(authentication.getName()));
    }

    @PatchMapping("/applications/{id}/cancel")
    @PreAuthorize("hasRole('VOLUNTEER')")
    public ResponseEntity<ApplicationResponse> cancelApplication(
            @PathVariable Long id,
            Authentication authentication
    ) {
        return ResponseEntity.ok(applicationService.cancelByVolunteer(id, authentication.getName()));
    }

    @GetMapping("/events/{eventId}/applications")
    @PreAuthorize("hasRole('COORDINATOR')")
    public ResponseEntity<List<ApplicationResponse>> getEventApplications(
            @PathVariable Long eventId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(applicationService.getEventApplications(eventId, authentication.getName()));
    }

    @PatchMapping("/applications/{id}/approve")
    @PreAuthorize("hasRole('COORDINATOR')")
    public ResponseEntity<ApplicationResponse> approve(
            @PathVariable Long id,
            @RequestBody(required = false) ApplicationReviewRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(applicationService.approve(id, request, authentication.getName()));
    }

    @PatchMapping("/applications/{id}/reject")
    @PreAuthorize("hasRole('COORDINATOR')")
    public ResponseEntity<ApplicationResponse> reject(
            @PathVariable Long id,
            @RequestBody(required = false) ApplicationReviewRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(applicationService.reject(id, request, authentication.getName()));
    }

    @PatchMapping("/applications/{id}/attend")
    @PreAuthorize("hasRole('COORDINATOR')")
    public ResponseEntity<ApplicationResponse> markAttended(
            @PathVariable Long id,
            @RequestBody(required = false) ApplicationReviewRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(applicationService.markAttended(id, request, authentication.getName()));
    }

    @GetMapping("/events/{eventId}/export-csv")
    @PreAuthorize("hasRole('COORDINATOR')")
    public ResponseEntity<byte[]> exportCsv(
            @PathVariable Long eventId,
            Authentication authentication
    ) {
        byte[] csv = applicationService.exportParticipantsCsv(eventId, authentication.getName());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=participants_event_" + eventId + ".csv")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }
}

package com.eventra.repository;

import com.eventra.domain.event.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    Page<Event> findByCreatedBy(UUID userId, Pageable pageable);
    Optional<Event> findByIdAndCreatedBy(UUID id, UUID userId);

    List<Event> findByStatusAndEventDateBetween(String status, LocalDateTime start, LocalDateTime end);
    List<Event> findByEventDateBetween(LocalDateTime start, LocalDateTime end);
}
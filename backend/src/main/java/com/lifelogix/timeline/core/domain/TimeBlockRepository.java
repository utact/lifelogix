package com.lifelogix.timeline.core.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TimeBlockRepository extends JpaRepository<TimeBlock, Long> {
    List<TimeBlock> findByActivity_User_IdAndDate(Long userId, LocalDate testDate);
}
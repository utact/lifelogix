package com.lifelogix.timeline.core.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TimeBlockRepository extends JpaRepository<TimeBlock, Long> {

    @Query("SELECT tb FROM TimeBlock tb JOIN FETCH tb.activity a JOIN FETCH a.category c WHERE a.user.id = :userId AND tb.date = :date")
    List<TimeBlock> findByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
}
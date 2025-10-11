package com.lifelogix.timeline.core.domain;

import com.lifelogix.timeline.activity.domain.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional; // 👈 Optional import 추가

public interface TimeBlockRepository extends JpaRepository<TimeBlock, Long> {

    @Query("SELECT tb FROM TimeBlock tb JOIN FETCH tb.activity a JOIN FETCH a.category c WHERE a.user.id = :userId AND tb.date = :date")
    List<TimeBlock> findByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    boolean existsByActivity(Activity activity);

    @Query("SELECT tb FROM TimeBlock tb JOIN tb.activity a WHERE a.user.id = :userId AND tb.date = :date AND tb.startTime = :startTime AND tb.type = :type")
    Optional<TimeBlock> findUserTimeBlockForSlot(
            @Param("userId") Long userId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("type") TimeBlockType type);
}
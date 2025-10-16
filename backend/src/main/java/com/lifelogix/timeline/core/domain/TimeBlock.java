package com.lifelogix.timeline.core.domain;

import com.lifelogix.timeline.activity.domain.Activity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class TimeBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime startTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TimeBlockType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    public TimeBlock(LocalDate date, LocalTime startTime, TimeBlockType type, Activity activity) {
        this.date = date;
        this.startTime = startTime;
        this.type = type;
        this.activity = activity;
    }

    @Deprecated
    public TimeBlock(Long id, LocalDate date, LocalTime startTime, TimeBlockType type, Activity activity) {
        this.id = id;
        this.date = date;
        this.startTime = startTime;
        this.type = type;
        this.activity = activity;
    }

    /**
     * 타임블록에 기록된 활동을 변경
     */
    public void updateActivity(Activity activity) {
        this.activity = activity;
    }
}
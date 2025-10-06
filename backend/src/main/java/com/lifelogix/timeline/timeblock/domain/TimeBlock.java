package com.lifelogix.timeline.timeblock.domain;

import com.lifelogix.timeline.activity.domain.Activity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    // 테스트 및 서비스 로직 편의를 위한 생성자
    public TimeBlock(LocalDate date, LocalTime startTime, TimeBlockType type, Activity activity) {
        this.date = date;
        this.startTime = startTime;
        this.type = type;
        this.activity = activity;
    }
}

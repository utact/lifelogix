package com.lifelogix.timeline.activity.domain;

import com.lifelogix.timeline.category.domain.Category;
import com.lifelogix.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // 테스트 및 서비스 로직 편의를 위한 생성자
    public Activity(String name, User user, Category category) {
        this.name = name;
        this.user = user;
        this.category = category;
    }
}

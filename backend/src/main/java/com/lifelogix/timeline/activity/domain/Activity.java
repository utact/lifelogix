package com.lifelogix.timeline.activity.domain;

import com.lifelogix.timeline.category.domain.Category;
import com.lifelogix.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
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

    public Activity(String name, User user, Category category) {
        this.name = name;
        this.user = user;
        this.category = category;
    }

    @Deprecated
    public Activity(Long id, String name, User user, Category category) {
        this.id = id;
        this.name = name;
        this.user = user;
        this.category = category;
    }

    public void update(String name) {
        this.name = name;
    }
}
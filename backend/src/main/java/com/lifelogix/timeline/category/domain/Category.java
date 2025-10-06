package com.lifelogix.timeline.category.domain;

import com.lifelogix.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 7)
    private String color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // 시스템 카테고리의 경우 null
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id") // 최상위 카테고리의 경우 null
    private Category parent;

    @OneToMany(mappedBy = "parent")
    private List<Category> children = new ArrayList<>();

    // 테스트 및 서비스 로직 편의를 위한 생성자
    public Category(String name, String color, User user, Category parent) {
        this.name = name;
        this.color = color;
        this.user = user;
        this.parent = parent;
    }
}

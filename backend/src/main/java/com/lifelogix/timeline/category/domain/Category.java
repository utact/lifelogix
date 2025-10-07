package com.lifelogix.timeline.category.domain;

import com.lifelogix.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 7)
    private String color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent")
    private List<Category> children = new ArrayList<>();

    public Category(String name, String color, User user, Category parent) {
        this.name = name;
        this.color = color;
        this.user = user;
        this.parent = parent;
    }

    /**
     * @deprecated 테스트 코드에서만 사용되는 생성자
     **/
    @Deprecated
    public Category(Long id, String name, String color, User user, Category parent) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.user = user;
        this.parent = parent;
    }
}

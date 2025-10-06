package com.lifelogix.timeline.activity.domain;

import com.lifelogix.timeline.category.domain.Category;
import com.lifelogix.timeline.category.domain.CategoryRepository;
import com.lifelogix.user.domain.User;
import com.lifelogix.user.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ActivityRepositoryTest {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User savedUser;
    private Category savedCategory;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .email("tester@test.com")
                .password("password123")
                .username("tester")
                .build();
        savedUser = userRepository.save(user);

        Category category = new Category("업무", "#3498DB", savedUser, null);
        savedCategory = categoryRepository.save(category);
    }

    @Test
    @DisplayName("활동 저장 및 조회")
    void 활동을_저장하고_조회하면_사용자와_카테고리_정보가_올바르게_연결된다() {
        // given
        Activity activity = new Activity("회의", savedUser, savedCategory);

        // when
        Activity savedActivity = activityRepository.save(activity);
        Activity foundActivity = activityRepository.findById(savedActivity.getId()).orElse(null);

        // then
        assertThat(foundActivity).isNotNull();
        assertThat(foundActivity.getName()).isEqualTo("회의");
        assertThat(foundActivity.getUser().getId()).isEqualTo(savedUser.getId());
        assertThat(foundActivity.getCategory().getId()).isEqualTo(savedCategory.getId());
    }
}
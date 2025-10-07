package com.lifelogix.timeline.category.domain;

import com.lifelogix.user.domain.User;
import com.lifelogix.user.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private User savedUser;
    private Category parentCategory;

    @BeforeEach
    void setUp() {
        User user = User.builder().email("tester@test.com").password("p").username("tester").build();
        savedUser = userRepository.save(user);

        parentCategory = new Category("학습", "#9B59B6", null, null);
        categoryRepository.save(parentCategory);
    }

    @Test
    @DisplayName("사용자 정의 카테고리 저장 및 조회")
    void 사용자정의_카테고리를_저장하고_조회하면_부모와_사용자_정보가_올바르게_연결된다() {
        // given
        Category customCategory = new Category("AWS 자격증 공부", "#F39C12", savedUser, parentCategory);

        // when
        Category savedCategory = categoryRepository.save(customCategory);
        Category foundCategory = categoryRepository.findById(savedCategory.getId()).orElseThrow();

        // then
        assertThat(foundCategory.getName()).isEqualTo("AWS 자격증 공부");
        assertThat(foundCategory.getUser().getId()).isEqualTo(savedUser.getId());
        assertThat(foundCategory.getParent().getId()).isEqualTo(parentCategory.getId());
    }
}
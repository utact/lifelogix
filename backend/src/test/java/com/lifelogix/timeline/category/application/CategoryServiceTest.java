package com.lifelogix.timeline.category.application;

import com.lifelogix.exception.BusinessException;
import com.lifelogix.exception.ErrorCode;
import com.lifelogix.timeline.category.api.dto.request.CreateCategoryRequest;
import com.lifelogix.timeline.category.api.dto.response.CategoryResponse;
import com.lifelogix.timeline.category.domain.Category;
import com.lifelogix.timeline.category.domain.CategoryRepository;
import com.lifelogix.user.domain.User;
import com.lifelogix.user.domain.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    @DisplayName("사용자 정의 카테고리 생성")
    void 사용자_정의_카테고리를_성공적으로_생성한다() {
        // given
        Long userId = 1L;
        Long parentId = 10L;
        var request = new CreateCategoryRequest("AWS 자격증 공부", "#F39C12", parentId);

        User fakeUser = User.builder().id(userId).build();
        // 1. Category 클래스의 Mock 객체 생성
        Category fakeParentCategory = mock(Category.class);
        // 2. getId() 메서드가 호출되면 parentId(10L)를 반환하도록 설정
        when(fakeParentCategory.getId()).thenReturn(parentId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(fakeUser));
        when(categoryRepository.findById(parentId)).thenReturn(Optional.of(fakeParentCategory));
        // save 메서드가 호출되면, 실제 저장된 것처럼 Category 객체를 반환하도록 설정
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        CategoryResponse response = categoryService.createCustomCategory(userId, request);

        // then
        assertThat(response.name()).isEqualTo("AWS 자격증 공부");
        assertThat(response.isCustom()).isTrue();
        assertThat(response.parentId()).isEqualTo(parentId);
    }

    @Test
    @DisplayName("카테고리 생성 실패 - 존재하지 않는 부모")
    void 존재하지_않는_부모_카테고리로는_생성할_수_없다() {
        // given
        Long userId = 1L;
        Long nonExistentParentId = 999L;
        var request = new CreateCategoryRequest("실패 테스트", "#123", nonExistentParentId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(User.builder().id(userId).build()));
        when(categoryRepository.findById(nonExistentParentId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> categoryService.createCustomCategory(userId, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);
    }

    @Test
    @DisplayName("카테고리 생성 실패 - 부모가 시스템 카테고리가 아님")
    void 부모가_시스템_카테고리가_아니면_생성할_수_없다() {
        // given
        Long userId = 1L;
        Long parentId = 10L;
        var request = new CreateCategoryRequest("실패 테스트", "#123", parentId);

        User fakeUser = User.builder().id(userId).build();
        // user가 할당된, 사용자 정의 카테고리
        Category fakeParentCategory = new Category(parentId, "남의 커스텀 카테고리", "#456", fakeUser, null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(fakeUser));
        when(categoryRepository.findById(parentId)).thenReturn(Optional.of(fakeParentCategory));

        // when & then
        assertThatThrownBy(() -> categoryService.createCustomCategory(userId, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_PARENT_CATEGORY);
    }

    @Test
    @DisplayName("전체 카테고리 조회")
    void 사용자가_조회_가능한_모든_카테고리를_가져온다() {
        // given
        Long userId = 1L;
        User fakeUser = User.builder().id(userId).build();

        Category systemCategory = new Category("운동", "#2ECC71", null, null);
        Category customCategory = new Category("개인 프로젝트", "#E74C3C", fakeUser, null);

        // Repository가 시스템 카테고리와 사용자 정의 카테고리를 모두 반환한다고 가정
        when(categoryRepository.findByUserIdOrUserIsNull(userId)).thenReturn(List.of(systemCategory, customCategory));

        // when
        List<CategoryResponse> responses = categoryService.findAllCategoriesForUser(userId);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).name()).isEqualTo("운동");
        assertThat(responses.get(0).isCustom()).isFalse();
        assertThat(responses.get(1).name()).isEqualTo("개인 프로젝트");
        assertThat(responses.get(1).isCustom()).isTrue();
    }
}
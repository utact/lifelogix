package com.lifelogix.timeline.category.application;

import com.lifelogix.exception.BusinessException;
import com.lifelogix.exception.ErrorCode;
import com.lifelogix.timeline.activity.domain.ActivityRepository;
import com.lifelogix.timeline.category.api.dto.request.CreateCategoryRequest;
import com.lifelogix.timeline.category.api.dto.request.UpdateCategoryRequest;
import com.lifelogix.timeline.category.api.dto.response.CategoryResponse;
import com.lifelogix.timeline.category.domain.Category;
import com.lifelogix.timeline.category.domain.CategoryRepository;
import com.lifelogix.user.domain.User;
import com.lifelogix.user.domain.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ActivityRepository activityRepository;
    @InjectMocks
    private CategoryService categoryService;

    @Nested
    @DisplayName("카테고리 생성")
    class CreateCategoryTest {

        @Test
        @DisplayName("성공")
        void 사용자_정의_카테고리를_성공적으로_생성한다() {
            // given
            Long userId = 1L;
            Long parentId = 10L;
            var request = new CreateCategoryRequest("AWS 자격증 공부", "#F39C12", parentId);
            User fakeUser = User.builder().id(userId).build();
            Category fakeParentCategory = new Category(parentId, "학습", "#9B59B6", null, null);

            when(userRepository.findById(userId)).thenReturn(Optional.of(fakeUser));
            when(categoryRepository.findById(parentId)).thenReturn(Optional.of(fakeParentCategory));
            when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
                Category newCategory = invocation.getArgument(0);
                return new Category(100L, newCategory.getName(), newCategory.getColor(), newCategory.getUser(), newCategory.getParent());
            });

            // when
            CategoryResponse response = categoryService.createCustomCategory(userId, request);

            // then
            assertThat(response.id()).isEqualTo(100L);
            assertThat(response.name()).isEqualTo("AWS 자격증 공부");
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 부모")
        void 존재하지_않는_부모_카테고리로는_생성할_수_없다() {
            // given
            Long userId = 1L;
            var request = new CreateCategoryRequest("실패", "#123", 999L);
            when(userRepository.findById(userId)).thenReturn(Optional.of(User.builder().id(userId).build()));
            when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> categoryService.createCustomCategory(userId, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);
        }

        @Test
        @DisplayName("실패 - 부모가 시스템 카테고리가 아님")
        void 부모가_시스템_카테고리가_아니면_생성할_수_없다() {
            // given
            Long userId = 1L;
            Long parentId = 10L;
            var request = new CreateCategoryRequest("실패", "#123", parentId);
            User fakeUser = User.builder().id(userId).build();
            Category fakeParentCategory = new Category(parentId, "남의 커스텀", "#456", fakeUser, null);

            when(userRepository.findById(userId)).thenReturn(Optional.of(fakeUser));
            when(categoryRepository.findById(parentId)).thenReturn(Optional.of(fakeParentCategory));

            // when & then
            assertThatThrownBy(() -> categoryService.createCustomCategory(userId, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.INVALID_PARENT_CATEGORY);
        }
    }

    @Nested
    @DisplayName("카테고리 조회")
    class FindCategoryTest {
        @Test
        @DisplayName("성공 - 시스템/사용자 카테고리 모두 조회")
        void 사용자가_조회_가능한_모든_카테고리를_가져온다() {
            // given
            Long userId = 1L;
            User fakeUser = User.builder().id(userId).build();
            Category systemCategory = new Category(1L, "운동", "#2ECC71", null, null);
            Category customCategory = new Category(2L, "개인 프로젝트", "#E74C3C", fakeUser, null);

            when(categoryRepository.findByUserIdOrUserIsNull(userId)).thenReturn(List.of(systemCategory, customCategory));

            // when
            List<CategoryResponse> responses = categoryService.findAllCategoriesForUser(userId);

            // then
            assertThat(responses).hasSize(2);
            assertThat(responses.stream().map(CategoryResponse::name)).contains("운동", "개인 프로젝트");
        }
    }

    @Nested
    @DisplayName("카테고리 수정")
    class UpdateCategoryTest {
        @Test
        @DisplayName("성공")
        void 자신의_커스텀_카테고리_정보를_성공적으로_수정한다() {
            // given
            Long userId = 1L;
            Long categoryId = 2L;
            var request = new UpdateCategoryRequest("수정된 이름", "#000000");
            User fakeUser = User.builder().id(userId).build();
            Category myCustomCategory = new Category(categoryId, "원본 이름", "#123456", fakeUser, null);

            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(myCustomCategory));

            // when
            CategoryResponse response = categoryService.updateCustomCategory(userId, categoryId, request);

            // then
            assertThat(response.id()).isEqualTo(categoryId);
            assertThat(response.name()).isEqualTo("수정된 이름");
            assertThat(response.color()).isEqualTo("#000000");
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void 다른_사람의_카테고리는_수정할_수_없다() {
            // given
            Long myUserId = 1L;
            Long otherUserId = 2L;
            Long otherUserCategoryId = 3L;
            var request = new UpdateCategoryRequest("수정 시도", "#000000");
            User fakeOtherUser = User.builder().id(otherUserId).build();
            Category otherUsersCategory = new Category(otherUserCategoryId, "남의 카테고리", "#FFFFFF", fakeOtherUser, null);

            when(categoryRepository.findById(otherUserCategoryId)).thenReturn(Optional.of(otherUsersCategory));

            // when & then
            assertThatThrownBy(() -> categoryService.updateCustomCategory(myUserId, otherUserCategoryId, request))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.PERMISSION_DENIED);
        }
    }

    @Nested
    @DisplayName("카테고리 삭제")
    class DeleteCategoryTest {
        @Test
        @DisplayName("성공")
        void 자신의_커스텀_카테고리를_성공적으로_삭제한다() {
            // given
            Long userId = 1L;
            Long categoryId = 2L;
            User fakeUser = User.builder().id(userId).build();
            Category myCustomCategory = new Category(categoryId, "삭제될 카테고리", "#123456", fakeUser, null);

            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(myCustomCategory));
            when(activityRepository.existsByCategory(myCustomCategory)).thenReturn(false);

            // when
            categoryService.deleteCustomCategory(userId, categoryId);

            // then
            verify(categoryRepository).deleteById(categoryId);
        }

        @Test
        @DisplayName("실패 - 활동 존재")
        void 활동이_존재하는_카테고리는_삭제할_수_없다() {
            // given
            Long userId = 1L;
            Long categoryId = 2L;
            User fakeUser = User.builder().id(userId).build();
            Category myCustomCategory = new Category(categoryId, "삭제될 카테고리", "#123456", fakeUser, null);

            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(myCustomCategory));
            when(activityRepository.existsByCategory(myCustomCategory)).thenReturn(true);

            // when & then
            assertThatThrownBy(() -> categoryService.deleteCustomCategory(userId, categoryId))
                    .isInstanceOf(BusinessException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.CATEGORY_IN_USE);
        }
    }
}
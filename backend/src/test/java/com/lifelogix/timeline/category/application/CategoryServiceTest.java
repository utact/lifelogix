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
import org.junit.jupiter.api.BeforeEach;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService 단위 테스트")
class CategoryServiceTest {

    @InjectMocks
    private CategoryService categoryService;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ActivityRepository activityRepository;

    private User user;
    private Category systemParentCategory;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).email("test@example.com").nickname("tester").build();
        systemParentCategory = new Category(10L, "운동", "#123456", null, null); // user가 null인 시스템 카테고리
    }


    @Nested
    @DisplayName("사용자 정의 카테고리 생성")
    class CreateCustomCategory {

        @Test
        @DisplayName("성공")
        void create_success() {
            // given
            CreateCategoryRequest request = new CreateCategoryRequest("헬스", "#FFFFFF", 10L);
            Category newCategory = new Category(request.name(), request.color(), user, systemParentCategory);

            given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
            given(categoryRepository.findById(request.parentId())).willReturn(Optional.of(systemParentCategory));
            given(categoryRepository.existsByUserAndName(user, request.name())).willReturn(false);
            given(categoryRepository.save(any(Category.class))).willReturn(newCategory);

            // when
            CategoryResponse response = categoryService.createCustomCategory(user.getId(), request);

            // then
            assertThat(response.name()).isEqualTo(request.name());
            assertThat(response.color()).isEqualTo(request.color());
            assertThat(response.isCustom()).isTrue();
            then(categoryRepository).should().save(any(Category.class));
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사용자")
        void create_fail_userNotFound() {
            // given
            CreateCategoryRequest request = new CreateCategoryRequest("헬스", "#FFFFFF", 10L);
            given(userRepository.findById(user.getId())).willReturn(Optional.empty());

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> categoryService.createCustomCategory(user.getId(), request));

            // then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.USER_NOT_FOUND);
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 부모 카테고리")
        void create_fail_parentCategoryNotFound() {
            // given
            CreateCategoryRequest request = new CreateCategoryRequest("헬스", "#FFFFFF", 999L);
            given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
            given(categoryRepository.findById(request.parentId())).willReturn(Optional.empty());

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> categoryService.createCustomCategory(user.getId(), request));

            // then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CATEGORY_NOT_FOUND);
        }

        @Test
        @DisplayName("실패 - 부모가 시스템 카테고리가 아님")
        void create_fail_parentIsNotSystemCategory() {
            // given
            CreateCategoryRequest request = new CreateCategoryRequest("헬스", "#FFFFFF", 11L);
            Category customParentCategory = new Category(11L, "유산소", "#000000", user, systemParentCategory); // user가 있는 커스텀 카테고리

            given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
            given(categoryRepository.findById(request.parentId())).willReturn(Optional.of(customParentCategory));

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> categoryService.createCustomCategory(user.getId(), request));

            // then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_PARENT_CATEGORY);
        }

        @Test
        @DisplayName("실패 - 카테고리 이름 중복")
        void create_fail_duplicateName() {
            // given
            CreateCategoryRequest request = new CreateCategoryRequest("헬스", "#FFFFFF", 10L);
            given(userRepository.findById(user.getId())).willReturn(Optional.of(user));
            given(categoryRepository.findById(request.parentId())).willReturn(Optional.of(systemParentCategory));
            given(categoryRepository.existsByUserAndName(user, request.name())).willReturn(true);

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> categoryService.createCustomCategory(user.getId(), request));

            // then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CATEGORY_NAME_DUPLICATE);
        }
    }

    @Nested
    @DisplayName("사용자별 모든 카테고리 조회")
    class FindAllCategoriesForUser {

        @Test
        @DisplayName("성공 - 시스템 카테고리와 사용자 정의 카테고리를 함께 반환")
        void findAll_success() {
            // given
            Category customCategory = new Category("헬스", "#FFFFFF", user, systemParentCategory);
            List<Category> categories = List.of(systemParentCategory, customCategory);
            given(categoryRepository.findByUserIdOrUserIsNull(user.getId())).willReturn(categories);

            // when
            List<CategoryResponse> responses = categoryService.findAllCategoriesForUser(user.getId());

            // then
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).name()).isEqualTo("운동");
            assertThat(responses.get(1).name()).isEqualTo("헬스");
        }
    }

    @Nested
    @DisplayName("사용자 정의 카테고리 수정")
    class UpdateCustomCategory {

        private Category customCategory;
        private UpdateCategoryRequest request;

        @BeforeEach
        void setUp() {
            customCategory = new Category(20L, "요가", "#AAAAAA", user, systemParentCategory);
            request = new UpdateCategoryRequest("필라테스", "#BBBBBB");
        }

        @Test
        @DisplayName("성공")
        void update_success() {
            // given
            given(categoryRepository.findById(customCategory.getId())).willReturn(Optional.of(customCategory));
            given(categoryRepository.existsByUserAndName(user, request.name())).willReturn(false);

            // when
            CategoryResponse response = categoryService.updateCustomCategory(user.getId(), customCategory.getId(), request);

            // then
            assertThat(response.id()).isEqualTo(customCategory.getId());
            assertThat(response.name()).isEqualTo("필라테스");
            assertThat(response.color()).isEqualTo("#BBBBBB");
        }

        @Test
        @DisplayName("성공 - 이름 변경 없이 색상만 변경")
        void update_success_onlyColor() {
            // given
            UpdateCategoryRequest onlyColorRequest = new UpdateCategoryRequest(customCategory.getName(), "#CCCCCC");
            given(categoryRepository.findById(customCategory.getId())).willReturn(Optional.of(customCategory));

            // when
            CategoryResponse response = categoryService.updateCustomCategory(user.getId(), customCategory.getId(), onlyColorRequest);

            // then
            assertThat(response.name()).isEqualTo(customCategory.getName()); // 이름은 그대로
            assertThat(response.color()).isEqualTo("#CCCCCC"); // 색상만 변경
            then(categoryRepository).should(never()).existsByUserAndName(any(), any()); // 이름 중복 검사는 실행되지 않아야 함
        }

        @Test
        @DisplayName("실패 - 권한 없음 (다른 사용자 카테고리)")
        void update_fail_permissionDenied_otherUser() {
            // given
            Long otherUserId = 2L;
            given(categoryRepository.findById(customCategory.getId())).willReturn(Optional.of(customCategory));

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> categoryService.updateCustomCategory(otherUserId, customCategory.getId(), request));

            // then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PERMISSION_DENIED);
        }

        @Test
        @DisplayName("실패 - 권한 없음 (시스템 카테고리)")
        void update_fail_permissionDenied_systemCategory() {
            // given
            given(categoryRepository.findById(systemParentCategory.getId())).willReturn(Optional.of(systemParentCategory));

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> categoryService.updateCustomCategory(user.getId(), systemParentCategory.getId(), request));

            // then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PERMISSION_DENIED);
        }

        @Test
        @DisplayName("실패 - 이름 중복")
        void update_fail_duplicateName() {
            // given
            given(categoryRepository.findById(customCategory.getId())).willReturn(Optional.of(customCategory));
            given(categoryRepository.existsByUserAndName(user, request.name())).willReturn(true);

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> categoryService.updateCustomCategory(user.getId(), customCategory.getId(), request));

            // then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CATEGORY_NAME_DUPLICATE);
        }
    }

    @Nested
    @DisplayName("사용자 정의 카테고리 삭제")
    class DeleteCustomCategory {

        private Category customCategory;

        @BeforeEach
        void setUp() {
            customCategory = new Category(20L, "요가", "#AAAAAA", user, systemParentCategory);
        }

        @Test
        @DisplayName("성공")
        void delete_success() {
            // given
            given(categoryRepository.findById(customCategory.getId())).willReturn(Optional.of(customCategory));
            given(activityRepository.existsByCategory(customCategory)).willReturn(false);
            willDoNothing().given(categoryRepository).deleteById(customCategory.getId());

            // when
            categoryService.deleteCustomCategory(user.getId(), customCategory.getId());

            // then
            then(categoryRepository).should().deleteById(customCategory.getId());
        }

        @Test
        @DisplayName("실패 - 권한 없음")
        void delete_fail_permissionDenied() {
            // given
            Long otherUserId = 2L;
            given(categoryRepository.findById(customCategory.getId())).willReturn(Optional.of(customCategory));

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> categoryService.deleteCustomCategory(otherUserId, customCategory.getId()));

            // then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PERMISSION_DENIED);
        }

        @Test
        @DisplayName("실패 - 사용 중인 카테고리")
        void delete_fail_categoryInUse() {
            // given
            given(categoryRepository.findById(customCategory.getId())).willReturn(Optional.of(customCategory));
            given(activityRepository.existsByCategory(customCategory)).willReturn(true);

            // when
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> categoryService.deleteCustomCategory(user.getId(), customCategory.getId()));

            // then
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CATEGORY_IN_USE);
        }
    }
}
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;

    @Transactional
    public CategoryResponse createCustomCategory(Long userId, CreateCategoryRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Category parentCategory = categoryRepository.findById(request.parentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        if (parentCategory.getUser() != null) {
            throw new BusinessException(ErrorCode.INVALID_PARENT_CATEGORY);
        }

        if (categoryRepository.existsByUserAndName(user, request.name())) {
            throw new BusinessException(ErrorCode.CATEGORY_NAME_DUPLICATE);
        }

        Category newCategory = new Category(request.name(), request.color(), user, parentCategory);
        Category savedCategory = categoryRepository.save(newCategory);
        return CategoryResponse.from(savedCategory);
    }

    public List<CategoryResponse> findAllCategoriesForUser(Long userId) {
        List<Category> categories = categoryRepository.findByUserIdOrUserIsNull(userId);
        return categories.stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Transactional
    public CategoryResponse updateCustomCategory(Long userId, Long categoryId, UpdateCategoryRequest request) {
        Category category = findCategoryById(categoryId);
        validateCategoryOwner(userId, category);

        // 수정하려는 이름이 현재 이름과 다를 때만 중복 검사 수행
        if (!category.getName().equals(request.name()) && categoryRepository.existsByUserAndName(category.getUser(), request.name())) {
            throw new BusinessException(ErrorCode.CATEGORY_NAME_DUPLICATE);
        }

        category.update(request.name(), request.color());
        return CategoryResponse.from(category);
    }

    @Transactional
    public void deleteCustomCategory(Long userId, Long categoryId) {
        Category category = findCategoryById(categoryId);
        validateCategoryOwner(userId, category);

        // 해당 카테고리를 사용하는 활동(Activity)이 있는지 확인
        if (activityRepository.existsByCategory(category)) {
            throw new BusinessException(ErrorCode.CATEGORY_IN_USE);
        }

        categoryRepository.deleteById(categoryId);
    }

    // 중복 로직 추출: ID로 카테고리 조회
    private Category findCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    // 중복 로직 추출: 카테고리 소유권 검증
    private void validateCategoryOwner(Long userId, Category category) {
        // 시스템 카테고리(user=null)이거나, 소유자가 다른 경우 수정/삭제 불가
        if (category.getUser() == null || !category.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
    }
}
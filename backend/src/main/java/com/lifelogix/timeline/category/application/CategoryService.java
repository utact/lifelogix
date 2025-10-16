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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CategoryService {

    private static final Logger log = LoggerFactory.getLogger(CategoryService.class);
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;

    @Transactional
    public CategoryResponse createCustomCategory(Long userId, CreateCategoryRequest request) {
        log.info("[Backend|CategoryService] CreateCustomCategory - Attempt for userId: {} with categoryName: {}", userId, request.name());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Category parentCategory = categoryRepository.findById(request.parentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        if (parentCategory.getUser() != null) {
            log.warn("[Backend|CategoryService] CreateCustomCategory - Failed: Invalid parent category for userId: {}", userId);
            throw new BusinessException(ErrorCode.INVALID_PARENT_CATEGORY);
        }

        if (categoryRepository.existsByUserAndName(user, request.name())) {
            log.warn("[Backend|CategoryService] CreateCustomCategory - Failed: Duplicate category name '{}' for userId: {}", request.name(), userId);
            throw new BusinessException(ErrorCode.CATEGORY_NAME_DUPLICATE);
        }

        Category newCategory = new Category(request.name(), request.color(), user, parentCategory);
        Category savedCategory = categoryRepository.save(newCategory);
        log.info("[Backend|CategoryService] CreateCustomCategory - Success for userId: {} with new categoryId: {}", userId, savedCategory.getId());
        return CategoryResponse.from(savedCategory);
    }

    public List<CategoryResponse> findAllCategoriesForUser(Long userId) {
        log.info("[Backend|CategoryService] FindAllCategories - Attempt for userId: {}", userId);
        List<Category> categories = categoryRepository.findByUserIdOrUserIsNull(userId);
        List<CategoryResponse> response = categories.stream()
                .map(CategoryResponse::from)
                .toList();
        log.info("[Backend|CategoryService] FindAllCategories - Success for userId: {}. Found {} categories.", userId, response.size());
        return response;
    }

    @Transactional
    public CategoryResponse updateCustomCategory(Long userId, Long categoryId, UpdateCategoryRequest request) {
        log.info("[Backend|CategoryService] UpdateCustomCategory - Attempt for userId: {} on categoryId: {}", userId, categoryId);
        Category category = findCategoryById(categoryId);
        validateCategoryOwner(userId, category);

        if (!category.getName().equals(request.name()) && categoryRepository.existsByUserAndName(category.getUser(), request.name())) {
            log.warn("[Backend|CategoryService] UpdateCustomCategory - Failed: Duplicate category name '{}' for userId: {}", request.name(), userId);
            throw new BusinessException(ErrorCode.CATEGORY_NAME_DUPLICATE);
        }

        category.update(request.name(), request.color());
        log.info("[Backend|CategoryService] UpdateCustomCategory - Success for userId: {} on categoryId: {}", userId, categoryId);
        return CategoryResponse.from(category);
    }

    @Transactional
    public void deleteCustomCategory(Long userId, Long categoryId) {
        log.info("[Backend|CategoryService] DeleteCustomCategory - Attempt for userId: {} on categoryId: {}", userId, categoryId);
        Category category = findCategoryById(categoryId);
        validateCategoryOwner(userId, category);

        if (activityRepository.existsByCategory(category)) {
            log.warn("[Backend|CategoryService] DeleteCustomCategory - Failed: Category in use for categoryId: {}", categoryId);
            throw new BusinessException(ErrorCode.CATEGORY_IN_USE);
        }

        categoryRepository.deleteById(categoryId);
        log.info("[Backend|CategoryService] DeleteCustomCategory - Success for userId: {} on categoryId: {}", userId, categoryId);
    }

    // 중복 로직 추출: ID로 카테고리 조회
    private Category findCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.warn("[Backend|CategoryService] FindCategoryById - Failed: Category not found for id: {}", categoryId);
                    return new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
                });
    }

    // 중복 로직 추출: 카테고리 소유권 검증
    private void validateCategoryOwner(Long userId, Category category) {
        // 시스템 카테고리(user=null)이거나, 소유자가 다른 경우 수정/삭제 불가
        if (category.getUser() == null || !category.getUser().getId().equals(userId)) {
            log.warn("[Backend|CategoryService] ValidateCategoryOwner - Failed: Permission denied for userId: {} on categoryId: {}", userId, category.getId());
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }
    }
}
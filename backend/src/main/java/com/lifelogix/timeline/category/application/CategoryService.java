package com.lifelogix.timeline.category.application;

import com.lifelogix.exception.BusinessException;
import com.lifelogix.exception.ErrorCode;
import com.lifelogix.timeline.category.api.dto.request.CreateCategoryRequest;
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
@Transactional(readOnly = true) // 기본적으로 읽기 전용 트랜잭션 적용
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Transactional // 쓰기 작업이므로 별도 트랜잭션 적용
    public CategoryResponse createCustomCategory(Long userId, CreateCategoryRequest request) {
        // 1. 요청한 사용자 여부 체크
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2. 부모 카테고리 여부 체크
        Category parentCategory = categoryRepository.findById(request.parentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        // 부모 카테고리는 반드시 시스템 기본 카테고리 (소유자 X)
        if (parentCategory.getUser() != null) {
            throw new BusinessException(ErrorCode.INVALID_PARENT_CATEGORY);
        }

        // 3. 새로운 카테고리 엔티티 생성
        Category newCategory = new Category(request.name(), request.color(), user, parentCategory);

        // 4. 레포지토리에 저장
        Category savedCategory = categoryRepository.save(newCategory);

        // 5. 응답 DTO로 변환하여 반환
        return CategoryResponse.from(savedCategory);
    }

    public List<CategoryResponse> findAllCategoriesForUser(Long userId) {
        // 1. 레포지토리에서 시스템 카테고리(user is null)와 현재 사용자의 카테고리를 모두 조회
        List<Category> categories = categoryRepository.findByUserIdOrUserIsNull(userId);

        // 2. 조회된 엔티티 목록을 응답 DTO 목록으로 변환하여 반환
        return categories.stream()
                .map(CategoryResponse::from)
                .toList();
    }
}
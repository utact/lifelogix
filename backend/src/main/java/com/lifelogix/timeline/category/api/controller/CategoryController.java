package com.lifelogix.timeline.category.api.controller;

import com.lifelogix.timeline.category.api.dto.request.CreateCategoryRequest;
import com.lifelogix.timeline.category.api.dto.request.UpdateCategoryRequest;
import com.lifelogix.timeline.category.api.dto.response.CategoryResponse;
import com.lifelogix.timeline.category.application.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 새로운 사용자 정의 카테고리를 생성
     **/
    @PostMapping
    public ResponseEntity<CategoryResponse> createCustomCategory(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CreateCategoryRequest request) { // 👈 @Valid 추가

        CategoryResponse response = categoryService.createCustomCategory(userId, request);
        URI location = URI.create("/api/v1/categories/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    /**
     * 사용 가능한 모든 카테고리 목록(시스템 기본 + 사용자 정의)을 조회
     **/
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories(
            @AuthenticationPrincipal Long userId) {

        List<CategoryResponse> responses = categoryService.findAllCategoriesForUser(userId);
        return ResponseEntity.ok(responses);
    }

    /**
     * 자신의 커스텀 카테고리 정보를 수정
     **/
    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> updateCustomCategory(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long categoryId,
            @Valid @RequestBody UpdateCategoryRequest request) {

        CategoryResponse response = categoryService.updateCustomCategory(userId, categoryId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 자신의 커스텀 카테고리를 삭제
     **/
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCustomCategory(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long categoryId) {

        categoryService.deleteCustomCategory(userId, categoryId);
        return ResponseEntity.noContent().build(); // 204 No Content 응답
    }
}
package com.lifelogix.timeline.category.api.controller;

import com.lifelogix.timeline.category.api.dto.request.CreateCategoryRequest;
import com.lifelogix.timeline.category.api.dto.request.UpdateCategoryRequest;
import com.lifelogix.timeline.category.api.dto.response.CategoryResponse;
import com.lifelogix.timeline.category.application.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private static final Logger log = LoggerFactory.getLogger(CategoryController.class);
    private final CategoryService categoryService;

    /**
     * 새로운 사용자 정의 카테고리를 생성
     */
    @PostMapping
    public ResponseEntity<CategoryResponse> createCustomCategory(
            Principal principal,
            @Valid @RequestBody CreateCategoryRequest request) {

        Long userId = Long.parseLong(principal.getName());
        log.info("[Backend|CategoryController] CreateCustomCategory - Received request from userId: {} with name: {}", userId, request.name());
        CategoryResponse response = categoryService.createCustomCategory(userId, request);
        URI location = URI.create("/api/v1/categories/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    /**
     * 사용 가능한 모든 카테고리 목록(시스템 기본 + 사용자 정의)을 조회
     */
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories(
            Principal principal) {

        Long userId = Long.parseLong(principal.getName());
        log.info("[Backend|CategoryController] GetAllCategories - Received request from userId: {}", userId);
        List<CategoryResponse> responses = categoryService.findAllCategoriesForUser(userId);
        return ResponseEntity.ok(responses);
    }

    /**
     * 자신의 커스텀 카테고리 정보를 수정
     */
    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> updateCustomCategory(
            Principal principal,
            @PathVariable Long categoryId,
            @Valid @RequestBody UpdateCategoryRequest request) {

        Long userId = Long.parseLong(principal.getName());
        log.info("[Backend|CategoryController] UpdateCustomCategory - Received request from userId: {} for categoryId: {}", userId, categoryId);
        CategoryResponse response = categoryService.updateCustomCategory(userId, categoryId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 자신의 커스텀 카테고리를 삭제
     */
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCustomCategory(
            Principal principal,
            @PathVariable Long categoryId) {

        Long userId = Long.parseLong(principal.getName());
        log.info("[Backend|CategoryController] DeleteCustomCategory - Received request from userId: {} for categoryId: {}", userId, categoryId);
        categoryService.deleteCustomCategory(userId, categoryId);
        return ResponseEntity.noContent().build();
    }
}
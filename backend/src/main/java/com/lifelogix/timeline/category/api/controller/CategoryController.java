package com.lifelogix.timeline.category.api.controller;

import com.lifelogix.timeline.category.api.dto.request.CreateCategoryRequest;
import com.lifelogix.timeline.category.api.dto.response.CategoryResponse;
import com.lifelogix.timeline.category.application.CategoryService;
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
            @AuthenticationPrincipal Long userId, // JWT 토큰에서 사용자 ID 추출
            @RequestBody CreateCategoryRequest request) {

        CategoryResponse response = categoryService.createCustomCategory(userId, request);

        // 생성된 리소스의 URI를 Location 헤더에 담아 201 Created 응답 반환
        URI location = URI.create("/api/v1/categories/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    /**
     * 사용 가능한 모든 카테고리 목록(시스템 기본 + 사용자 정의)을 조회
     **/
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories(
            @AuthenticationPrincipal Long userId) { // JWT 토큰에서 사용자 ID 추출

        List<CategoryResponse> responses = categoryService.findAllCategoriesForUser(userId);
        return ResponseEntity.ok(responses);
    }
}
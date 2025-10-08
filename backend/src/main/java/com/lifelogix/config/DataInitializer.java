package com.lifelogix.config;

import com.lifelogix.timeline.category.domain.Category;
import com.lifelogix.timeline.category.domain.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (categoryRepository.count() > 0) {
            return;
        }

        // 시스템 기본 카테고리 생성
        List<Category> systemCategories = List.of(
                new Category("수면", "#5D6D7E", null, null),
                new Category("식사", "#F5B041", null, null),
                new Category("개인정비", "#A9CCE3", null, null),
                new Category("이동", "#EB984E", null, null),
                new Category("직장/학교", "#3498DB", null, null),
                new Category("가사", "#85C1E9", null, null),
                new Category("학습", "#9B59B6", null, null),
                new Category("운동", "#2ECC71", null, null),
                new Category("자기계발", "#8E44AD", null, null),
                new Category("취미/오락", "#E74C3C", null, null),
                new Category("사회 활동", "#52BE80", null, null),
                new Category("휴식", "#FAD7A0", null, null)
        );

        categoryRepository.saveAll(systemCategories);
    }
}
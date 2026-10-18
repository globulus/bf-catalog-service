package com.shopfront.catalog.web;

import com.shopfront.catalog.model.Category;
import com.shopfront.catalog.repository.CategoryRepository;
import com.shopfront.catalog.service.DuplicateException;
import com.shopfront.catalog.web.dto.CategoryDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Categories are simple enough that they never got a service class.
@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categories")
public class CategoryController {

    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public List<CategoryDto> list() {
        return categoryRepository.findAllByOrderByNameAsc().stream()
                .map(c -> new CategoryDto(c.getId(), c.getName(), c.getSlug()))
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto create(@Valid @RequestBody CategoryDto request) {
        if (categoryRepository.existsBySlug(request.slug())) {
            throw new DuplicateException("Category slug " + request.slug() + " already exists");
        }
        Category saved = categoryRepository.save(new Category(request.name(), request.slug()));
        return new CategoryDto(saved.getId(), saved.getName(), saved.getSlug());
    }
}

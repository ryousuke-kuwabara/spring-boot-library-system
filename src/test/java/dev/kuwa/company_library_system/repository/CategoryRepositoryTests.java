package dev.kuwa.company_library_system.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import dev.kuwa.company_library_system.entity.Category;

@DataJpaTest
public class CategoryRepositoryTests {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void save_新しいカテゴリーを保存する() {
        // Arrange
        Category newCategory = new Category();
        newCategory.setName("CATEGORY_TEST");

        // Act
        Category savedCategory = categoryRepository.save(newCategory);

        // Assert
        assertThat(savedCategory).isNotNull();
        assertThat(savedCategory.getId()).isPositive();
        assertThat(savedCategory.getName()).isEqualTo("CATEGORY_TEST");
    }

    @Test
    void findById_既存のカテゴリーを取得する() {
        // Arrange
        Category category = new Category();
        category.setName("CATEGORY_TEST");
        Long categoryId = entityManager.persistAndGetId(category, Long.class);
        entityManager.flush();

        // Act
        Optional<Category> foundCategoryOptional = categoryRepository.findById(categoryId);

        // Assert
        assertThat(foundCategoryOptional).isPresent();
        Category foundCategory = foundCategoryOptional.get();
        assertThat(foundCategory.getId()).isEqualTo(categoryId);
        assertThat(foundCategory.getName()).isEqualTo("CATEGORY_TEST");
    }

    @Test
    void findById_存在しないIDで検索する() {
        // Arrange
        Long nonExistingId = 999L;

        // Act
        Optional<Category> foundCategoryOptional = categoryRepository.findById(nonExistingId);

        // Assert
        assertThat(foundCategoryOptional).isEmpty();
    }

    @Test
    void save_既存のカテゴリーを更新する() {
        // Arrange
        Category originalCategory = new Category();
        originalCategory.setName("CATEGORY_ORIGINAL");
        entityManager.persistAndFlush(originalCategory);

        Category categoryToUpdate = categoryRepository.findByName("CATEGORY_ORIGINAL").orElseThrow();
        categoryToUpdate.setName("CATEGORY_UPDATE");

        // Act
        Category updatedCategory = categoryRepository.save(categoryToUpdate);

        // Assert
        assertThat(updatedCategory.getName()).isEqualTo("CATEGORY_UPDATE");
        assertThat(updatedCategory.getId()).isEqualTo(originalCategory.getId());
    }

    @Test
    void delete_既存のカテゴリーを削除する() {
        // Arrange
        Category categoryToDelete = new Category();
        categoryToDelete.setName("CATEGORY_TEST");
        Long categoryId = entityManager.persistAndGetId(categoryToDelete, Long.class);

        // Act
        categoryRepository.delete(categoryToDelete);
        entityManager.flush();

        // Assert
        Optional<Category> foundOptional = categoryRepository.findById(categoryId);
        assertThat(foundOptional).isEmpty();
    }

    @Test
    void save_重複した名前で例外が発生する() {
        // Arrange
        Category existingCategory = new Category();
        existingCategory.setName("CATEGORY_TEST");
        entityManager.persistAndFlush(existingCategory);

        Category duplicateCategory = new Category();
        duplicateCategory.setName("CATEGORY_TEST");

        // Act and Assert
        assertThrows(DataIntegrityViolationException.class, () -> {
            categoryRepository.save(duplicateCategory);
        });
    }
}

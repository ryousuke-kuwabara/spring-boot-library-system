package dev.kuwa.company_library_system.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import dev.kuwa.company_library_system.entity.Book;
import dev.kuwa.company_library_system.entity.Category;

@DataJpaTest
public class BookRepositoryTests {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Category categoryTech;
    private Category categoryBusiness;

    @BeforeEach
    void setUp() {
        categoryTech = new Category();
        categoryTech.setName("技術書");
        entityManager.persist(categoryTech);

        categoryBusiness = new Category();
        categoryBusiness.setName("ビジネス書");
        entityManager.persist(categoryBusiness);

        entityManager.flush();
    }

    @Test
    void save_複数カテゴリーを持つ書籍を保存する() {
        // Arrange
        Book newBook = new Book();
        newBook.setIsbn("testIsbn");
        newBook.setTitle("testTitle");
        newBook.setAuthor("testAuthor");
        newBook.setPublisher("testPublisher");
        newBook.setCategories(Set.of(categoryTech, categoryBusiness));

        // Act
        Book savedBook = bookRepository.save(newBook);

        // Assert
        assertThat(savedBook).isNotNull();

        Book foundBook = entityManager.find(Book.class, savedBook.getId());
        assertThat(foundBook.getCategories()).hasSize(2);
        assertThat(foundBook.getCategories())
                .extracting(Category::getName)
                .containsExactlyInAnyOrder("技術書", "ビジネス書");
    }

    @Test
    void findById_関連カテゴリも同時に取得する() {
        // Arrange
        Book book = new Book();
        book.setIsbn("testIsbn");
        book.setTitle("testTitle");
        book.setAuthor("testAuthor");
        book.setPublisher("testPublisher");
        book.setCategories(Set.of(categoryTech));
        Long bookId = entityManager.persistAndGetId(book, Long.class);

        // Act
        Book foundBook = bookRepository.findById(bookId).orElseThrow();

        // Assert
        assertThat(foundBook.getTitle()).isEqualTo("testTitle");
        assertThat(foundBook.getCategories()).hasSize(1);
        assertThat(foundBook.getCategories().iterator().next().getName()).isEqualTo("技術書");
    }

    @Test
    void delete_書籍を削除してもカテゴリは残る() {
        // Arrange
        Book book = new Book();
        book.setIsbn("testIsbn");
        book.setTitle("testTitle");
        book.setAuthor("testAuthor");
        book.setPublisher("testPublisher");
        book.setCategories(Set.of(categoryTech));
        Long bookId = entityManager.persistAndGetId(book, Long.class);
        Long categoryId = categoryTech.getId();

        // Act
        bookRepository.delete(book);
        entityManager.flush();

        // Assert
        Book deletedBook = entityManager.find(Book.class, bookId);
        assertThat(deletedBook).isNull();

        Category stillExistingCategory = entityManager.find(Category.class, categoryId);
        assertThat(stillExistingCategory).isNotNull();
        assertThat(stillExistingCategory.getName()).isEqualTo("技術書");
    }

    @Test
    void save_重複したisbnで例外が発生する() {
        // Arrange
        Book existingBook = new Book();
        existingBook.setIsbn("testIsbn");
        existingBook.setTitle("testTitle");
        existingBook.setAuthor("testAuthor");
        existingBook.setPublisher("setPublisher");
        entityManager.persistAndFlush(existingBook);

        Book duplicatedBook = new Book();
        duplicatedBook.setIsbn("testIsbn");
        duplicatedBook.setTitle("anotherTitle");
        duplicatedBook.setAuthor("anotherAuthor");
        duplicatedBook.setPublisher("anotherPublisher");

        // Act and Assert
        assertThrows(DataIntegrityViolationException.class, () -> {
            bookRepository.saveAndFlush(duplicatedBook);
        });
    }
}

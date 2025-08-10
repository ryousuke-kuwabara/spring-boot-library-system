package dev.kuwa.company_library_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import dev.kuwa.company_library_system.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}

package com.tns.newscrawler.repository;

import com.tns.newscrawler.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findBySlug(String slug);

    List<Category> findByIsActiveTrue();

    Optional<Category> findByCode(String code);

    boolean existsByCode(String code);

    List<Category> findByIsActiveTrueOrderByNameAsc();

    Optional<Category> findBySlugAndIsActiveTrue(String slug);
    List<Category> findByParentIdIsNull();
    List<Category> findByParentId(Long parentId);
    @Query("SELECT COUNT(cat) FROM Category cat WHERE cat.isActive = true")
    Long CountCat();
}

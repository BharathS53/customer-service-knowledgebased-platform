package com.example.customerservice.repository;

import com.example.customerservice.entity.ArticleTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleTagRepository extends JpaRepository<ArticleTag, Long> {
    Optional<ArticleTag> findByNameIgnoreCase(String name);
    List<ArticleTag> findByNameIn(Collection<String> names);
    boolean existsByNameIgnoreCase(String name);
}

package com.proteinpro.bookmark.repository;

import com.proteinpro.bookmark.model.Bookmark;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends MongoRepository<Bookmark, String> {
    List<Bookmark> findAllByUserIdOrderByCreatedAtDesc(String userId);
    Optional<Bookmark> findByIdAndUserId(String id, String userId);
    boolean existsByUserIdAndProteinId(String userId, String proteinId);
}

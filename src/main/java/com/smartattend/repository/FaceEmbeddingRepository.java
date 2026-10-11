package com.smartattend.repository;

import com.smartattend.model.FaceEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FaceEmbeddingRepository extends JpaRepository<FaceEmbedding, Long> {
    List<FaceEmbedding> findByUserId(Long userId);
}

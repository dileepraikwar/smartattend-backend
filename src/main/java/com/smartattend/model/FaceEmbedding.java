package com.smartattend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "face_embeddings")
@Data
public class FaceEmbedding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String embeddingVector; // JSON array stored as a string

    private LocalDateTime createdAt = LocalDateTime.now();
}

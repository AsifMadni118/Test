package com.proteinpro.bookmark.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "bookmarks")
@CompoundIndex(name = "unique_user_protein", def = "{'userId': 1, 'proteinId': 1}", unique = true)
public class Bookmark {
    @Id
    private String id;
    @Indexed
    private String userId;
    private String proteinId;
    private BookmarkProteinData proteinData;
    private String comment;
    private Instant createdAt;
    private Instant updatedAt;

    public Bookmark() {
    }

    public Bookmark(String userId, String proteinId, BookmarkProteinData proteinData, String comment) {
        this.userId = userId;
        this.proteinId = proteinId;
        this.proteinData = proteinData;
        this.comment = comment;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getProteinId() { return proteinId; }
    public BookmarkProteinData getProteinData() { return proteinData; }
    public String getComment() { return comment; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setComment(String comment) { this.comment = comment; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

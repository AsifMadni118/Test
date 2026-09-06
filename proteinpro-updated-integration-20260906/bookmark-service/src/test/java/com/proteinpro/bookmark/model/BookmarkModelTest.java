package com.proteinpro.bookmark.model;

import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class BookmarkModelTest {
    @Test
    void storesTypedProteinSnapshotAndLifecycleFields() {
        BookmarkProteinData protein = new BookmarkProteinData("p1", "whey", BigDecimal.ONE,
                "$10", 20, "T", "F");
        Bookmark bookmark = new Bookmark("u1", "p1", protein, "compare");
        assertThat(Bookmark.class.getAnnotation(Document.class).collection()).isEqualTo("bookmarks");
        assertThat(bookmark.getId()).isNull();
        assertThat(bookmark.getUserId()).isEqualTo("u1");
        assertThat(bookmark.getProteinId()).isEqualTo("p1");
        assertThat(bookmark.getProteinData()).isSameAs(protein);
        assertThat(bookmark.getComment()).isEqualTo("compare");
        assertThat(bookmark.getCreatedAt()).isNotNull();
        assertThat(bookmark.getUpdatedAt()).isEqualTo(bookmark.getCreatedAt());
        bookmark.setComment("updated");
        var updated = java.time.Instant.now();
        bookmark.setUpdatedAt(updated);
        assertThat(bookmark.getComment()).isEqualTo("updated");
        assertThat(bookmark.getUpdatedAt()).isEqualTo(updated);

        assertThat(protein.getId()).isEqualTo("p1");
        assertThat(protein.getSource()).isEqualTo("whey");
        assertThat(protein.getCostGrams()).isEqualByComparingTo(BigDecimal.ONE);
        assertThat(protein.getCostPackage()).isEqualTo("$10");
        assertThat(protein.getProteinPerPack()).isEqualTo(20);
        assertThat(protein.getVegetarian()).isEqualTo("T");
        assertThat(protein.getVegan()).isEqualTo("F");
        assertThat(new BookmarkProteinData().getId()).isNull();
        assertThat(new Bookmark().getId()).isNull();
    }
}

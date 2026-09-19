package ru.rsoi.library.entity;

import jakarta.persistence.*;

@Entity @Table(name = "library_books")
public class LibraryBook {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "book_id") private Long bookId;
    @Column(name = "library_id") private Long libraryId;
    @Column(name = "available_count") private Integer availableCount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
    public Long getLibraryId() { return libraryId; }
    public void setLibraryId(Long libraryId) { this.libraryId = libraryId; }
    public Integer getAvailableCount() { return availableCount; }
    public void setAvailableCount(Integer availableCount) { this.availableCount = availableCount; }
}
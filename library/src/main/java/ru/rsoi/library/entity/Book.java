package ru.rsoi.library.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity @Table(name = "books")
public class Book {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "book_uid", unique = true, nullable = false)
    private UUID bookUid;
    private String name;
    private String author;
    private String genre;
    private String condition;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UUID getBookUid() { return bookUid; }
    public void setBookUid(UUID bookUid) { this.bookUid = bookUid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
}
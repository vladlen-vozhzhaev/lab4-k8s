package ru.rsoi.rating.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "rating")
public class Rating {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String username;

    @Column(nullable = false)
    private Integer stars;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Integer getStars() { return stars; }
    public void setStars(Integer stars) { this.stars = stars; }
}
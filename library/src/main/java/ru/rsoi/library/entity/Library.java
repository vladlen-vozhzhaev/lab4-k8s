package ru.rsoi.library.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity @Table(name = "library")
public class Library {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "library_uid", unique = true, nullable = false)
    private UUID libraryUid;
    private String name;
    private String city;
    private String address;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UUID getLibraryUid() { return libraryUid; }
    public void setLibraryUid(UUID libraryUid) { this.libraryUid = libraryUid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
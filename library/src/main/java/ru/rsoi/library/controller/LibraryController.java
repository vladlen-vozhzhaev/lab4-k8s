package ru.rsoi.library.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.rsoi.library.dto.BookDto;
import ru.rsoi.library.dto.LibraryDto;
import ru.rsoi.library.dto.PageResponse;
import ru.rsoi.library.entity.Book;
import ru.rsoi.library.entity.Library;
import ru.rsoi.library.entity.LibraryBook;
import ru.rsoi.library.repo.BookRepository;
import ru.rsoi.library.repo.LibraryBookRepository;
import ru.rsoi.library.repo.LibraryRepository;

import java.util.*;

@RestController
@RequestMapping("/api/v1")
public class LibraryController {

    private final LibraryRepository libraryRepo;
    private final BookRepository bookRepo;
    private final LibraryBookRepository lbRepo;

    public LibraryController(LibraryRepository libraryRepo, BookRepository bookRepo, LibraryBookRepository lbRepo) {
        this.libraryRepo = libraryRepo;
        this.bookRepo = bookRepo;
        this.lbRepo = lbRepo;
    }

    @GetMapping("/libraries")
    public PageResponse<LibraryDto> libraries(@RequestParam("city") String city,
                                              @RequestParam(value = "page", defaultValue = "1") int page,
                                              @RequestParam(value = "size", defaultValue = "10") int size) {
        Page<Library> p = libraryRepo.findByCity(city, PageRequest.of(Math.max(page - 1, 0), size));
        List<LibraryDto> items = p.getContent().stream()
                .map(l -> new LibraryDto(l.getLibraryUid(), l.getName(), l.getAddress(), l.getCity()))
                .toList();
        return new PageResponse<>(page, size, p.getTotalElements(), items);
    }

    @GetMapping("/libraries/{libraryUid}")
    public ResponseEntity<LibraryDto> getLibrary(@PathVariable UUID libraryUid) {
        return libraryRepo.findByLibraryUid(libraryUid)
                .map(l -> ResponseEntity.ok(new LibraryDto(l.getLibraryUid(), l.getName(), l.getAddress(), l.getCity())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/libraries/{libraryUid}/books")
    public ResponseEntity<PageResponse<BookDto>> books(@PathVariable UUID libraryUid,
                                                       @RequestParam(value = "page", defaultValue = "1") int page,
                                                       @RequestParam(value = "size", defaultValue = "25") int size,
                                                       @RequestParam(value = "showAll", defaultValue = "false") boolean showAll) {
        Optional<Library> libOpt = libraryRepo.findByLibraryUid(libraryUid);
        if (libOpt.isEmpty()) return ResponseEntity.notFound().build();
        Library lib = libOpt.get();

        List<LibraryBook> links = lbRepo.findByLibraryId(lib.getId());
        List<BookDto> all = new ArrayList<>();
        for (LibraryBook link : links) {
            Book b = bookRepo.findById(link.getBookId()).orElse(null);
            if (b == null) continue;
            if (!showAll && link.getAvailableCount() <= 0) continue;
            all.add(new BookDto(b.getBookUid(), b.getName(), b.getAuthor(), b.getGenre(),
                    b.getCondition(), link.getAvailableCount()));
        }

        int from = Math.min(Math.max(page - 1, 0) * size, all.size());
        int to = Math.min(from + size, all.size());
        List<BookDto> pageItems = all.subList(from, to);
        return ResponseEntity.ok(new PageResponse<>(page, size, all.size(), pageItems));
    }

    @GetMapping("/books/{bookUid}")
    public ResponseEntity<BookDto> getBook(@PathVariable UUID bookUid) {
        Optional<Book> bOpt = bookRepo.findByBookUid(bookUid);
        if (bOpt.isEmpty()) return ResponseEntity.notFound().build();
        Book b = bOpt.get();
        return ResponseEntity.ok(new BookDto(b.getBookUid(), b.getName(), b.getAuthor(),
                b.getGenre(), b.getCondition(), null));
    }

    @PostMapping("/libraries/{libraryUid}/books/{bookUid}/reserve")
    public ResponseEntity<Void> reserve(@PathVariable UUID libraryUid, @PathVariable UUID bookUid) {
        Optional<Library> libOpt = libraryRepo.findByLibraryUid(libraryUid);
        Optional<Book> bOpt = bookRepo.findByBookUid(bookUid);
        if (libOpt.isEmpty() || bOpt.isEmpty()) return ResponseEntity.notFound().build();

        Optional<LibraryBook> lbOpt = lbRepo.findByLibraryIdAndBookId(libOpt.get().getId(), bOpt.get().getId());
        if (lbOpt.isEmpty()) return ResponseEntity.notFound().build();

        LibraryBook lb = lbOpt.get();
        if (lb.getAvailableCount() <= 0) return ResponseEntity.status(HttpStatus.CONFLICT).build();
        lb.setAvailableCount(lb.getAvailableCount() - 1);
        lbRepo.save(lb);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/libraries/{libraryUid}/books/{bookUid}/return")
    public ResponseEntity<Void> returnBook(@PathVariable UUID libraryUid, @PathVariable UUID bookUid) {
        Optional<Library> libOpt = libraryRepo.findByLibraryUid(libraryUid);
        Optional<Book> bOpt = bookRepo.findByBookUid(bookUid);
        if (libOpt.isEmpty() || bOpt.isEmpty()) return ResponseEntity.notFound().build();

        Optional<LibraryBook> lbOpt = lbRepo.findByLibraryIdAndBookId(libOpt.get().getId(), bOpt.get().getId());
        if (lbOpt.isEmpty()) return ResponseEntity.notFound().build();

        LibraryBook lb = lbOpt.get();
        lb.setAvailableCount(lb.getAvailableCount() + 1);
        lbRepo.save(lb);
        return ResponseEntity.ok().build();
    }
}
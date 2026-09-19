package ru.rsoi.library.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.rsoi.library.entity.Book;
import ru.rsoi.library.entity.Library;
import ru.rsoi.library.entity.LibraryBook;
import ru.rsoi.library.repo.BookRepository;
import ru.rsoi.library.repo.LibraryBookRepository;
import ru.rsoi.library.repo.LibraryRepository;

import java.util.UUID;

@Component
public class DataInitializer implements CommandLineRunner {
    private final LibraryRepository libraryRepo;
    private final BookRepository bookRepo;
    private final LibraryBookRepository lbRepo;

    public DataInitializer(LibraryRepository libraryRepo, BookRepository bookRepo, LibraryBookRepository lbRepo) {
        this.libraryRepo = libraryRepo;
        this.bookRepo = bookRepo;
        this.lbRepo = lbRepo;
    }

    @Override
    public void run(String... args) {
        if (libraryRepo.count() > 0) return;

        Library l = new Library();
        l.setLibraryUid(UUID.fromString("83575e12-7ce0-48ee-9931-51919ff3c9ee"));
        l.setName("Библиотека имени 7 Непьющих");
        l.setCity("Москва");
        l.setAddress("2-я Бауманская ул., д.5, стр.1");
        libraryRepo.save(l);

        Book b = new Book();
        b.setBookUid(UUID.fromString("f7cdc58f-2caf-4b15-9727-f89dcc629b27"));
        b.setName("Краткий курс C++ в 7 томах");
        b.setAuthor("Бьерн Страуструп");
        b.setGenre("Научная фантастика");
        b.setCondition("EXCELLENT");
        bookRepo.save(b);

        LibraryBook lb = new LibraryBook();
        lb.setLibraryId(l.getId());
        lb.setBookId(b.getId());
        lb.setAvailableCount(1);
        lbRepo.save(lb);
    }
}
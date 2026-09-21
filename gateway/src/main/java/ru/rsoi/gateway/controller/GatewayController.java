package ru.rsoi.gateway.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import ru.rsoi.gateway.dto.ReturnBookRequest;
import ru.rsoi.gateway.dto.TakeBookRequest;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/v1")
public class GatewayController {

    private final RestClient client;

    @Value("${rating-url}")      private String ratingUrl;
    @Value("${library-url}")     private String libraryUrl;
    @Value("${reservation-url}") private String reservationUrl;

    public GatewayController(RestClient client) { this.client = client; }

    // ---------- Libraries ----------
    @GetMapping("/libraries")
    public ResponseEntity<Object> libraries(@RequestParam("city") String city,
                                            @RequestParam(value = "page", defaultValue = "1") int page,
                                            @RequestParam(value = "size", defaultValue = "10") int size) {
        Object body = client.get()
                .uri(libraryUrl + "/api/v1/libraries?city={c}&page={p}&size={s}", city, page, size)
                .retrieve().body(Object.class);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/libraries/{libraryUid}/books")
    public ResponseEntity<Object> books(@PathVariable String libraryUid,
                                        @RequestParam(value = "page", defaultValue = "1") int page,
                                        @RequestParam(value = "size", defaultValue = "25") int size,
                                        @RequestParam(value = "showAll", defaultValue = "false") boolean showAll) {
        Object body = client.get()
                .uri(libraryUrl + "/api/v1/libraries/{u}/books?page={p}&size={s}&showAll={a}",
                        libraryUid, page, size, showAll)
                .retrieve().body(Object.class);
        return ResponseEntity.ok(body);
    }

    // ---------- Rating ----------
    @GetMapping("/rating")
    public ResponseEntity<?> rating(@RequestHeader("X-User-Name") String username) {
        try {
            Object body = client.get().uri(ratingUrl + "/api/v1/rating")
                    .header("X-User-Name", username)
                    .retrieve().body(Object.class);
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("message", "Bonus Service unavailable"));
        }
    }

    // ---------- Reservations list ----------
    @GetMapping("/reservations")
    public ResponseEntity<List<Map<String, Object>>> listReservations(@RequestHeader("X-User-Name") String username) {
        List<Map<String, Object>> rows = client.get()
                .uri(reservationUrl + "/api/v1/reservations")
                .header("X-User-Name", username)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        List<Map<String, Object>> out = new ArrayList<>();
        if (rows != null) {
            for (Map<String, Object> r : rows) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("reservationUid", r.get("reservationUid"));
                item.put("status", r.get("status"));
                item.put("startDate", r.get("startDate"));
                item.put("tillDate", r.get("tillDate"));

                Map<String, Object> book = fetchMap(libraryUrl + "/api/v1/books/" + r.get("bookUid"));
                Map<String, Object> library = fetchMap(libraryUrl + "/api/v1/libraries/" + r.get("libraryUid"));

                if (book != null) {
                    item.put("book", extractBook(book));
                } else {
                    // fallback: только bookUid
                    item.put("book", Map.of("bookUid", r.get("bookUid")));
                }
                if (library != null) {
                    item.put("library", extractLibrary(library));
                } else {
                    // fallback: только libraryUid
                    item.put("library", Map.of("libraryUid", r.get("libraryUid")));
                }
                out.add(item);
            }
        }
        return ResponseEntity.ok(out);
    }

    // ---------- Take book ----------
    @PostMapping("/reservations")
    public ResponseEntity<?> takeBook(@RequestHeader("X-User-Name") String username,
                                      @RequestBody TakeBookRequest req) {
        // 1. rating
        Map<String, Object> rating;
        try {
            rating = client.get().uri(ratingUrl + "/api/v1/rating")
                    .header("X-User-Name", username)
                    .retrieve().body(new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("message", "Bonus Service unavailable"));
        }
        if (rating == null) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("message", "Bonus Service unavailable"));
        }
        int stars = ((Number) rating.get("stars")).intValue();

        // 2. active reservations
        Long active;
        try {
            active = client.get().uri(reservationUrl + "/api/v1/reservations/count-active")
                    .header("X-User-Name", username)
                    .retrieve().body(Long.class);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("message", "Bonus Service unavailable"));
        }
        long rented = active == null ? 0 : active;

        if (rented >= stars) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "User cannot take more books"));
        }

        // 3. reserve in library
        ResponseEntity<Void> reserve;
        try {
            reserve = client.post()
                    .uri(libraryUrl + "/api/v1/libraries/{l}/books/{b}/reserve", req.libraryUid(), req.bookUid())
                    .retrieve().toBodilessEntity();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("message", "Bonus Service unavailable"));
        }
        if (reserve.getStatusCode().isError()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Book is not available"));
        }

        // 4. create reservation
        Map<String, Object> createBody = new LinkedHashMap<>();
        createBody.put("bookUid", req.bookUid());
        createBody.put("libraryUid", req.libraryUid());
        createBody.put("tillDate", req.tillDate());

        Map<String, Object> reservation;
        try {
            reservation = client.post()
                    .uri(reservationUrl + "/api/v1/reservations")
                    .header("X-User-Name", username)
                    .body(createBody)
                    .retrieve().body(new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("message", "Bonus Service unavailable"));
        }

        // 5. enrich
        Map<String, Object> book = fetchMap(libraryUrl + "/api/v1/books/" + req.bookUid());
        Map<String, Object> library = fetchMap(libraryUrl + "/api/v1/libraries/" + req.libraryUid());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("reservationUid", reservation.get("reservationUid"));
        response.put("status", reservation.get("status"));
        response.put("startDate", reservation.get("startDate"));
        response.put("tillDate", reservation.get("tillDate"));
        response.put("book", extractBook(book));
        response.put("library", extractLibrary(library));
        response.put("rating", Map.of("stars", stars));
        return ResponseEntity.ok(response);
    }

    // ---------- Return book ----------
    @PostMapping("/reservations/{reservationUid}/return")
    public ResponseEntity<?> returnBook(@RequestHeader("X-User-Name") String username,
                                        @PathVariable String reservationUid,
                                        @RequestBody ReturnBookRequest req) {

        // find reservation
        List<Map<String, Object>> rows;
        try {
            rows = client.get()
                    .uri(reservationUrl + "/api/v1/reservations")
                    .header("X-User-Name", username)
                    .retrieve().body(new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("message", "Bonus Service unavailable"));
        }

        Map<String, Object> current = null;
        if (rows != null) {
            for (Map<String, Object> r : rows) {
                if (reservationUid.equals(r.get("reservationUid"))) { current = r; break; }
            }
        }
        if (current == null) return ResponseEntity.notFound().build();

        // original condition
        Map<String, Object> book = fetchMap(libraryUrl + "/api/v1/books/" + current.get("bookUid"));
        String originalCondition = book == null ? null : (String) book.get("condition");

        // return in reservation
        Map<String, Object> returnBody = new LinkedHashMap<>();
        returnBody.put("condition", req.condition());
        returnBody.put("date", req.date());
        try {
            client.post().uri(reservationUrl + "/api/v1/reservations/{u}/return", reservationUid)
                    .header("X-User-Name", username)
                    .body(returnBody)
                    .retrieve().toBodilessEntity();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("message", "Bonus Service unavailable"));
        }

        // return in library — игнорируем ошибку (не критично)
        try {
            client.post().uri(libraryUrl + "/api/v1/libraries/{l}/books/{b}/return",
                            current.get("libraryUid"), current.get("bookUid"))
                    .retrieve().toBodilessEntity();
        } catch (Exception e) {
            // library недоступен — пропускаем, книга всё равно возвращена
        }

        // rating recalculation — игнорируем ошибку (не критично)
        LocalDate till = LocalDate.parse((String) current.get("tillDate"));
        boolean late = req.date() != null && req.date().isAfter(till);
        boolean badCondition = originalCondition != null && !originalCondition.equals(req.condition());
        int delta = (late || badCondition) ? -10 : 1;

        try {
            client.post().uri(ratingUrl + "/api/v1/rating?delta={d}", delta)
                    .header("X-User-Name", username)
                    .retrieve().toBodilessEntity();
        } catch (Exception e) {
            // rating недоступен — пропускаем, книга всё равно возвращена
        }

        return ResponseEntity.noContent().build();
    }

    // ---------- helpers ----------
    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchMap(String url) {
        try {
            return client.get().uri(url).retrieve().body(new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, Object> extractBook(Map<String, Object> book) {
        if (book == null) return Map.of();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("bookUid", book.get("bookUid"));
        out.put("name", book.get("name"));
        out.put("author", book.get("author"));
        out.put("genre", book.get("genre"));
        return out;
    }

    private Map<String, Object> extractLibrary(Map<String, Object> library) {
        if (library == null) return Map.of();
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("libraryUid", library.get("libraryUid"));
        out.put("name", library.get("name"));
        out.put("address", library.get("address"));
        out.put("city", library.get("city"));
        return out;
    }
}
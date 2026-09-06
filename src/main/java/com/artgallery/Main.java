package com.artgallery;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class Main {
    private static final int PORT = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));

    private record Artist(int id, String name, int birthYear, Integer deathYear, String country,
                          String movement, String description, String portrait) {}

    private record Painting(int id, String title, int artistId, int year, String genre,
                            String museum, String description, String image) {}

    private static final List<Artist> ARTISTS = new CopyOnWriteArrayList<>(List.of(
            new Artist(1, "Vincent van Gogh", 1853, 1890, "Netherlands", "Post-Impressionism",
                    "Dutch painter known for expressive color, energetic brushwork and emotionally direct landscapes and portraits.",
                    "/images/van-gogh.svg"),
            new Artist(2, "Claude Monet", 1840, 1926, "France", "Impressionism",
                    "French painter and a founder of Impressionism, famous for studying light, atmosphere and the same motif at different times of day.",
                    "/images/monet.svg"),
            new Artist(3, "Leonardo da Vinci", 1452, 1519, "Italy", "High Renaissance",
                    "Italian artist, engineer and scientist whose paintings combine observation, anatomy, perspective and subtle tonal modeling.",
                    "/images/leonardo.svg"),
            new Artist(4, "Pablo Picasso", 1881, 1973, "Spain", "Cubism",
                    "Spanish painter and sculptor whose long career moved through many styles and helped define modern art.",
                    "/images/picasso.svg"),
            new Artist(5, "Salvador Dalí", 1904, 1989, "Spain", "Surrealism",
                    "Spanish surrealist known for dreamlike imagery, precise technique and unexpected combinations of familiar objects.",
                    "/images/dali.svg"),
            new Artist(6, "Edvard Munch", 1863, 1944, "Norway", "Expressionism",
                    "Norwegian painter whose work explored anxiety, memory, loneliness and intense psychological states.",
                    "/images/munch.svg")
    ));

    private static final List<Painting> PAINTINGS = new CopyOnWriteArrayList<>(List.of(
            new Painting(101, "The Starry Night", 1, 1889, "Post-Impressionism", "Museum of Modern Art, New York",
                    "A nocturnal landscape with a turbulent sky, a quiet village and a tall cypress linking earth and sky.", "/images/starry-night.svg"),
            new Painting(102, "Sunflowers", 1, 1888, "Post-Impressionism", "National Gallery, London",
                    "A still life from Van Gogh's celebrated series of sunflowers painted in Arles.", "/images/sunflowers.svg"),
            new Painting(103, "Bedroom in Arles", 1, 1888, "Post-Impressionism", "Van Gogh Museum, Amsterdam",
                    "A simplified interior with strong color contrasts and intentionally flattened perspective.", "/images/bedroom.svg"),
            new Painting(104, "Irises", 1, 1889, "Post-Impressionism", "J. Paul Getty Museum, Los Angeles",
                    "A close view of irises painted during Van Gogh's stay at Saint-Rémy.", "/images/irises.svg"),
            new Painting(201, "Impression, Sunrise", 2, 1872, "Impressionism", "Musée Marmottan Monet, Paris",
                    "A misty harbor scene whose title helped give the Impressionist movement its name.", "/images/impression-sunrise.svg"),
            new Painting(202, "Water Lilies", 2, 1906, "Impressionism", "Art Institute of Chicago",
                    "One of Monet's many studies of the water garden at his home in Giverny.", "/images/water-lilies.svg"),
            new Painting(203, "Woman with a Parasol", 2, 1875, "Impressionism", "National Gallery of Art, Washington",
                    "A breezy outdoor portrait emphasizing light, movement and a low viewpoint.", "/images/parasol.svg"),
            new Painting(301, "Mona Lisa", 3, 1503, "High Renaissance", "Louvre Museum, Paris",
                    "A portrait celebrated for its subtle expression, atmospheric background and sfumato modeling.", "/images/mona-lisa.svg"),
            new Painting(302, "The Last Supper", 3, 1498, "High Renaissance", "Santa Maria delle Grazie, Milan",
                    "A monumental mural depicting the dramatic moment after Christ announces a betrayal.", "/images/last-supper.svg"),
            new Painting(303, "Lady with an Ermine", 3, 1490, "High Renaissance", "National Museum, Kraków",
                    "A portrait notable for its turning pose, soft modeling and carefully observed animal.", "/images/ermine.svg"),
            new Painting(401, "Guernica", 4, 1937, "Cubism", "Museo Reina Sofía, Madrid",
                    "A monumental black-and-white response to the bombing of Guernica during the Spanish Civil War.", "/images/guernica.svg"),
            new Painting(402, "The Weeping Woman", 4, 1937, "Cubism", "Tate Modern, London",
                    "A fractured portrait using angular forms and intense emotion.", "/images/weeping-woman.svg"),
            new Painting(403, "Girl before a Mirror", 4, 1932, "Cubism", "Museum of Modern Art, New York",
                    "A vividly colored portrait exploring reflection, identity and doubled forms.", "/images/girl-mirror.svg"),
            new Painting(501, "The Persistence of Memory", 5, 1931, "Surrealism", "Museum of Modern Art, New York",
                    "A dreamlike landscape famous for soft watches draped over strange objects.", "/images/persistence-memory.svg"),
            new Painting(502, "Swans Reflecting Elephants", 5, 1937, "Surrealism", "Private collection",
                    "A double-image composition in which reflected swans transform into elephants.", "/images/swans-elephants.svg"),
            new Painting(503, "Metamorphosis of Narcissus", 5, 1937, "Surrealism", "Tate Modern, London",
                    "A carefully constructed double image inspired by the myth of Narcissus.", "/images/narcissus.svg"),
            new Painting(601, "The Scream", 6, 1893, "Expressionism", "National Museum, Oslo",
                    "An iconic image of existential anxiety set against a dramatically colored sky.", "/images/scream.svg"),
            new Painting(602, "Madonna", 6, 1894, "Expressionism", "Munch Museum, Oslo",
                    "A psychologically charged figure painting combining tenderness and unease.", "/images/madonna.svg"),
            new Painting(603, "The Dance of Life", 6, 1899, "Expressionism", "National Gallery, Oslo",
                    "A symbolic scene of couples dancing through stages of life.", "/images/dance-life.svg")
    ));

    private static final AtomicInteger NEXT_ARTIST_ID = new AtomicInteger(7);
    private static final AtomicInteger NEXT_PAINTING_ID = new AtomicInteger(700);

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", Main::route);
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.start();

        System.out.println("Art Gallery QA is running: http://localhost:" + PORT);
        System.out.println("API docs: http://localhost:" + PORT + "/api-docs");
        System.out.println("API Lab:  http://localhost:" + PORT + "/api-lab");
        System.out.println("Press Ctrl+C to stop.");
    }

    private static void route(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            if (path.startsWith("/api/")) {
                handleApi(exchange, path);
                return;
            }
            serveStatic(exchange, path);
        } catch (ResponseAlreadySentException ignored) {
            return;
        } catch (BadJsonException e) {
            sendJson(exchange, 400, errorJson("Invalid JSON body"));
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, errorJson("Internal server error"));
        }
    }

    private static void handleApi(HttpExchange exchange, String path) throws IOException {
        addCommonHeaders(exchange);
        String method = exchange.getRequestMethod().toUpperCase();

        if ("OPTIONS".equals(method)) {
            exchange.getResponseHeaders().set("Allow", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, DELETE, OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
            sendNoContent(exchange, 204);
            return;
        }

        if (path.equals("/api/health")) {
            if (!"GET".equals(method)) {
                methodNotAllowed(exchange, "GET, OPTIONS");
                return;
            }
            sendJson(exchange, 200, "{\"status\":\"UP\",\"date\":" + q(LocalDate.now().toString()) + "}");
            return;
        }

        if (path.equals("/api/artists")) {
            switch (method) {
                case "GET" -> handleArtistsList(exchange);
                case "POST" -> createArtist(exchange);
                default -> methodNotAllowed(exchange, "GET, POST, OPTIONS");
            }
            return;
        }

        if (path.startsWith("/api/artists/")) {
            String suffix = path.substring("/api/artists/".length());
            if (suffix.endsWith("/paintings")) {
                String idPart = suffix.substring(0, suffix.length() - "/paintings".length());
                Integer artistId = parseIntOrNull(idPart);
                if (artistId == null) {
                    sendJson(exchange, 400, errorJson("Invalid artist id"));
                    return;
                }
                if (!"GET".equals(method)) {
                    methodNotAllowed(exchange, "GET, OPTIONS");
                    return;
                }
                if (findArtist(artistId).isEmpty()) {
                    sendJson(exchange, 404, errorJson("Artist not found"));
                    return;
                }
                List<Painting> result = PAINTINGS.stream().filter(p -> p.artistId() == artistId).toList();
                sendJson(exchange, 200, paintingsJson(result));
                return;
            }

            Integer artistId = parseIntOrNull(suffix);
            if (artistId == null) {
                sendJson(exchange, 400, errorJson("Invalid artist id"));
                return;
            }

            switch (method) {
                case "GET" -> getArtist(exchange, artistId);
                case "PUT" -> replaceArtist(exchange, artistId);
                case "PATCH" -> patchArtist(exchange, artistId);
                case "DELETE" -> deleteArtist(exchange, artistId);
                default -> methodNotAllowed(exchange, "GET, PUT, PATCH, DELETE, OPTIONS");
            }
            return;
        }

        if (path.equals("/api/paintings")) {
            switch (method) {
                case "GET" -> handlePaintingsList(exchange);
                case "POST" -> createPainting(exchange);
                default -> methodNotAllowed(exchange, "GET, POST, OPTIONS");
            }
            return;
        }

        if (path.startsWith("/api/paintings/")) {
            Integer paintingId = parseIntOrNull(path.substring("/api/paintings/".length()));
            if (paintingId == null) {
                sendJson(exchange, 400, errorJson("Invalid painting id"));
                return;
            }

            switch (method) {
                case "GET" -> getPainting(exchange, paintingId);
                case "PUT" -> replacePainting(exchange, paintingId);
                case "PATCH" -> patchPainting(exchange, paintingId);
                case "DELETE" -> deletePainting(exchange, paintingId);
                default -> methodNotAllowed(exchange, "GET, PUT, PATCH, DELETE, OPTIONS");
            }
            return;
        }

        sendJson(exchange, 404, errorJson("Endpoint not found"));
    }

    private static void handleArtistsList(HttpExchange exchange) throws IOException {
        Map<String, String> query = parseQuery(exchange.getRequestURI());
        String search = query.getOrDefault("search", "");
        List<Artist> result = ARTISTS.stream()
                .filter(a -> search.isBlank() || a.name().contains(search))
                .toList();
        sendJson(exchange, 200, artistsJson(result));
    }

    private static void getArtist(HttpExchange exchange, int artistId) throws IOException {
        Optional<Artist> artist = findArtist(artistId);
        if (artist.isEmpty()) {
            sendJson(exchange, 404, errorJson("Artist not found"));
            return;
        }
        sendJson(exchange, 200, artistJson(artist.get()));
    }

    private static void createArtist(HttpExchange exchange) throws IOException {
        Map<String, String> body = readJsonBody(exchange);
        String requestedName = body.get("name");
        if (requestedName != null && ARTISTS.stream().anyMatch(a -> a.name().equalsIgnoreCase(requestedName))) {
            sendJson(exchange, 409, errorJson("Artist name already exists"));
            return;
        }
        String validation = validateArtist(body, true, null);
        if (validation != null) {
            sendJson(exchange, 400, errorJson(validation));
            return;
        }

        int id = NEXT_ARTIST_ID.getAndIncrement();
        Artist artist = artistFrom(body, id, null);
        ARTISTS.add(artist);
        exchange.getResponseHeaders().set("Location", "/api/artists/" + id);
        sendJson(exchange, 201, artistJson(artist));
    }

    private static void replaceArtist(HttpExchange exchange, int id) throws IOException {
        Optional<Artist> existing = findArtist(id);
        if (existing.isEmpty()) {
            sendJson(exchange, 404, errorJson("Artist not found"));
            return;
        }

        Map<String, String> body = readJsonBody(exchange);
        String requestedName = body.get("name");
        if (requestedName != null && ARTISTS.stream().anyMatch(a -> a.id() != id && a.name().equalsIgnoreCase(requestedName))) {
            sendJson(exchange, 409, errorJson("Artist name already exists"));
            return;
        }
        String validation = validateArtist(body, true, existing.get());
        if (validation != null) {
            sendJson(exchange, 400, errorJson(validation));
            return;
        }

        Artist updated = artistFrom(body, id, existing.get());
        replaceArtistInList(updated);
        sendJson(exchange, 200, artistJson(updated));
    }

    private static void patchArtist(HttpExchange exchange, int id) throws IOException {
        Optional<Artist> existing = findArtist(id);
        if (existing.isEmpty()) {
            sendJson(exchange, 404, errorJson("Artist not found"));
            return;
        }

        Map<String, String> body = readJsonBody(exchange);
        String requestedName = body.get("name");
        if (requestedName != null && ARTISTS.stream().anyMatch(a -> a.id() != id && a.name().equalsIgnoreCase(requestedName))) {
            sendJson(exchange, 409, errorJson("Artist name already exists"));
            return;
        }
        if (body.isEmpty()) {
            sendJson(exchange, 400, errorJson("Request body must contain at least one field"));
            return;
        }
        String validation = validateArtist(body, false, existing.get());
        if (validation != null) {
            sendJson(exchange, 400, errorJson(validation));
            return;
        }

        Artist updated = artistFrom(body, id, existing.get());
        updated = new Artist(updated.id(), updated.name(), updated.birthYear(), updated.deathYear(),
                existing.get().country(), updated.movement(), updated.description(), updated.portrait());
        replaceArtistInList(updated);
        sendJson(exchange, 200, artistJson(updated));
    }

    private static void deleteArtist(HttpExchange exchange, int id) throws IOException {
        Optional<Artist> existing = findArtist(id);
        if (existing.isEmpty()) {
            sendJson(exchange, 404, errorJson("Artist not found"));
            return;
        }
        ARTISTS.removeIf(a -> a.id() == id);
        sendNoContent(exchange, 204);
    }

    private static void handlePaintingsList(HttpExchange exchange) throws IOException {
        Map<String, String> query = parseQuery(exchange.getRequestURI());
        String search = query.getOrDefault("search", "");
        Integer artistId = parseIntOrNull(query.get("artistId"));
        Integer yearFrom = parseIntOrNull(query.get("yearFrom"));
        Integer yearTo = parseIntOrNull(query.get("yearTo"));
        String genre = query.getOrDefault("genre", "");
        String sort = query.getOrDefault("sort", "title-asc");
        int page = positiveOrDefault(parseIntOrNull(query.get("page")), 1);
        int size = positiveOrDefault(parseIntOrNull(query.get("size")), 6);
        size = Math.min(size, 50);

        List<Painting> filtered = PAINTINGS.stream()
                .filter(p -> search.isBlank() || p.title().contains(search))
                .filter(p -> artistId == null || p.artistId() == artistId)
                .filter(p -> genre.isBlank() || p.genre().equals(genre))
                .filter(p -> yearFrom == null || p.year() >= yearFrom)
                .filter(p -> yearTo == null || p.year() <= yearTo)
                .collect(Collectors.toCollection(ArrayList::new));

        switch (sort) {
            case "title-desc" -> filtered.sort(Comparator.comparing(Painting::title).reversed());
            case "year-asc" -> filtered.sort(Comparator.comparingInt(Painting::year));
            case "year-desc" -> filtered.sort(Comparator.comparingInt(Painting::year));
            default -> filtered.sort(Comparator.comparing(Painting::title));
        }

        int total = filtered.size();
        int from = Math.min((page - 1) * size, total);
        int to = Math.min(from + size, total);
        List<Painting> result = filtered.subList(from, to);

        Headers headers = exchange.getResponseHeaders();
        headers.set("X-Total-Count", String.valueOf(total));
        headers.set("X-Page", String.valueOf(page));
        headers.set("X-Page-Size", String.valueOf(size));
        sendJson(exchange, 200, paintingsJson(result));
    }

    private static void getPainting(HttpExchange exchange, int paintingId) throws IOException {
        Optional<Painting> painting = findPainting(paintingId);
        if (painting.isEmpty()) {
            sendJson(exchange, 200, errorJson("Painting not found"));
            return;
        }
        sendJson(exchange, 200, paintingJson(painting.get()));
    }

    private static void createPainting(HttpExchange exchange) throws IOException {
        Map<String, String> body = readJsonBody(exchange);
        String validation = validatePainting(body, true, null, false);
        if (validation != null) {
            sendJson(exchange, 400, errorJson(validation));
            return;
        }

        int id = NEXT_PAINTING_ID.getAndIncrement();
        Painting painting = paintingFrom(body, id, null);
        PAINTINGS.add(painting);
        exchange.getResponseHeaders().set("Location", "/api/paintings/" + id);
        sendJson(exchange, 201, paintingJson(painting));
    }

    private static void replacePainting(HttpExchange exchange, int id) throws IOException {
        Optional<Painting> existing = findPainting(id);
        if (existing.isEmpty()) {
            sendJson(exchange, 404, errorJson("Painting not found"));
            return;
        }

        Map<String, String> body = readJsonBody(exchange);
        String validation = validatePainting(body, true, existing.get(), true);
        if (validation != null) {
            sendJson(exchange, 400, errorJson(validation));
            return;
        }

        Painting updated = paintingFrom(body, id, existing.get());
        replacePaintingInList(updated);
        sendJson(exchange, 201, paintingJson(updated));
    }

    private static void patchPainting(HttpExchange exchange, int id) throws IOException {
        Optional<Painting> existing = findPainting(id);
        if (existing.isEmpty()) {
            sendJson(exchange, 404, errorJson("Painting not found"));
            return;
        }

        Map<String, String> body = readJsonBody(exchange);
        if (body.isEmpty()) {
            sendJson(exchange, 400, errorJson("Request body must contain at least one field"));
            return;
        }
        String validation = validatePainting(body, false, existing.get(), true);
        if (validation != null) {
            sendJson(exchange, 400, errorJson(validation));
            return;
        }

        Painting updated = paintingFrom(body, id, existing.get());
        replacePaintingInList(updated);
        sendJson(exchange, 200, paintingJson(updated));
    }

    private static void deletePainting(HttpExchange exchange, int id) throws IOException {
        Optional<Painting> existing = findPainting(id);
        if (existing.isEmpty()) {
            sendNoContent(exchange, 204);
            return;
        }
        PAINTINGS.removeIf(p -> p.id() == id);
        sendNoContent(exchange, 204);
    }

    private static Map<String, String> readJsonBody(HttpExchange exchange) throws IOException {
        String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
        if (contentType == null || !contentType.toLowerCase().startsWith("application/json")) {
            sendJson(exchange, 415, errorJson("Content-Type must be application/json"));
            throw new ResponseAlreadySentException();
        }
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        if (body.isBlank()) throw new BadJsonException();
        return parseFlatJson(body);
    }

    private static String validateArtist(Map<String, String> body, boolean full, Artist existing) {
        List<String> allowed = List.of("name", "birthYear", "deathYear", "country", "movement", "description", "portrait");
        for (String key : body.keySet()) {
            if (!allowed.contains(key)) return "Unknown field: " + key;
        }
        if (full) {
            for (String field : List.of("name", "birthYear", "country", "movement", "description", "portrait")) {
                if (!body.containsKey(field)) return "Missing required field: " + field;
            }
        }

        String name = value(body, "name", existing == null ? null : existing.name());
        Integer birthYear = integerValue(body, "birthYear", existing == null ? null : existing.birthYear());
        Integer deathYear = nullableIntegerValue(body, "deathYear", existing == null ? null : existing.deathYear());
        String country = value(body, "country", existing == null ? null : existing.country());
        String movement = value(body, "movement", existing == null ? null : existing.movement());
        String description = value(body, "description", existing == null ? null : existing.description());
        String portrait = value(body, "portrait", existing == null ? null : existing.portrait());

        if (isBlank(name)) return "name must not be blank";
        if (birthYear == null) return "birthYear must be an integer";
        if (birthYear < 1000 || birthYear > LocalDate.now().getYear()) return "birthYear is out of range";
        if (deathYear != null && deathYear < birthYear) return "deathYear must not be earlier than birthYear";
        if (isBlank(country)) return "country must not be blank";
        if (isBlank(movement)) return "movement must not be blank";
        if (isBlank(description)) return "description must not be blank";
        if (isBlank(portrait)) return "portrait must not be blank";

        return null;
    }

    private static String validatePainting(Map<String, String> body, boolean full, Painting existing, boolean validateArtistReference) {
        List<String> allowed = List.of("title", "artistId", "year", "genre", "museum", "description", "image");
        for (String key : body.keySet()) {
            if (!allowed.contains(key)) return "Unknown field: " + key;
        }
        if (full) {
            for (String field : allowed) {
                if (!body.containsKey(field)) return "Missing required field: " + field;
            }
        }

        String title = value(body, "title", existing == null ? null : existing.title());
        Integer artistId = integerValue(body, "artistId", existing == null ? null : existing.artistId());
        Integer year = integerValue(body, "year", existing == null ? null : existing.year());
        String genre = value(body, "genre", existing == null ? null : existing.genre());
        String museum = value(body, "museum", existing == null ? null : existing.museum());
        String description = value(body, "description", existing == null ? null : existing.description());
        String image = value(body, "image", existing == null ? null : existing.image());

        if (isBlank(title)) return "title must not be blank";
        if (artistId == null) return "artistId must be an integer";
        if (validateArtistReference && findArtist(artistId).isEmpty()) return "artistId does not reference an existing artist";
        if (year == null) return "year must be an integer";
        if (year < 1000 || year > LocalDate.now().getYear()) return "year is out of range";
        if (isBlank(genre)) return "genre must not be blank";
        if (isBlank(museum)) return "museum must not be blank";
        if (isBlank(description)) return "description must not be blank";
        if (isBlank(image)) return "image must not be blank";
        return null;
    }

    private static Artist artistFrom(Map<String, String> body, int id, Artist existing) {
        return new Artist(
                id,
                value(body, "name", existing == null ? null : existing.name()),
                integerValue(body, "birthYear", existing == null ? null : existing.birthYear()),
                nullableIntegerValue(body, "deathYear", existing == null ? null : existing.deathYear()),
                value(body, "country", existing == null ? null : existing.country()),
                value(body, "movement", existing == null ? null : existing.movement()),
                value(body, "description", existing == null ? null : existing.description()),
                value(body, "portrait", existing == null ? null : existing.portrait())
        );
    }

    private static Painting paintingFrom(Map<String, String> body, int id, Painting existing) {
        return new Painting(
                id,
                value(body, "title", existing == null ? null : existing.title()),
                integerValue(body, "artistId", existing == null ? null : existing.artistId()),
                integerValue(body, "year", existing == null ? null : existing.year()),
                value(body, "genre", existing == null ? null : existing.genre()),
                value(body, "museum", existing == null ? null : existing.museum()),
                value(body, "description", existing == null ? null : existing.description()),
                value(body, "image", existing == null ? null : existing.image())
        );
    }

    private static void replaceArtistInList(Artist updated) {
        for (int i = 0; i < ARTISTS.size(); i++) {
            if (ARTISTS.get(i).id() == updated.id()) {
                ARTISTS.set(i, updated);
                return;
            }
        }
    }

    private static void replacePaintingInList(Painting updated) {
        for (int i = 0; i < PAINTINGS.size(); i++) {
            if (PAINTINGS.get(i).id() == updated.id()) {
                PAINTINGS.set(i, updated);
                return;
            }
        }
    }

    private static Optional<Artist> findArtist(int id) {
        return ARTISTS.stream().filter(a -> a.id() == id).findFirst();
    }

    private static Optional<Painting> findPainting(int id) {
        return PAINTINGS.stream().filter(p -> p.id() == id).findFirst();
    }

    private static String value(Map<String, String> body, String key, String fallback) {
        return body.containsKey(key) ? body.get(key) : fallback;
    }

    private static Integer integerValue(Map<String, String> body, String key, Integer fallback) {
        if (!body.containsKey(key)) return fallback;
        return parseIntOrNull(body.get(key));
    }

    private static Integer nullableIntegerValue(Map<String, String> body, String key, Integer fallback) {
        if (!body.containsKey(key)) return fallback;
        String value = body.get(key);
        if (value == null || value.equals("null") || value.isBlank()) return null;
        return parseIntOrNull(value);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank() || value.equals("null");
    }

    private static void serveStatic(HttpExchange exchange, String path) throws IOException {
        addCommonHeaders(exchange);
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendText(exchange, 405, "Method not allowed", "text/plain; charset=utf-8");
            return;
        }

        if (path.equals("/")) path = "/index.html";
        if (path.equals("/artist")) path = "/artist.html";
        if (path.equals("/painting")) path = "/painting.html";
        if (path.equals("/api-docs")) path = "/api-docs.html";
        if (path.equals("/api-lab")) path = "/api-lab.html";

        String resourcePath = "/public" + path;
        try (InputStream input = Main.class.getResourceAsStream(resourcePath)) {
            if (input == null) {
                sendText(exchange, 404, "Page not found", "text/plain; charset=utf-8");
                return;
            }
            byte[] data = input.readAllBytes();
            Headers headers = exchange.getResponseHeaders();
            headers.set("Content-Type", contentType(path));
            headers.set("Cache-Control", "no-store");
            exchange.sendResponseHeaders(200, data.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(data);
            }
        }
    }

    private static void addCommonHeaders(HttpExchange exchange) {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Access-Control-Allow-Origin", "*");
        headers.set("X-Content-Type-Options", "nosniff");
        headers.set("Cache-Control", "no-store");
    }

    private static String contentType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=utf-8";
        if (path.endsWith(".css")) return "text/css; charset=utf-8";
        if (path.endsWith(".js")) return "application/javascript; charset=utf-8";
        if (path.endsWith(".svg")) return "image/svg+xml; charset=utf-8";
        return "application/octet-stream";
    }

    private static Map<String, String> parseQuery(URI uri) {
        Map<String, String> map = new LinkedHashMap<>();
        String query = uri.getRawQuery();
        if (query == null || query.isBlank()) return map;
        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            String key = decode(parts[0]);
            String value = parts.length > 1 ? decode(parts[1]) : "";
            map.put(key, value);
        }
        return map;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static Integer parseIntOrNull(String value) {
        if (value == null || value.isBlank() || value.equals("null")) return null;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static int positiveOrDefault(Integer value, int defaultValue) {
        return value == null || value <= 0 ? defaultValue : value;
    }

    private static Map<String, String> parseFlatJson(String json) {
        JsonCursor c = new JsonCursor(json);
        Map<String, String> result = new LinkedHashMap<>();
        c.skipWhitespace();
        c.expect('{');
        c.skipWhitespace();
        if (c.peek('}')) {
            c.expect('}');
            c.skipWhitespace();
            c.expectEnd();
            return result;
        }

        while (true) {
            c.skipWhitespace();
            String key = c.readString();
            c.skipWhitespace();
            c.expect(':');
            c.skipWhitespace();
            String value;
            if (c.peek('"')) {
                value = c.readString();
            } else if (c.startsWith("null")) {
                c.advance(4);
                value = "null";
            } else {
                value = c.readScalar();
            }
            result.put(key, value);
            c.skipWhitespace();
            if (c.peek(',')) {
                c.expect(',');
                continue;
            }
            c.expect('}');
            c.skipWhitespace();
            c.expectEnd();
            return result;
        }
    }

    private static final class JsonCursor {
        private final String text;
        private int pos;

        private JsonCursor(String text) { this.text = text; }
        private void skipWhitespace() { while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) pos++; }
        private boolean peek(char ch) { return pos < text.length() && text.charAt(pos) == ch; }
        private boolean startsWith(String value) { return text.startsWith(value, pos); }
        private void advance(int count) { pos += count; }
        private void expect(char ch) { if (!peek(ch)) throw new BadJsonException(); pos++; }
        private void expectEnd() { if (pos != text.length()) throw new BadJsonException(); }

        private String readString() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (pos < text.length()) {
                char ch = text.charAt(pos++);
                if (ch == '"') return sb.toString();
                if (ch == '\\') {
                    if (pos >= text.length()) throw new BadJsonException();
                    char e = text.charAt(pos++);
                    switch (e) {
                        case '"' -> sb.append('"');
                        case '\\' -> sb.append('\\');
                        case '/' -> sb.append('/');
                        case 'b' -> sb.append('\b');
                        case 'f' -> sb.append('\f');
                        case 'n' -> sb.append('\n');
                        case 'r' -> sb.append('\r');
                        case 't' -> sb.append('\t');
                        case 'u' -> {
                            if (pos + 4 > text.length()) throw new BadJsonException();
                            try {
                                sb.append((char) Integer.parseInt(text.substring(pos, pos + 4), 16));
                            } catch (NumberFormatException ex) {
                                throw new BadJsonException();
                            }
                            pos += 4;
                        }
                        default -> throw new BadJsonException();
                    }
                } else {
                    sb.append(ch);
                }
            }
            throw new BadJsonException();
        }

        private String readScalar() {
            int start = pos;
            while (pos < text.length() && text.charAt(pos) != ',' && text.charAt(pos) != '}') pos++;
            String scalar = text.substring(start, pos).trim();
            if (scalar.isEmpty()) throw new BadJsonException();
            if (scalar.equals("true") || scalar.equals("false") || scalar.matches("-?\\d+")) return scalar;
            throw new BadJsonException();
        }
    }

    private static final class BadJsonException extends RuntimeException {}
    private static final class ResponseAlreadySentException extends RuntimeException {}

    private static String artistsJson(List<Artist> artists) {
        return artists.stream().map(Main::artistJson).collect(Collectors.joining(",", "[", "]"));
    }

    private static String paintingsJson(List<Painting> paintings) {
        return paintings.stream().map(Main::paintingJson).collect(Collectors.joining(",", "[", "]"));
    }

    private static String artistJson(Artist a) {
        return "{" +
                "\"id\":" + a.id() + "," +
                "\"name\":" + q(a.name()) + "," +
                "\"birthYear\":" + a.birthYear() + "," +
                "\"deathYear\":" + (a.deathYear() == null ? "null" : a.deathYear()) + "," +
                "\"country\":" + q(a.country()) + "," +
                "\"movement\":" + q(a.movement()) + "," +
                "\"description\":" + q(a.description()) + "," +
                "\"portrait\":" + q(a.portrait()) +
                "}";
    }

    private static String paintingJson(Painting p) {
        String artistName = ARTISTS.stream().filter(a -> a.id() == p.artistId()).map(Artist::name).findFirst().orElse("Unknown");
        return "{" +
                "\"id\":" + p.id() + "," +
                "\"title\":" + q(p.title()) + "," +
                "\"artistId\":" + p.artistId() + "," +
                "\"artistName\":" + q(artistName) + "," +
                "\"year\":" + p.year() + "," +
                "\"genre\":" + q(p.genre()) + "," +
                "\"museum\":" + q(p.museum()) + "," +
                "\"description\":" + q(p.description()) + "," +
                "\"image\":" + q(p.image()) +
                "}";
    }

    private static String errorJson(String message) {
        return "{\"error\":" + q(message) + "}";
    }

    private static String q(String value) {
        if (value == null) return "null";
        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r") + "\"";
    }

    private static void methodNotAllowed(HttpExchange exchange, String allow) throws IOException {
        exchange.getResponseHeaders().set("Allow", allow);
        sendJson(exchange, 405, errorJson("Method not allowed"));
    }

    private static void sendJson(HttpExchange exchange, int status, String body) throws IOException {
        sendText(exchange, status, body, "application/json; charset=utf-8");
    }

    private static void sendNoContent(HttpExchange exchange, int status) throws IOException {
        exchange.sendResponseHeaders(status, -1);
        exchange.close();
    }

    private static void sendText(HttpExchange exchange, int status, String body, String contentType) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}

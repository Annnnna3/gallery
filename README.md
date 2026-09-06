# Art Gallery QA

Небольшой pet-project для практики **manual QA** на локальном веб-приложении.

## Что внутри

- каталог 6 художников;
- 19 картин;
- страницы художника и картины;
- поиск, фильтрация, сортировка и пагинация;
- REST API;
- `GET`, `POST`, `PUT`, `PATCH`, `DELETE`, `OPTIONS`;
- разные HTTP status codes: `200`, `201`, `204`, `400`, `404`, `405`, `409`, `415`;
- заголовки `Location`, `Allow`, `X-Total-Count`, `X-Page`, `X-Page-Size`;
- интерактивный API Lab прямо в браузере;
- Postman collection;
- API requirements для составления тест-кейсов;
- намеренно заложенные дефекты для поиска тестировщиком.

Проект написан на **Java 21** без внешних библиотек. Maven, Gradle, Node.js и база данных не нужны.

## Быстрый запуск

```bash
java --add-modules jdk.httpserver -jar art-gallery-qa.jar
```

Открыть сайт:

```text
http://localhost:8080
```

API docs:

```text
http://localhost:8080/api-docs
```

API Lab:

```text
http://localhost:8080/api-lab
```

Остановка:

```text
Ctrl + C
```

## Важно про данные

Проект использует in-memory данные.

`POST`, `PUT`, `PATCH`, `DELETE` реально меняют состояние приложения, но только до перезапуска.

Чтобы восстановить исходные данные:

```text
остановить приложение → запустить снова
```

Это удобно для повторного прохождения тестов.

## Запуск из исходников

macOS / Linux:

```bash
./build.sh
./run.sh
```

Windows:

```bat
build.bat
run.bat
```

## Docker

```bash
docker build -t art-gallery-qa .
docker run --rm -p 8080:8080 art-gallery-qa
```

## API методы

Artists:

```http
GET    /api/artists
GET    /api/artists/{id}
GET    /api/artists/{id}/paintings
POST   /api/artists
PUT    /api/artists/{id}
PATCH  /api/artists/{id}
DELETE /api/artists/{id}
```

Paintings:

```http
GET    /api/paintings
GET    /api/paintings/{id}
POST   /api/paintings
PUT    /api/paintings/{id}
PATCH  /api/paintings/{id}
DELETE /api/paintings/{id}
```

Other:

```http
GET     /api/health
OPTIONS /api/paintings
```

## Что тестировать

1. UI smoke.
2. Навигацию.
3. Поиск и регистр.
4. Фильтры.
5. Сортировку.
6. Пагинацию.
7. GET API.
8. POST создание ресурса.
9. PUT полное обновление.
10. PATCH частичное обновление.
11. DELETE.
12. Повторный GET после изменения/удаления.
13. Positive / negative API cases.
14. Некорректный JSON.
15. Пустые/отсутствующие поля.
16. Невалидные ID.
17. Несуществующие ресурсы.
18. Неверный `Content-Type`.
19. Неподдерживаемые методы и `Allow`.
20. Status codes и response headers.
21. Согласованность UI ↔ API.
22. Согласованность связанных сущностей artist ↔ paintings.

## QA материалы

```text
qa/
├── API_REQUIREMENTS.md
├── TEST_MISSION.md
├── postman/
│   └── Art-Gallery-QA.postman_collection.json
├── http/
│   └── ArtGallery.http
└── answer-key/
    └── KNOWN_BUGS.md
```

Начни с:

```text
qa/API_REQUIREMENTS.md
qa/TEST_MISSION.md
```

`qa/http/ArtGallery.http` можно запускать прямо из IntelliJ IDEA: нажимай ▶ слева от нужного HTTP-запроса.

`qa/answer-key/KNOWN_BUGS.md` — ответ. Не открывай его до конца тестирования.

## Git

```bash
git init
git add .
git commit -m "Initial Art Gallery QA project"
git branch -M main
```

После этого создай пустой repository на GitHub и выполни команды `git remote add origin ...` и `git push -u origin main`, которые покажет GitHub.

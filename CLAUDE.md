# Project context for Claude / AI assistants

E-commerce streetwear shop. Spring Boot 3 + Thymeleaf SSR + MySQL 8 + Flyway.

## Stack

- **Java 17, Spring Boot 3.3.4** — web, security, data-jpa, validation, devtools
- **Hibernate 6** — JPA mapping; `ddl-auto=none`, schema do Flyway quản
- **MySQL 8** *(qua Docker)* hoặc MariaDB *(XAMPP local)*
- **Thymeleaf** — server-side rendering, không có SPA
- **Spring Security 6** — dual filter chain: admin (`/admin/**`) và user, session độc lập trong cùng browser
- **Flyway 10** — migrations ở `backend/src/main/resources/db/migration/V*.sql`
- **spring-dotenv** — auto-load `.env` từ CWD lúc startup
- **JUnit 5 + Mockito** — unit tests

## Layout

```
backend/                         Spring Boot app
├── src/main/java/com/shop/
│   ├── BadHabitsApplication.java   entry point
│   ├── config/                     WebConfig, DataInitializer (seed admin + categories + products)
│   ├── controller/                 HTTP handlers (admin/* riêng cho admin panel)
│   ├── service/  + impl/           ProductService và OrderService có interface + impl tách
│   ├── repository/                 JpaRepository interfaces
│   ├── entity/                     User, Category, Product, OrderDetail, Order, Cart, CartItem, Color, Role, OrderStatus
│   ├── security/                   SecurityConfig + AdminAuth/UserAuth providers
│   └── dto/ exception/ util/       (rỗng — placeholder)
├── src/main/resources/
│   ├── application.properties      đọc env vars qua ${VAR:-default}
│   ├── db/migration/V1__init_schema.sql   Flyway migration đầu tiên
│   ├── templates/                  Thymeleaf (pages/, admin/, fragments/, layouts/)
│   └── static/                     css, js, images/products/*.webp (10 ảnh local)
├── src/test/java/                  JUnit + Mockito (ProductServiceImplTest có 13 cases)
└── .env                            local dev creds (ADMIN_EMAIL, ADMIN_PASSWORD)

docker/
├── Dockerfile                      multi-stage maven → JRE
├── docker-compose.yml              mysql:8 + app + phpmyadmin
└── .env                            DB_ROOT_PASSWORD, ADMIN_*

database/                           README only (migrations live in backend/)
uploads/                            ảnh upload runtime (gitignored content, mounted vào /uploads in container)
```

## Conventions

- Package: `com.shop` *(không phải `com.badhabits` — đã rename)*
- Entities: dùng accessor record-style `name()`, `slug()`, ... thay vì `getName()` để Thymeleaf gọi `${product.name}` hay `${product.name()}` đều OK. *(Không bỏ `getId()` vì Spring binding cần.)*
- DataInitializer chỉ seed khi DB rỗng → không ghi đè data user thêm.
- Admin password đọc từ `.env` hoặc env var `ADMIN_PASSWORD`. Không hardcode.
- Mỗi lần đổi entity → tạo migration `V<n>__*.sql` mới. **Không sửa file V cũ.**
- Docker rebuild khi đổi Java code: `docker compose up -d --build` *(thiếu `--build` = dùng image cũ)*.

## URLs

- Storefront: http://localhost:8080
- Admin: http://localhost:8080/admin/login *(`admin@bh.vn` / `admin123` mặc định, đổi qua `.env`)*
- phpMyAdmin: http://localhost:8081 *(auto-login khi dùng Docker)*

## Tài liệu thêm

- [`README.md`](README.md) — hướng dẫn chạy
- [`ARCHITECTURE.md`](ARCHITECTURE.md) — kiến trúc + flow
- [`docker/README.md`](docker/README.md) — Docker workflow
- [`database/README.md`](database/README.md) — Flyway migration

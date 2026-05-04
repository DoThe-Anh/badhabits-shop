# 🏛️ KIẾN TRÚC DỰ ÁN — BAD HABITS STORE

Dự án dùng layout chuẩn Spring Boot: **`backend/`** chứa toàn bộ ứng dụng
(Java + Thymeleaf templates + static assets), các thư mục root khác chứa
artifact ngoài runtime (DB scripts, docs, prompts, docker, uploads).

> **Note:** package Java hiện tại là `com.badhabits` (tên project gốc), không
> phải `com.shop` như diagram trong README.md. Nếu muốn đổi, đó là refactor
> riêng đụng mọi file Java + import.

---

## 📂 Cây thư mục

```
test_24_04_2026/
│
├── backend/                                  🟦 Spring Boot app (toàn bộ)
│   ├── src/main/java/com/badhabits/
│   │   ├── BadHabitsApplication.java          ← entry @SpringBootApplication
│   │   ├── config/                            ← MVC, init data, model attrs
│   │   ├── controller/                        ← HTTP handlers
│   │   │   ├── HomeController.java
│   │   │   ├── ProductController.java
│   │   │   ├── CategoryController.java
│   │   │   ├── CartController.java
│   │   │   ├── CheckoutController.java
│   │   │   ├── AccountController.java
│   │   │   ├── AuthController.java
│   │   │   └── admin/                         ← admin-only endpoints
│   │   ├── service/                           ← business logic
│   │   ├── repository/                        ← Spring Data JPA
│   │   ├── model/                             ← JPA entities + enums
│   │   ├── security/                          ← Spring Security config
│   │   ├── dto/                               ← (rỗng — thêm khi cần)
│   │   ├── exception/                         ← (rỗng — thêm khi cần)
│   │   └── util/                              ← (rỗng — thêm khi cần)
│   │
│   ├── src/main/resources/
│   │   ├── application.properties             ← Spring config
│   │   ├── templates/                         ← Thymeleaf (classpath:/templates/)
│   │   │   ├── layouts/main.html
│   │   │   ├── fragments/                     ← header, footer, cart-drawer…
│   │   │   ├── pages/                         ← home, login, product-detail…
│   │   │   └── admin/                         ← dashboard, orders, products…
│   │   └── static/                            ← (classpath:/static/)
│   │       ├── css/main.css
│   │       ├── js/main.js
│   │       ├── images/products/
│   │       └── modules/auth/                  ← JS module sandbox (auth.*.js + login.view.html)
│   │
│   ├── src/test/java/                         ← (chưa có test)
│   ├── data/                                  ← H2 file DB (gitignored)
│   ├── target/                                ← Maven output (gitignored)
│   ├── pom.xml                                ← Maven config — layout chuẩn, không custom path
│   ├── mvnw   mvnw.cmd   .mvn/                ← Maven wrapper
│
├── database/                                  ← Flyway migration docs (SQL ở backend/.../db/migration/)
├── uploads/                                   ← ảnh sản phẩm runtime (gitignored content)
├── docker/                                    ← Dockerfile, docker-compose.yml, phpmyadmin
│
├── .gitignore  .dockerignore
├── ARCHITECTURE.md                            ← file này
├── CLAUDE.md                                  ← context cho AI assistant
└── README.md                                  ← diagram tổng quan
```

---

## 🔄 Luồng request

```
Browser ── GET /product/saboteur-boxy-tee ──▶ Spring Boot
                                                │
   Controller ─▶ Service ─▶ Repository ─▶ Model (JPA entity)
                                                │
   Controller trả tên view: "pages/product-detail" + Model
                                                │
   Thymeleaf resolver tìm classpath:/templates/pages/product-detail.html
   + fragments/header, footer, cart-drawer, product-card
                                                │
                                                ▼
                                  HTML response (+ /css, /js từ /static/)
```

---

## 🛠️ Pom.xml

`backend/pom.xml` dùng **layout Maven chuẩn** — không khai báo `<sourceDirectory>`
hay `<resources>` custom. Maven tự tìm:

- Java: `backend/src/main/java/`
- Resources: `backend/src/main/resources/` (gồm `templates/` và `static/`)
- Test: `backend/src/test/java/`

Khi `mvn package`, mọi thứ trong `src/main/resources/` được copy thẳng vào
`target/classes/`, đúng convention Spring Boot.

---

## ⚡ Dev workflow

### Chạy thử

```bash
cd backend
./mvnw spring-boot:run
# → http://localhost:8080
```

Hoặc trong IDE: click phải `BadHabitsApplication.java` → Run.

### Live-reload khi edit

`spring.thymeleaf.cache=false` + `spring-boot-devtools` được khai báo trong pom.

- Sửa `.html` trong `src/main/resources/templates/` → IDE auto-copy vào `target/classes/templates/`, devtools restart, F5 thấy ngay.
- Sửa `.css/.js` trong `src/main/resources/static/` → tương tự.
- Sửa `.java` → devtools auto-restart (vài giây).

> Khác với layout cũ (đọc trực tiếp `file:./frontend/...` không cần rebuild),
> giờ đã chuyển sang classpath chuẩn — phải qua devtools/IDE classpath sync.
> Tradeoff đáng để theo Spring Boot convention và đóng jar được không cần đổi config.

### Build sản phẩm (jar)

```bash
cd backend
./mvnw clean package
java -jar target/badhabits-store-1.0.0.jar
```

---

## 🎯 Nguyên tắc khi thêm code mới

| Nội dung | Đặt ở đâu |
|----------|-----------|
| API endpoint / form handler | `backend/src/main/java/com/badhabits/controller/` |
| Logic nghiệp vụ | `…/service/` |
| Truy vấn DB | `…/repository/` |
| JPA entity | `…/model/` |
| DTO request/response | `…/dto/` |
| Custom exception | `…/exception/` |
| Helper / utility | `…/util/` |
| Page mới (Thymeleaf) | `backend/src/main/resources/templates/pages/` + controller |
| Reusable HTML | `…/templates/fragments/` |
| Style | `…/static/css/main.css` |
| JS tương tác | `…/static/js/main.js` |
| Ảnh / font / icon | `…/static/images/`, `…/static/fonts/` |
| Migration SQL mới | `backend/src/main/resources/db/migration/V<n>__*.sql` |
| Ảnh upload runtime | `uploads/` |

---

## 🚀 Nếu sau này tách thành 2 repo (SPA + REST API)

Diagram trong README.md đã chừa sẵn `frontend/` cho React:

1. Biến `@Controller` thành `@RestController` → trả JSON.
2. Tạo project React tại `frontend/` (src/, components/, pages/, services/).
3. Xoá `templates/` ở backend, deploy độc lập.

Hiện tại SSR Thymeleaf vẫn nhanh hơn và SEO-friendly cho giai đoạn này.

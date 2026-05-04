# 🛍️ Bad Habits Shop

> E-commerce streetwear store với storefront + admin panel quản lý sản phẩm, danh mục và đơn hàng.

Xây dựng bằng **Spring Boot 3 + Thymeleaf SSR + MySQL 8**, đóng gói qua Docker, có Flyway migration và dual-session security (admin/user độc lập trong cùng browser).

---

## 📑 Mục lục

- [Tính năng](#-tính-năng)
- [Tech stack](#-tech-stack)
- [Yêu cầu hệ thống](#-yêu-cầu-hệ-thống)
- [Quick start](#-quick-start)
- [Cấu hình](#-cấu-hình)
- [Workflow](#-workflow)
- [Lệnh hay dùng](#-lệnh-hay-dùng)
- [Cấu trúc thư mục](#-cấu-trúc-thư-mục)
- [Testing](#-testing)
- [Troubleshooting](#-troubleshooting)
- [Tài liệu thêm](#-tài-liệu-thêm)
- [Tác giả](#-tác-giả)

---

## ✨ Tính năng

**Storefront (user)**
- Duyệt sản phẩm theo danh mục, lọc theo giá/màu/size
- Giỏ hàng + checkout với địa chỉ giao hàng
- Đăng ký / đăng nhập, quản lý đơn hàng cá nhân

**Admin panel** (`/admin`)
- CRUD sản phẩm (upload ảnh / paste URL), danh mục, user
- Quản lý đơn hàng + cập nhật trạng thái (`PENDING` → `SHIPPED` → `DELIVERED`)
- Session admin tách biệt với session user

---

## 🧰 Tech stack

| Layer | Công nghệ |
|---|---|
| **Language** | Java 17 |
| **Framework** | Spring Boot 3.3.4 (web, security, data-jpa, validation) |
| **ORM** | Hibernate 6 + JPA *(`ddl-auto=none`, schema do Flyway quản)* |
| **Database** | MySQL 8 *(Docker)* hoặc MariaDB *(XAMPP local)* |
| **View** | Thymeleaf *(server-side rendering, không SPA)* |
| **Security** | Spring Security 6 — dual filter chain |
| **Migration** | Flyway 10 |
| **Config** | spring-dotenv *(auto-load `.env`)* |
| **Test** | JUnit 5 + Mockito |
| **Container** | Docker + docker-compose |

---

## 📋 Yêu cầu hệ thống

- **Docker Desktop** *(khuyến nghị)*, hoặc
- **Java 17 + Maven 3.9+ + MySQL 8/MariaDB** *(chạy local)*

---

## 🚀 Quick start

### Cách 1 — Docker *(khuyến nghị)*

```bash
cd docker
docker compose up -d --build
```

Lần đầu mất ~3–5 phút. Lần sau ~30s. Khi log hiện `Started BadHabitsApplication` là OK.

### Cách 2 — Chạy local *(không Docker)*

1. Bật MySQL trong XAMPP *(hoặc cài MySQL standalone)*
2. Tạo file `backend/.env` *(xem [Cấu hình](#-cấu-hình))*
3. Chạy:

```bash
cd backend
./mvnw spring-boot:run        # Linux/Mac
.\mvnw.cmd spring-boot:run    # Windows PowerShell
```

### Truy cập

| URL | Mô tả |
|---|---|
| http://localhost:8080 | 🛍️ Storefront |
| http://localhost:8080/admin/login | 🔐 Admin panel — `admin@bh.vn` / `admin123` |
| http://localhost:8081 | 🗄️ phpMyAdmin *(auto-login khi dùng Docker)* |

---

## ⚙️ Cấu hình

Biến môi trường đọc từ `.env` ở `backend/` *(local)* hoặc `docker/` *(Docker)*. Mặc định trong `application.properties` qua syntax `${VAR:-default}`.

| Biến | Mặc định | Mô tả |
|---|---|---|
| `DB_HOST` | `localhost` | MySQL host |
| `DB_PORT` | `3306` | MySQL port |
| `DB_NAME` | `badhabits_shop` | Database name |
| `DB_USERNAME` | `root` | DB user |
| `DB_PASSWORD` | *(empty)* | DB password |
| `ADMIN_EMAIL` | `admin@bh.vn` | Admin login email |
| `ADMIN_PASSWORD` | `admin123` | Admin login password |
| `SERVER_PORT` | `8080` | App port |

> ⚠️ **Không commit `.env`** — đã được `.gitignore` chặn. Dùng `.env.example` làm template *(nếu có)*.

---

## 💼 Workflow

| Việc | Đường |
|---|---|
| Thêm sản phẩm | `/admin/products/new` — upload ảnh hoặc paste URL, nhập sizes/màu/giá |
| Thêm danh mục | `/admin/categories/new` |
| Sửa giá / stock nhanh | phpMyAdmin → table `products` → Edit |
| Reset DB *(seed lại)* | `docker compose down -v && docker compose up -d --build` |
| Đổi schema | Tạo migration mới `backend/src/main/resources/db/migration/V<n>__*.sql` *(không sửa file V cũ)* |

---

## 🛠️ Lệnh hay dùng

**Docker**

```bash
docker compose up -d --build       # rebuild + start (sau khi đổi Java code)
docker compose logs -f app         # follow log app
docker compose restart app         # restart không build
docker compose down                # stop, giữ data
docker compose down -v             # stop + reset DB
```

**Maven (Windows PowerShell)**

```bash
.\mvnw.cmd spring-boot:run         # chạy app
.\mvnw.cmd test                    # chạy unit tests
.\mvnw.cmd clean package           # build .jar
```

---

## 📂 Cấu trúc thư mục

```
badhabits-shop/
├── backend/         Spring Boot app (Java + templates + static)
│   ├── src/main/java/com/shop/    config, controller, service, repository, entity, security
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   ├── db/migration/          Flyway SQL migrations
│   │   ├── templates/             Thymeleaf views
│   │   └── static/                CSS, JS, images
│   └── src/test/java/             JUnit + Mockito tests
├── docker/          Dockerfile + docker-compose (mysql + app + phpmyadmin)
├── database/        Migration docs (file SQL ở backend/)
├── uploads/         Ảnh sản phẩm runtime (gitignored)
├── ARCHITECTURE.md  Kiến trúc + flow chi tiết
└── CLAUDE.md        Context cho AI assistants
```

Chi tiết: [`ARCHITECTURE.md`](ARCHITECTURE.md).

---

## 🧪 Testing

```bash
cd backend
.\mvnw.cmd test
```

Unit tests dùng JUnit 5 + Mockito. `ProductServiceImplTest` có 13 test cases làm template tham khảo.

---

## 🐛 Troubleshooting

| Lỗi | Fix |
|---|---|
| `Bind for 0.0.0.0:3306 failed: port already allocated` | XAMPP MySQL đang chạy → tắt, hoặc đổi port trong `docker-compose.yml` |
| App chạy code cũ sau khi sửa Java | `docker compose up -d --build` *(thiếu `--build` là dùng image cũ)* |
| Whitelabel 500 / Bad credentials | `docker compose logs app --tail 100` xem stacktrace |
| MySQL container restart loop | `docker compose down -v && docker compose up -d --build` |
| Flyway checksum mismatch | Không sửa migration đã apply — tạo `V<n+1>__fix.sql` mới |

Chi tiết: [`docker/README.md`](docker/README.md).

---

## 📚 Tài liệu thêm

- [`ARCHITECTURE.md`](ARCHITECTURE.md) — kiến trúc + luồng request
- [`docker/README.md`](docker/README.md) — Docker workflow
- [`database/README.md`](database/README.md) — Flyway migration guide
- [`CLAUDE.md`](CLAUDE.md) — context cho AI assistants

---

## 👤 Tác giả

**DoThe-Anh** — [GitHub](https://github.com/DoThe-Anh)

Repo: [badhabits-shop](https://github.com/DoThe-Anh/badhabits-shop)

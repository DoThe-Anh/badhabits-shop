# Shop — Spring Boot + Thymeleaf + MySQL

E-commerce streetwear store: storefront + admin panel quản lý sản phẩm/danh mục/đơn hàng. Java 17, Spring Boot 3, Hibernate + JPA, Flyway migrations, đóng gói Docker.

---

## 🚀 Chạy thử

**Yêu cầu:** Docker Desktop *(hoặc XAMPP nếu chạy local)*.

```bash
cd docker
docker compose up -d --build
```

Lần đầu mất ~3–5 phút. Lần sau ~30s. Khi log hiện `Started BadHabitsApplication` là OK.

| URL | |
|---|---|
| http://localhost:8080 | 🛍️ Storefront |
| http://localhost:8080/admin/login | 🔐 Admin — `admin@bh.vn` / `admin123` |
| http://localhost:8081 | 🗄️ phpMyAdmin *(tự auto-login)* |

> Chạy local không Docker? Bật MySQL trong XAMPP, sau đó: `cd backend && .\mvnw.cmd spring-boot:run`. Sửa `backend/.env` nếu muốn đổi creds.

---

## 💼 Workflow

| Việc | Đường |
|---|---|
| Thêm sản phẩm | `/admin/products/new` — upload ảnh hoặc paste URL, nhập sizes/màu/giá |
| Thêm danh mục | `/admin/categories/new` |
| Sửa giá / stock nhanh | phpMyAdmin → table `products` → Edit |
| Reset DB *(seed lại)* | `docker compose down -v && docker compose up -d --build` |
| Đổi schema | Tạo `backend/src/main/resources/db/migration/V<n>__*.sql` *(không sửa file V cũ)* |

---

## 🛠️ Lệnh hay dùng

```bash
docker compose up -d --build       # rebuild + start (sau khi đổi Java code)
docker compose logs -f app         # follow log app
docker compose restart app         # restart không build
docker compose down                # stop, giữ data
docker compose down -v             # stop + reset DB

# Local Maven (Windows PowerShell)
.\mvnw.cmd spring-boot:run         # chạy app
.\mvnw.cmd test                    # 13 unit tests
```

---

## 📂 Cấu trúc

```
backend/      ← Spring Boot app (Java + templates + static)
docker/       ← Dockerfile + compose (mysql + app + phpmyadmin)
database/     ← Migration docs (file SQL ở backend/src/main/resources/db/migration/)
uploads/      ← Ảnh sản phẩm runtime (gitignored)
```

Chi tiết: [`ARCHITECTURE.md`](ARCHITECTURE.md).

---

## 🐛 Lỗi hay gặp

| Lỗi | Fix |
|---|---|
| `Bind for 0.0.0.0:3306 failed: port already allocated` | XAMPP MySQL đang chạy → tắt, hoặc đổi port trong `docker-compose.yml` |
| App chạy code cũ sau khi sửa Java | `docker compose up -d --build` *(thiếu `--build` là dùng image cũ)* |
| Whitelabel 500 / Bad credentials | `docker compose logs app --tail 100` xem stacktrace |
| MySQL container restart loop | `docker compose down -v && docker compose up -d --build` |

Chi tiết troubleshooting: [`docker/README.md`](docker/README.md).

---

## 📚 Tài liệu thêm

- [`ARCHITECTURE.md`](ARCHITECTURE.md) — kiến trúc + luồng request
- [`docker/README.md`](docker/README.md) — Docker workflow
- [`database/README.md`](database/README.md) — Flyway migration guide

# docker/

Setup Docker cho local dev — không cần XAMPP, không cần cài Maven/Java trên máy.

## Yêu cầu

- **Docker Desktop** đang chạy *(Windows / macOS)*
- Hoặc **Docker Engine + docker-compose-plugin** *(Linux)*

## Khởi động

```bash
cd docker
docker compose up --build
```

Lần đầu mất ~3-5 phút *(pull MySQL image, build Maven cache, compile Java, package jar)*. Lần sau ~30 giây *(layer cache)*.

Khi log hiện `Started BadHabitsApplication in X.X seconds` → app sẵn sàng:
- App: http://localhost:8080
- Admin: http://localhost:8080/admin/login → `admin@bh.vn` / `admin123`
- MySQL trên host: `localhost:3306`, user `root`, password `root` *(xem `.env`)*

## Lệnh hay dùng

| Lệnh | Tác dụng |
|---|---|
| `docker compose up` | Start (image đã build sẵn) |
| `docker compose up --build` | Rebuild image rồi start *(sau khi đổi Java code)* |
| `docker compose up -d` | Chạy nền (detached) |
| `docker compose logs -f app` | Follow log của app |
| `docker compose logs -f mysql` | Follow log MySQL |
| `docker compose down` | Stop containers, **giữ data** |
| `docker compose down -v` | Stop + **xoá volume MySQL** *(reset DB hoàn toàn)* |
| `docker compose exec mysql mysql -uroot -proot shop` | Vào MySQL CLI |
| `docker compose ps` | Xem trạng thái containers |

## Cấu trúc

```
docker/
├── Dockerfile           # Multi-stage build: maven → JRE
├── docker-compose.yml   # 2 service: mysql + app
├── .env                 # Creds (gitignored)
└── README.md            # File này
```

## Volume

- **`mysql-data`** *(named volume)*: data MySQL, persist giữa các lần restart
- **`../uploads:/uploads`** *(bind mount)*: ảnh upload từ admin → ghi thẳng vào `uploads/` ở project root, không mất khi rebuild image

## Khi nào dùng Docker vs XAMPP

| Tình huống | Khuyến nghị |
|---|---|
| Bạn quen XAMPP, đang dev nhanh | **XAMPP** *(simpler)* |
| Muốn DB/app cô lập, deploy được | **Docker** |
| Onboard máy mới, không cài XAMPP | **Docker** *(1 lệnh)* |
| Demo cho người khác | **Docker** *(reproducible)* |

Hai setup **không xung đột** — XAMPP MySQL cũng dùng port 3306; Docker MySQL cũng port 3306. **Chỉ chạy 1 trong 2 cùng lúc.**

## Troubleshooting

### `Bind for 0.0.0.0:3306 failed: port is already allocated`
→ XAMPP MySQL đang chạy. Tắt XAMPP MySQL trước, hoặc đổi port trong `docker-compose.yml`:
```yaml
ports:
  - "3307:3306"   # Host 3307 → container 3306
```

### App khởi động trước MySQL ready → connection refused
→ Đã có healthcheck + `depends_on.condition: service_healthy`. Nếu vẫn lỗi, đợi vài giây rồi `docker compose restart app`.

### Đổi Java code mà container không thấy thay đổi
→ Phải `docker compose up --build` để rebuild image. Hot-reload không có trong production-style image này.

### Reset toàn bộ
```bash
docker compose down -v       # xoá volume MySQL
docker compose up --build    # rebuild + chạy lại từ đầu
```
DataInitializer sẽ seed admin + 4 categories + 28 products lại.

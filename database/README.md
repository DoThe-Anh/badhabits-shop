# database/

Thư mục này dành cho artifact DB **không phải** migration scripts.

## Migrations sống ở đâu?

Flyway migrations nằm trong codebase Spring Boot:
**[`backend/src/main/resources/db/migration/`](../backend/src/main/resources/db/migration/)**

- `V1__init_schema.sql` — schema gốc (8 bảng)
- `V2__*.sql`, `V3__*.sql` — các migration tiếp theo (chưa có)

Lý do: Flyway auto-load từ classpath:db/migration/ khi app khởi động → migrations đi cùng jar khi deploy. Đặt ở root `database/` thì Flyway phải config thêm path, dễ lệch giữa dev và prod.

## Khi nào cần thêm migration?

Khi **đổi entity** (thêm field, đổi type, thêm bảng mới…):

1. Tạo file mới `backend/src/main/resources/db/migration/V<n>__<mô_tả>.sql`
   *(số `<n>` tăng dần, không gap, không sửa file đã chạy)*
2. Viết SQL `ALTER TABLE …` hoặc `CREATE TABLE …`
3. Restart app → Flyway tự chạy migration mới, ghi nhận vào bảng `flyway_schema_history`

**Đừng sửa file V đã chạy.** Flyway dùng checksum để phát hiện thay đổi → app sẽ refuse start với error "Migration checksum mismatch".

## Folder này dùng cho gì?

- `schema.sql` (tuỳ chọn) — snapshot đầy đủ schema export từ `mysqldump`, để xem nhanh.
- `seed.sql` (tuỳ chọn) — seed data ngoài Flyway (vd. dump production data đã anonymize cho dev).
- `diagram.png` (tuỳ chọn) — ER diagram, schema visualisation.

Tất cả những thứ trên là **tham khảo**, không phải nguồn schema chính. Nguồn duy nhất là Flyway migrations.

## Reset DB local

```sql
-- phpMyAdmin
DROP DATABASE shop;
```

Restart app → Flyway tạo lại từ V1.

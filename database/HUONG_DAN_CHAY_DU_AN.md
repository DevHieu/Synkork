# Hướng dẫn chạy dự án Synkork & Kiểm tra chức năng Calendar

## 1. Yêu cầu hệ thống

| Công cụ | Phiên bản |
|---------|-----------|
| Java JDK | 21+ |
| Node.js | 18+ |
| MySQL | 8.0+ |
| Maven | 3.9+ (hoặc dùng `mvnw` có sẵn) |

---

## 2. Cài đặt Database

1. Mở MySQL và tạo database bằng file SQL có sẵn:

```sql
-- Chạy file SynkorkDB_query.sql trong thư mục database/
source d:/LearningInFPT/DuAnTotNgiep/Synkork/database/SynkorkDB_query.sql;
```

2. Kiểm tra kết nối mặc định trong `backend/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/synkork
    username: root
    password: 123
```

> **Lưu ý:** Nếu MySQL của bạn dùng username/password khác, hãy sửa lại trong file `application.yml` hoặc set biến môi trường `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.

---

## 3. Chạy Backend

```bash
cd backend
./mvnw spring-boot:run
```

> Backend chạy tại: **http://localhost:8080/api**

---

## 4. Chạy Frontend

```bash
cd frontend
npm install
npm run dev
```

> Frontend chạy tại: **http://localhost:5173**

---

## 5. Kiểm tra chức năng Calendar

### 5.1. Truy cập trang Calendar

1. Mở trình duyệt → **http://localhost:5173**
2. Đăng nhập bằng tài khoản có sẵn trong database
3. Chọn một **Room** ở sidebar bên trái
4. Trong sidebar Space, tìm mục **"KÊNH LỊCH TRÌNH"** → Click vào một space Calendar

### 5.2. Kiểm tra hiển thị lịch

| Chức năng | Cách kiểm tra | Kết quả mong đợi |
|-----------|---------------|-------------------|
| **View Tháng** (mặc định) | Mở trang Calendar | Hiển thị grid lịch tháng hiện tại, panel event bên phải |
| **View Tuần** | Click nút **"Tuần"** ở header | Hiển thị 7 cột, mỗi cột là 1 ngày trong tuần |
| **View Năm** | Click nút **"Năm"** ở header | Hiển thị 12 mini-calendar, click tháng → chuyển về Month view |
| **Navigation** | Click **◀ / ▶** hoặc nút **"Hôm nay"** | Chuyển tháng/tuần/năm tương ứng |
| **Chọn ngày** | Click vào 1 ngày trong grid | Panel bên phải hiển thị danh sách event của ngày đó |

### 5.3. Kiểm tra CRUD Event

#### Thêm sự kiện mới
1. Click nút **"＋ Thêm sự kiện"** ở góc phải trên
2. Điền form: Tiêu đề, Mô tả, Ngày, Giờ bắt đầu, Giờ kết thúc
3. (Tùy chọn) Bật **"Cho phép mọi người chỉnh sửa"**
4. Click **"Tạo sự kiện"**
5. **Kết quả:** Event mới xuất hiện trong danh sách + dot indicator trên ngày tương ứng

#### Chỉnh sửa sự kiện
1. Hover vào event trong danh sách → Click icon ✏️
2. Sửa thông tin trong dialog → Click **"Cập nhật"**
3. **Kết quả:** Thông tin event được cập nhật

> **Quyền chỉnh sửa:** Chỉ người tạo hoặc event có bật `allowEditAll` mới thấy nút sửa.

#### Xóa sự kiện
1. Hover vào event → Click icon 🗑️
2. Xác nhận xóa trong popup
3. **Kết quả:** Event bị xóa khỏi danh sách

> **Quyền xóa:** Chỉ người tạo event mới thấy nút xóa.

### 5.4. Kiểm tra API trực tiếp (tùy chọn)

Nếu muốn test API backend riêng, dùng `curl` hoặc Postman:

```bash
# Lấy tất cả event của 1 space
GET http://localhost:8080/api/calendar-events/{spaceId}

# Lấy event theo khoảng ngày
GET http://localhost:8080/api/calendar-events/{spaceId}/range?start=2026-03-01&end=2026-03-31

# Lấy event theo ngày cụ thể
GET http://localhost:8080/api/calendar-events/{spaceId}/date?date=2026-03-01

# Tạo event mới
POST http://localhost:8080/api/calendar-events
Content-Type: application/json
{
  "spaceId": "<space-uuid>",
  "title": "Họp nhóm",
  "description": "Họp sprint planning",
  "eventDate": "2026-03-05",
  "startTime": "09:00",
  "endTime": "10:00",
  "allowEditAll": false,
  "createdById": "<user-uuid>"
}

# Cập nhật event
PUT http://localhost:8080/api/calendar-events/{eventId}
Content-Type: application/json
{ ...same body as POST... }

# Xóa event
DELETE http://localhost:8080/api/calendar-events/{eventId}?userId=<user-uuid>
```

---

## 6. Xử lý lỗi thường gặp

| Lỗi | Nguyên nhân | Cách khắc phục |
|-----|-------------|----------------|
| `Connection refused` khi chạy backend | MySQL chưa chạy | Khởi động MySQL service |
| `Access denied` | Sai username/password MySQL | Sửa `application.yml` hoặc set biến môi trường |
| Trang trắng khi mở frontend | Backend chưa chạy | Chạy backend trước, sau đó refresh |
| Không thấy kênh Calendar | Room chưa có space Calendar | Tạo space mới với type `CALENDAR` trong database |

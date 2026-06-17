# API User và Manager/Admin

## Thông tin chung

- Base URL local: `http://localhost:8080/api`
- Xác thực: JWT Bearer token
- Header:

```http
Authorization: Bearer <access_token>
Content-Type: application/json
```

### Phân quyền

| Nhóm API | ADMIN | MANAGER |
|---|---:|---:|
| `/manage/users/**` | Có | Có |
| `/manage/admin/**` | Có | Không |
| Đổi role của User | Có | Không |

API User chỉ đọc và thao tác tài khoản đang có role `USER`. Sau khi ADMIN nâng
User thành `MANAGER` hoặc `ADMIN`, tài khoản đó sẽ không còn xuất hiện trong
danh sách User và sẽ xuất hiện trong danh sách Manager/Admin.

---

## I. User API

### 1. Lấy danh sách User

```http
GET /api/manage/users
```

Quyền: `ADMIN`, `MANAGER`.

Query parameters:

| Tên | Kiểu | Mặc định | Mô tả |
|---|---|---:|---|
| `search` | string | | Tìm theo username, email hoặc displayName |
| `status` | enum | | `ACTIVE`, `INACTIVE`, `BANNED` |
| `plan` | enum | | `FREE`, `TEAM`, `BUSINESS` |
| `dateFrom` | datetime | | ISO date-time, ví dụ `2026-06-01T00:00:00` |
| `dateTo` | datetime | | ISO date-time |
| `page` | integer | `0` | Trang bắt đầu từ 0 |
| `size` | integer | `20` | Từ 1 đến 100 |

Ví dụ:

```http
GET /api/manage/users?search=an&status=ACTIVE&plan=FREE&page=0&size=20
```

Response `200`:

```json
{
  "success": true,
  "message": "Get user list successfully",
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "username": "nguyenvana",
      "displayName": "Nguyen Van A",
      "email": "user@synkork.com",
      "avatarUrl": null,
      "role": "user",
      "plan": "FREE",
      "status": "active",
      "provider": "local",
      "createdAt": "2026-06-15T10:00:00",
      "updatedAt": "2026-06-15T10:00:00"
    }
  ],
  "meta": {
    "page": 1,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "hasNext": false,
    "hasPrev": false
  }
}
```

Lưu ý: tham số `role` có trong DTO filter nhưng service luôn giới hạn kết quả
về role `USER`.

### 2. Lấy chi tiết User

```http
GET /api/manage/users/{id}
```

Quyền: `ADMIN`, `MANAGER`.

Response `200`:

```json
{
  "success": true,
  "message": "Get user successfully",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "username": "nguyenvana",
    "displayName": "Nguyen Van A",
    "email": "user@synkork.com",
    "avatarUrl": null,
    "role": "user",
    "plan": "FREE",
    "status": "active",
    "provider": "local",
    "createdAt": "2026-06-15T10:00:00",
    "updatedAt": "2026-06-15T10:00:00"
  },
  "meta": null
}
```

API từ chối nếu ID thuộc tài khoản `MANAGER` hoặc `ADMIN`.

### 3. Tạo User

```http
POST /api/manage/users
```

Quyền: `ADMIN`, `MANAGER`.

Request:

```json
{
  "firstName": "Van A",
  "lastName": "Nguyen",
  "username": "nguyenvana",
  "email": "user@synkork.com",
  "status": "active",
  "role": "user"
}
```

Validation:

- `firstName`, `lastName`, `username`, `email`, `status` bắt buộc.
- `email` phải đúng định dạng và chưa tồn tại.
- `username` phải chưa tồn tại.
- `status`: `active`, `inactive`, `banned`.
- `role` nếu truyền vào chỉ nhận `user`.

Hệ thống tự sinh mật khẩu tạm thời 8 ký tự, mã hóa mật khẩu và gửi email nếu
mail service đang hoạt động.

Response `201`: `ApiResponse<AdminUserResponse>`.

Lưu ý: frontend hiện có gửi thêm `plan`, nhưng DTO backend không nhận và service
không xử lý trường này khi tạo.

### 4. Cập nhật User

```http
PATCH /api/manage/users/{id}
```

Quyền:

- `ADMIN`: sửa thông tin và đổi role.
- `MANAGER`: sửa thông tin User, không được đổi role.

Tất cả trường đều không bắt buộc:

```json
{
  "displayName": "Nguyen Van A",
  "email": "new-email@synkork.com",
  "plan": "TEAM",
  "status": "ACTIVE",
  "role": "manager"
}
```

Giá trị hợp lệ:

- `plan`: `FREE`, `TEAM`, `BUSINESS`.
- `status`: `ACTIVE`, `INACTIVE`, `BANNED`.
- `role`: `user`, `manager`, `admin`.

Chỉ ADMIN được gửi thay đổi role. Đây là API dùng để nâng User lên Manager:

```json
{
  "role": "manager"
}
```

Hoặc nâng User lên Admin:

```json
{
  "role": "admin"
}
```

Response `200`: `ApiResponse<AdminUserResponse>`.

### 5. Cập nhật trạng thái User

```http
PATCH /api/manage/users/{id}/status
```

Quyền: `ADMIN`, `MANAGER`.

Request:

```json
{
  "status": "BANNED"
}
```

Giá trị nên gửi viết hoa: `ACTIVE`, `INACTIVE`, `BANNED`.

Response `200`: `ApiResponse<AdminUserResponse>`.

### 6. Xóa User

```http
DELETE /api/manage/users/{id}
```

Quyền: `ADMIN`, `MANAGER`.

Chỉ xóa được tài khoản có role `USER`.

Response `200`:

```json
{
  "success": true,
  "message": "Delete user successfully",
  "data": {
    "message": "Xoa nguoi dung thanh cong"
  },
  "meta": null
}
```

---

## II. Manager/Admin API

Toàn bộ API trong phần này chỉ dành cho `ADMIN`.

### 1. Lấy danh sách Manager và Admin

```http
GET /api/manage/admin
```

Query parameters:

| Tên | Kiểu | Mặc định | Mô tả |
|---|---|---:|---|
| `keyword` | string | | Tìm username, email hoặc displayName |
| `status` | string | | `active`, `inactive`, `banned` |
| `page` | integer | `0` | Trang bắt đầu từ 0 |
| `size` | integer | `20` | Số bản ghi mỗi trang |

Ví dụ:

```http
GET /api/manage/admin?keyword=admin&status=active&page=0&size=20
```

Response `200`:

```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "username": "admin01",
      "displayName": "System Admin",
      "email": "admin@synkork.com",
      "avatarUrl": null,
      "role": "admin",
      "status": "active",
      "provider": "local",
      "createdAt": "2026-06-15T10:00:00",
      "updatedAt": "2026-06-15T10:00:00"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "last": true
}
```

Danh sách gồm cả role `MANAGER` và `ADMIN`.

### 2. Lấy chi tiết Manager/Admin

```http
GET /api/manage/admin/{id}
```

Quyền: `ADMIN`.

Response `200`: `ManagerResponse`.

API từ chối nếu ID thuộc tài khoản role `USER`.

### 3. Tạo Manager hoặc Admin

```http
POST /api/manage/admin
```

Quyền: `ADMIN`.

Request tạo Manager:

```json
{
  "displayName": "Manager One",
  "username": "manager01",
  "email": "manager@synkork.com",
  "status": "active",
  "role": "manager"
}
```

Request tạo Admin:

```json
{
  "displayName": "Admin One",
  "username": "admin01",
  "email": "admin@synkork.com",
  "status": "active",
  "role": "admin"
}
```

Validation:

- Tất cả trường bắt buộc.
- `email` và `username` phải chưa tồn tại.
- `status`: `active`, `inactive`, `banned`.
- `role`: `manager`, `admin`.

Hệ thống tự sinh mật khẩu tạm thời 8 ký tự và gửi email nếu mail service đang
hoạt động.

Response `201`: `ManagerResponse`.

### 4. Cập nhật Manager/Admin

```http
PATCH /api/manage/admin/{id}
```

Quyền: `ADMIN`.

Tất cả trường đều không bắt buộc:

```json
{
  "displayName": "New Display Name",
  "email": "new-admin@synkork.com",
  "status": "inactive",
  "role": "manager"
}
```

API cho phép:

- Đổi `MANAGER` thành `ADMIN`.
- Đổi `ADMIN` thành `MANAGER`.
- Sửa displayName, email và status.
- Không cho sửa username.
- Không cho đổi trực tiếp về role `USER`.

Response `200`: `ManagerResponse`.

### 5. Xóa Manager/Admin

```http
DELETE /api/manage/admin/{id}
```

Quyền: `ADMIN`.

Chỉ xóa được tài khoản có role `MANAGER` hoặc `ADMIN`.

Response `200`:

```json
{
  "message": "Xoa tai khoan manager thanh cong"
}
```

---

## Mã HTTP thường gặp

| Mã | Ý nghĩa |
|---:|---|
| `200` | Thành công |
| `201` | Tạo tài khoản thành công |
| `400` | Request, enum hoặc ID tài khoản không hợp lệ |
| `401` | Thiếu token, token hết hạn hoặc không hợp lệ |
| `403` | Không đủ quyền truy cập endpoint |
| `404` | Không tìm thấy đường dẫn API |

Lưu ý theo exception handler hiện tại: trường hợp không tìm thấy tài khoản đang
được service ném dưới dạng `IllegalArgumentException`, vì vậy response thực tế
là `400`, không phải `404`.

## Tổng hợp endpoint

| Method | Endpoint | Quyền |
|---|---|---|
| `GET` | `/api/manage/users` | ADMIN, MANAGER |
| `GET` | `/api/manage/users/{id}` | ADMIN, MANAGER |
| `POST` | `/api/manage/users` | ADMIN, MANAGER |
| `PATCH` | `/api/manage/users/{id}` | ADMIN, MANAGER |
| `PATCH` | `/api/manage/users/{id}/status` | ADMIN, MANAGER |
| `DELETE` | `/api/manage/users/{id}` | ADMIN, MANAGER |
| `GET` | `/api/manage/admin` | ADMIN |
| `GET` | `/api/manage/admin/{id}` | ADMIN |
| `POST` | `/api/manage/admin` | ADMIN |
| `PATCH` | `/api/manage/admin/{id}` | ADMIN |
| `DELETE` | `/api/manage/admin/{id}` | ADMIN |

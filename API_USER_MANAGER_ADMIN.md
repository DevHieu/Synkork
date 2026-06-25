# API User va Manager/Admin - Postman

## Cau hinh chung

Base URL:

```http
http://localhost:8080/api
```

Headers:

```http
Authorization: Bearer <access_token>
Content-Type: application/json
```

Quyen:

| API | ADMIN | MANAGER |
|---|---:|---:|
| `/manage/users/**` | Co | Co |
| `/manage/admin/**` | Co | Khong |

## 1. Users Manage API

Dung de test Postman cho menu **Users** trong portal admin.

Base endpoint:

```http
/manage/users
```

### 1.1 Lay danh sach users

Request don gian nhat:

```http
GET /manage/users
```

Co filter:

```http
GET /manage/users?search=tram&status=ACTIVE&plan=FREE&page=0&size=20
```

Query params:

| Param | Bat buoc | Vi du | Ghi chu |
|---|---:|---|---|
| `search` | Khong | `tram` | Tim theo username/email/displayName |
| `status` | Khong | `ACTIVE` | `ACTIVE`, `INACTIVE`, `BANNED` |
| `plan` | Khong | `FREE` | `FREE`, `TEAM`, `BUSINESS` |
| `dateFrom` | Khong | `2026-06-01T00:00:00` | Loc theo ngay tao |
| `dateTo` | Khong | `2026-06-30T23:59:59` | Loc theo ngay tao |
| `page` | Khong | `0` | Mac dinh `0` |
| `size` | Khong | `20` | Mac dinh `20`, toi da `100` |

Note: API nay chi tra ve tai khoan role `USER`.

Response can xem:

```json
{
  "data": [
    {
      "id": "uuid",
      "username": "nguyenvana",
      "displayName": "Nguyen Van A",
      "email": "user@synkork.com",
      "role": "user",
      "plan": "FREE",
      "status": "active",
      "provider": "local",
      "createdAt": "2026-06-23T10:00:00"
    }
  ],
  "meta": {
    "page": 0,
    "size": 20,
    "totalElements": 1
  }
}
```

### 1.2 Lay chi tiet user

```http
GET /manage/users/{id}
```

Vi du:

```http
GET /manage/users/019ed000-0000-7000-8000-000000000000
```

### 1.3 Tao user

```http
POST /manage/users
```

Body:

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

Field:

| Field | Bat buoc | Gia tri |
|---|---:|---|
| `firstName` | Co | Text |
| `lastName` | Co | Text |
| `username` | Co | Text, khong trung |
| `email` | Co | Email, khong trung |
| `status` | Co | `active`, `inactive`, `banned` |
| `role` | Khong | Chi nen gui `user` |

Note: backend tao `displayName = firstName + lastName`, tao password tam thoi va gui email neu mail config chay.

### 1.4 Cap nhat user

```http
PATCH /manage/users/{id}
```

Body mau:

```json
{
  "displayName": "Nguyen Van A",
  "email": "new-user@synkork.com",
  "plan": "TEAM",
  "status": "ACTIVE"
}
```

Tat ca field trong body deu optional.

| Field | Gia tri |
|---|---|
| `displayName` | Text |
| `email` | Email, khong trung |
| `plan` | `FREE`, `TEAM`, `BUSINESS` |
| `status` | `ACTIVE`, `INACTIVE`, `BANNED` |
| `role` | `user`, `manager`, `admin` |

Note: chi `ADMIN` moi duoc gui field `role`. Neu test bang token `MANAGER`, dung body khong co `role`.

### 1.5 Cap nhat trang thai user

```http
PATCH /manage/users/{id}/status
```

Body:

```json
{
  "status": "BANNED"
}
```

Gia tri nen test: `ACTIVE`, `INACTIVE`, `BANNED`.

### 1.6 Xoa user

```http
DELETE /manage/users/{id}
```

Note: API nay chi xoa tai khoan role `USER`; neu id la manager/admin se bao loi.

## 2. Manager/Admin API

Chi `ADMIN` duoc goi cac API nay.

### Lay danh sach manager/admin

```http
GET /manage/admin?keyword=admin&status=active&role=admin&page=0&size=20
```

Query params:

| Param | Gia tri |
|---|---|
| `keyword` | username/email/displayName |
| `status` | `active`, `inactive`, `banned` |
| `role` | `manager`, `admin` |
| `page` | mac dinh `0` |
| `size` | mac dinh `20` |

### Lay chi tiet manager/admin

```http
GET /manage/admin/{id}
```

### Tao manager/admin

```http
POST /manage/admin
```

Body tao manager:

```json
{
  "displayName": "Manager One",
  "username": "manager01",
  "email": "manager@synkork.com",
  "status": "active",
  "role": "manager"
}
```

Body tao admin:

```json
{
  "displayName": "Admin One",
  "username": "admin01",
  "email": "admin@synkork.com",
  "status": "active",
  "role": "admin"
}
```

Bat buoc: `displayName`, `username`, `email`, `status`, `role`.

Gia tri hop le:

| Field | Gia tri |
|---|---|
| `status` | `active`, `inactive`, `banned` |
| `role` | `manager`, `admin` |

### Cap nhat manager/admin

```http
PATCH /manage/admin/{id}
```

Body mau:

```json
{
  "displayName": "New Display Name",
  "email": "new-admin@synkork.com",
  "status": "inactive",
  "role": "manager"
}
```

Tat ca field deu optional. Khong sua `username`. Khong doi ve role `user`.

### Xoa manager/admin

```http
DELETE /manage/admin/{id}
```

Chi xoa duoc tai khoan co role `MANAGER` hoac `ADMIN`.

## 3. Response nhanh

User API tra dang:

```json
{
  "success": true,
  "message": "...",
  "data": {},
  "meta": null
}
```

Manager/Admin API tra DTO truc tiep, khong boc `success/message`.

## 4. Ma loi hay gap

| Ma | Ly do |
|---:|---|
| `400` | Body sai, enum sai, email/username trung, id khong dung loai tai khoan |
| `401` | Thieu token hoac token het han |
| `403` | Khong du quyen |
| `404` | Sai URL |

## 5. Tom tat endpoint

| Method | Endpoint |
|---|---|
| `GET` | `/api/manage/users` |
| `GET` | `/api/manage/users/{id}` |
| `POST` | `/api/manage/users` |
| `PATCH` | `/api/manage/users/{id}` |
| `PATCH` | `/api/manage/users/{id}/status` |
| `DELETE` | `/api/manage/users/{id}` |
| `GET` | `/api/manage/admin` |
| `GET` | `/api/manage/admin/{id}` |
| `POST` | `/api/manage/admin` |
| `PATCH` | `/api/manage/admin/{id}` |
| `DELETE` | `/api/manage/admin/{id}` |

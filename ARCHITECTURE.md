# Architecture Overview
Đây là file giải thích hệ thống Synkork được thiết kế như thế nào

Synkork là 1 trang web giúp các hội nhóm hay group công ty có thể ngồi giao tiếp, trao đổi công việc.
Ứng dụng hoạt động như Discord nhưng được tích hợp thêm nhiều chức năng để giúp các dân văn phòng có thể quản lí công việc tốt hơn.

Các chức năng chính của dự án sẽ bao gồm:

- Chat realtime
- Video call nhóm
- Lịch chung cho từng nhóm
- Note chung cho từng nhóm
- Quản lí task của từng nhóm

Tech stack được sử dụng trong dự án

- Frontend: Vuejs + ShadCn
- Backend: Java (Spring boot)
- Video Call SDK: ZEGOCLOUD
- Realtime: Websocket
- Database: MySql
- Auth: Jwt + oAuth2

## 1. Project Structure
Phần này cung cấp tổng quan cấp cao về cấu trúc thư mục và tập tin của dự án, được phân loại theo lớp kiến ​​trúc hoặc lĩnh vực chức năng chính. Điều này rất cần thiết để nhanh chóng điều hướng mã nguồn, định vị các tập tin liên quan và hiểu được cấu trúc tổng thể cũng như sự phân tách trách nhiệm.


[Project Root]/
├── backend/              # Chứa toàn bộ mã nguồn backend và các API phía server
│   └── src/              # Mã nguồn chính của backend
│       ├── java/   
│       │   ├── common/       # Chứa các cấu hình hệ thống (security, websocket, cors,…)
│       │   ├── config/       # Chứa các cấu hình hệ thống (security, websocket, cors,…)
│       │   ├── filter/       # Các filter xử lý request/response (JWT, logging, …)
│       │   ├── security/     # Cấu hình bảo mật và phân quyền hệ thống
│       │   └── modules/      # Các module chức năng, mỗi module tương ứng một nhóm API
│       │        └── <module-name>/                       # Một module chức năng (auth, user, room, message, ...)
│       │             ├── <Module>Controller.java         # Định nghĩa các API endpoint của module
│       │             ├── <Module>Service.java            # Xử lý logic nghiệp vụ của module
│       │             ├── <Module>Repository.java         # Tầng truy cập dữ liệu
│       │             ├── <Module>Entity.java             # Entity ánh xạ với bảng trong database
│       │             ├── dto/                            # Các DTO dùng để trao đổi dữ liệu
│       │             └── enums/                          # Các enum phục vụ logic nghiệp vụ
│       │   
│       ├── tests/            # Kiểm thử (unit test, integration test)
│       └── Dockerfile        # Cấu hình Docker để build và deploy backend
│
├── frontend/             # Chứa toàn bộ mã nguồn giao diện người dùng
│   ├── src/              # Mã nguồn chính của frontend
│   │   ├── components/   # Các UI component có thể tái sử dụng
│   │   ├── pages/        # Các trang / màn hình chính của ứng dụng
│   │   ├── lib/          # Các hàm, cấu hình và thư viện dùng chung cho frontend
│   │   ├── assets/       # Hình ảnh, font, icon và các tài nguyên tĩnh
│   │   ├── services/     # Các service dùng để gọi API backend
│   │   ├── routers/      # Cấu hình routing và điều hướng trang
│   │   └── stores/       # Quản lý trạng thái ứng dụng (Vuex / Pinia)
│   ├── public/           # Các tài nguyên được public (index.html, favicon, …)
│   └── package.json      # Khai báo dependency và script cho frontend
│
├── docs/                 # Tài liệu dự án (API, hướng dẫn cài đặt, …)
│
├── .github/              # Cấu hình CI/CD (GitHub Actions, workflow, …)
├── .gitignore            # Danh sách các file/thư mục bị Git bỏ qua
├── README.md             # Tổng quan dự án và hướng dẫn khởi động nhanh
└── ARCHITECTURE.md       # Tài liệu mô tả kiến trúc hệ thống

## 2. High-Level System Diagram
Sơ đồ sau minh họa các thành phần chính của hệ thống và luồng giao tiếp giữa chúng, từ người dùng, ứng dụng frontend, backend service đến cơ sở dữ liệu.
 
[User] <--> [Frontend Application] <--> [Backend Service] <--> [Database]                        

## 3. Core Components

### 3.1. Frontend

Name: Synkork Webapp

Description: Synkork Webapp là giao diện chính để người dùng tương tác với hệ thống. Ứng dụng cho phép người dùng đăng nhập/đăng ký tài khoản, quản lí tài khoản, sử dụng các chức năng chính của ứng dụng (Chat realtime, Call video, ...). Frontend chịu trách nhiệm hiển thị dữ liệu, xử lý tương tác người dùng và giao tiếp với backend thông qua các API và kết nối thời gian thực.

Technologies: VueJs, Tailwind, ShadCn, Websocket

Deployment: Render

### 3.2. Backend Services

Name: Synkork backend service

Description: Synkork Backend sẽ là nơi để xử lí các dữ liệu, login nghiệp vụ, xác thực người dùng, quản lý phòng, nhắn tin thời gian thực và các chức năng cộng tác nhóm. Backend được tổ chức theo kiến trúc module-based, trong đó mỗi module đảm nhiệm một nhóm chức năng riêng nhưng cùng chạy trong một ứng dụng duy nhất.

Technologies: Java (Spring Boot), Websocket

Deployment: Render

## 4. Data Stores

Phần này mô tả các kho dữ liệu được sử dụng trong hệ thống, bao gồm mục đích lưu trữ và vai trò của từng bảng dữ liệu trong ứng dụng.

Các bảng dữ liệu chính bao gồm:
  + Users: Lưu trữ thông tin người dùng
  + Rooms: Lưu trữ thông tin các phòng làm việc được tạo
  + Room_Members: Quản lý quan hệ người dùng – phòng
  + Spaces: Các không gian chức năng trong Room (Chat, Voice, Task, Note, Calendar)
  + Messages: Lưu trữ tin nhắn trong các Space chat
  + Notes: Lưu trữ ghi chú trong Space
  + Calendar_Events: Lưu trữ các sự kiện lịch làm việc
  + Boards: Bảng quản lý công việc (Kanban)
  + Columns: Các cột trạng thái trong Board
  + Cards: Các task công việc
  + Edit_History: Lưu lịch sử chỉnh sửa của Note, Event và Task

Technologies: MySql

### 4.1. Primary Database

Name: Application Database

Type: MySQL

Purpose: 
Lưu trữ toàn bộ dữ liệu nghiệp vụ của hệ thống, bao gồm người dùng, phòng làm việc, không gian chức năng, tin nhắn và dữ liệu quản lý công việc.

Key Tables:
Users, Rooms, Room_Members, Spaces, Messages, Notes, Calendar_Events, Boards, Columns, Cards, Edit_History

## 5. External Integrations / APIs

Service Name 1: ZegoCloud SDK

Purpose: Giúp làm chức năng Video call nhóm được dễ dàng hơn

Integration Method: SDK

## 6. Deployment & Infrastructure (Chưa xác định)

Cloud Provider: [e.g., AWS, GCP, Azure, On-premise]

Key Services Used: [e.g., EC2, Lambda, S3, RDS, Kubernetes, Cloud Functions, App Engine]

CI/CD Pipeline: [e.g., GitHub Actions, GitLab CI, Jenkins, CircleCI]

Monitoring & Logging: [e.g., Prometheus, Grafana, CloudWatch, Stackdriver, ELK Stack]

## 7. Security Considerations

Authentication: OAuth2, JWT

Authorization: Role-Based Access Control (RBAC)

Data Encryption: TLS in transit, Password hashing using BCrypt

Key Security Tools/Practices: Spring Security, JWT expiration, CORS configuration, Password hashing

## 8. Development & Testing Environment (Chưa xác định)

Local Setup Instructions: [Link to CONTRIBUTING.md or brief steps]

Testing Frameworks: [e.g., Jest, Pytest, JUnit]

Code Quality Tools: [e.g., ESLint, Black, SonarQube]

## 9. Future Considerations / Roadmap

Do giới hạn về thời gian thực hiện, dự án hiện tại tập trung tối ưu hóa các tính năng cốt lõi. Các thành phần mở rộng như Hệ thống Lịch (Calendar) và Quản lý tác vụ chung (Shared Tasks) 
và hiện lịch sử thay đổi (Edit History) được thiết kế sẵn cấu trúc chờ để tích hợp trong các giai đoạn phát triển tiếp theo.

## 10. Project Identification

Project Name: Synkork

Repository URL: (Chưa xác định)

Primary Contact/Team: Bùi Minh Hiếu. Email: hieudd2090@gmail.com

Date of Last Update: 

## 11. Glossary / Acronyms (Chưa xác định)

Define any project-specific terms or acronyms.)

[Acronym]: [Full Definition]

[Term]: [Explanation]
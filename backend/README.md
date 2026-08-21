# Synkork — Backend Service

The backend for Synkork, a real-time team collaboration platform. Built with Java Spring Boot, this service handles all business logic, authentication, real-time communication, and integrations.

---

## Tech Stack

| Category | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.5.9 |
| Database | MySQL 8 |
| ORM | Spring Data JPA (Hibernate) |
| Authentication | JWT + OAuth2 (Google) |
| Real-time | WebSocket (STOMP) |
| File Storage | Cloudinary |
| Video Call | ZegoCloud SDK |
| Payment | MoMo |
| Observability | Prometheus + Grafana + Loki + Tempo (Zipkin) |
| Containerization | Docker + Docker Compose |

---

## Project Structure

```
backend/
├── src/main/java/com/synkork/backend/
│   ├── common/             # Shared utilities, base classes
│   ├── config/             # App configuration (Security, WebSocket, CORS, etc.)
│   ├── filter/             # HTTP filters (JWT auth filter, etc.)
│   ├── security/           # Security setup and OAuth2 handlers
│   ├── exception/          # Global exception handling
│   └── modules/            # Feature modules
│       ├── auth/           # Authentication & authorization
│       ├── user/           # User profile management
│       ├── room/           # Room (workspace) management
│       ├── roomMember/     # Room membership & roles
│       ├── space/          # Spaces within a room (chat, call, etc.)
│       ├── message/        # Real-time chat messages
│       ├── friend/         # Friend system
│       ├── notification/   # In-app notifications
│       ├── collaboration/
│       │   ├── note/       # Collaborative notes
│       │   ├── task/       # Kanban boards & task management
│       │   └── calendar/   # Shared team calendar events
│       ├── payment/        # MoMo payment integration
│       ├── admin/          # Admin management & statistics
│       ├── verification/   # Email verification
│       ├── report/         # User reporting system
│       └── zego/           # ZegoCloud token generation
├── docker/
│   ├── grafana/            # Grafana dashboard config
│   ├── prometheus/         # Prometheus scrape config
│   └── tempo/              # Tempo tracing config
├── .env.example            # Environment variable template
├── docker-compose.yml      # Docker Compose for full deployment
└── Dockerfile              # Multi-stage Docker build
```

---

## Prerequisites

- **Java 21** (JDK)
- **Maven 3.9+**
- **MySQL 8.x** — running locally or via Docker
- **Docker & Docker Compose** (optional, for containerized setup)

---

## Getting Started

### 1. Clone the repository

```sh
git clone https://github.com/DevHieu/Synkork.git
cd Synkork/backend
```

### 2. Configure environment variables

Copy the example env file and fill in your values:

```sh
cp .env.example .env
```

Then edit `.env`:

```env
FRONTEND_URL=http://localhost:5173
ADMIN_PORTAL_URL=http://localhost:5174
SERVER_URL=http://localhost:8080

# MySQL
DB_NAME=synkork
DB_USERNAME=root
DB_PASSWORD=your_password
DB_ROOT_PASSWORD=your_root_password

# Google OAuth2 (https://console.cloud.google.com)
GOOGLE_CLIENT_ID=
GOOGLE_CLIENT_SECRET=
GOOGLE_CALENDAR_REDIRECT_URI=http://localhost:8080/api/integrations/google-calendar/callback

# Gmail (for sending emails)
GMAIL_USERNAME=
GMAIL_PASSWORD=

# Cloudinary (https://cloudinary.com)
CLOUDINARY_CLOUD_NAME=
CLOUDINARY_API_KEY=
CLOUDINARY_API_SECRET=

# ZegoCloud (https://www.zegocloud.com)
ZEGO_APPID=
ZEGO_SERVER_SECRET=

# Google Gemini AI
GEMINI_API_KEY=

# MoMo Payment
MOMO_ACCESS_KEY=
MOMO_SECRET_KEY=
```

### 3. Run the application

**Option A — Local (requires MySQL running separately):**

```sh
./mvnw spring-boot:run
```

**Option B — Docker Compose (includes MySQL):**

```sh
docker-compose up --build
```

The API will be available at `http://localhost:8080/api`.

---

## API Documentation

Once the server is running, interactive API docs (Swagger UI) are available at:

```
http://localhost:8080/api/swagger-ui/index.html
```

---

## Observability Stack (Optional)

The `docker/` directory contains configuration files for the full observability stack:

| Tool | Purpose | Port |
|---|---|---|
| Prometheus | Metrics collection | 9090 |
| Grafana | Metrics & log dashboards | 3000 |
| Loki | Log aggregation | 3100 |
| Tempo | Distributed tracing | 9411 |

Metrics are exposed by the backend at `/api/actuator/prometheus`.

---

## Key Environment Variables Reference

| Variable | Description |
|---|---|
| `DB_URL` | MySQL JDBC connection string |
| `GOOGLE_CLIENT_ID` | Google OAuth2 client ID |
| `GOOGLE_CLIENT_SECRET` | Google OAuth2 client secret |
| `GMAIL_USERNAME` | Gmail address used for sending emails |
| `GMAIL_PASSWORD` | Gmail app password |
| `CLOUDINARY_CLOUD_NAME` | Cloudinary cloud name for file uploads |
| `ZEGO_APPID` | ZegoCloud app ID for video call token generation |
| `ZEGO_SERVER_SECRET` | ZegoCloud server secret |
| `GEMINI_API_KEY` | Google Gemini AI API key |
| `MOMO_ACCESS_KEY` | MoMo payment access key |
| `MOMO_SECRET_KEY` | MoMo payment secret key |

<!-- CONTRIBUTING -->
## Contributing

Contributions are what make the open source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

If you have a suggestion that would make this better, please fork the repo and create a pull request. You can also simply open an issue with the tag "enhancement".
Don't forget to give the project a star! Thanks again!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Top contributors:

<a href="https://github.com/DevHieu/Synkork/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=DevHieu/Synkork" />
</a>

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- LICENSE -->
## License

Distributed under the Unlicense License. See `LICENSE.txt` for more information.

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- CONTACT -->
## Contact

Bùi Minh Hiếu - [@DevHieu](https://github.com/DevHieu) - hieudd2090@gmail.com

Project Link: [https://github.com/DevHieu/Synkork](https://github.com/DevHieu/Synkork)

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- ACKNOWLEDGMENTS -->
## Acknowledgments

This project has been built using various open-source packages and frameworks that made its development possible:

* [Spring Boot](https://spring.io/projects/spring-boot)
* [VueJS](https://vuejs.org/)
* [Shadcn Vue](https://www.shadcn-vue.com/)
* [Tailwind CSS](https://tailwindcss.com/)
* [ZegoCloud SDK](https://www.zegocloud.com/)
* [Cloudinary](https://cloudinary.com/)
* [Liquid Loader (Admin Login)](https://github.com/Nazia-99/Svg-Loader)
* [Vue Grid Layout](https://github.com/marshal-zheng/vue-grid-layout)
* [ShadCn Vue Admin Template](https://github.com/Whbbit1999/shadcn-vue-admin)
* [README template](https://github.com/othneildrew/Best-README-Template)

<p align="right">(<a href="#readme-top">back to top</a>)</p>
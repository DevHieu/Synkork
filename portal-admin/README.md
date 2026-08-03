# Synkork — Admin Portal

The administration dashboard for the Synkork platform. Built with Vue 3 + TypeScript + Tailwind CSS, this portal is intended for system administrators and managers to monitor platform activity, manage users, rooms, subscriptions, and handle user reports.

---

## Tech Stack

| Category | Technology |
|---|---|
| Framework | Vue 3 (Composition API) |
| Language | TypeScript |
| Build Tool | Vite |
| Package Manager | pnpm |
| Styling | Tailwind CSS v4 |
| UI Components | Shadcn Vue / Reka UI |
| State Management | Pinia (with `pinia-plugin-persistedstate`) |
| Routing | Vue Router 5 |
| Data Fetching | TanStack Vue Query |
| Tables | TanStack Vue Table |
| Forms & Validation | VeeValidate + Zod |
| Charts | Unovis |
| Animations | Motion V |
| HTTP Client | Axios / ofetch |
| i18n | Vue I18n |
| Containerization | Docker + Nginx |

---

## Project Structure

```
portal-admin/
├── src/
│   ├── assets/             # Static assets (images, icons)
│   ├── components/         # Shared reusable UI components
│   ├── composables/        # Vue composables (shared logic)
│   ├── constants/          # App-wide constants
│   ├── layouts/            # Page layout shells
│   ├── lib/                # Utility wrappers and shared config
│   ├── pages/              # Route-based page components
│   │   ├── auth/           # Login page
│   │   ├── dashboard/      # Dashboard with stats & charts
│   │   │   └── tabs/
│   │   │       ├── overview-content.vue       # Platform overview tab
│   │   │       ├── user-overview.vue          # Users statistics tab
│   │   │       ├── room-overview.vue          # Rooms statistics tab
│   │   │       ├── subscription-overview.vue  # Subscriptions tab
│   │   │       └── report-overview.vue        # Reports summary tab
│   │   ├── users/          # User management (list, detail, ban)
│   │   ├── rooms/          # Room management
│   │   ├── subscriptions/  # Subscription management
│   │   ├── billing/        # Billing overview
│   │   ├── report/         # User report handling
│   │   ├── manager/        # Manager account management
│   │   ├── ai-talk/        # AI assistant integration
│   │   ├── log/            # System activity logs
│   │   └── settings/       # Portal settings
│   ├── plugins/            # Vue plugin registrations
│   ├── router/             # Vue Router config & route guards
│   ├── services/           # API service layer
│   ├── stores/             # Pinia state stores
│   ├── types/              # TypeScript type definitions
│   ├── utils/              # Helper functions
│   └── validators/         # Zod schema validators
├── public/                 # Static public assets
├── .env                    # Local environment variables (gitignored)
├── .env.example            # Environment variable template
├── Dockerfile              # Multi-stage Docker build (Node → Nginx)
├── docker-compose.yml      # Compose config for containerized run
├── vite.config.ts          # Vite configuration
└── package.json            # Dependencies and scripts
```

---

## Prerequisites

- **Node.js v22 or higher**
- **pnpm v10.33.2** — this project uses pnpm as the package manager

Install pnpm if you don't have it:

```sh
npm install -g pnpm@10.33.2
```

---

## Getting Started

### 1. Navigate to the portal-admin directory

```sh
cd Synkork/portal-admin
```

### 2. Install dependencies

```sh
pnpm install
```

### 3. Configure environment variables

Copy the example file and fill in your values:

```sh
cp .env.example .env
```

Then edit `.env`:

```env
# Backend API base URL (without /api prefix)
VITE_SERVER_API_URL=http://localhost:8080

# API path prefix
VITE_SERVER_API_PREFIX=/api

# Request timeout in milliseconds
VITE_SERVER_API_TIMEOUT=5000
```

### 4. Start the development server

```sh
pnpm dev
```

The portal will be available at `http://localhost:5174`.

---

## Available Scripts

| Script | Description |
|---|---|
| `pnpm dev` | Start development server with hot-reload |
| `pnpm build` | Build the production bundle to `dist/` |
| `pnpm preview` | Preview the production build locally |
| `pnpm lint` | Run ESLint checks |
| `pnpm lint:fix` | Run ESLint and auto-fix issues |
| `pnpm test` | Run unit tests with Vitest |

---

## Docker Deployment

Build and serve the portal via Nginx using Docker:

```sh
# Build the image
docker build -t synkork-portal-admin .

# Or use Docker Compose
docker-compose up --build
```

The container serves the built static files with Nginx on port `5173`.

---

## Access Control

This portal is restricted to users with admin or manager roles. The backend enforces role-based access control (RBAC) on all `/manage/*` API endpoints. Attempting to access the portal without the appropriate role will result in a `403 Forbidden` response.

---

## Key Environment Variables Reference

| Variable | Description |
|---|---|
| `VITE_SERVER_API_URL` | Base URL of the Synkork backend API (e.g., `http://localhost:8080`) |
| `VITE_SERVER_API_PREFIX` | API path prefix, typically `/api` |
| `VITE_SERVER_API_TIMEOUT` | Axios request timeout in milliseconds |

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
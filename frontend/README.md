# Synkork — Frontend

The client-side application of the Synkork platform. Built with Vue 3 + TypeScript + Tailwind CSS, this app gives users access to all core collaboration features: real-time chat, group video calls, task boards, shared notes, and team calendars.

---

## Tech Stack

| Category | Technology |
|---|---|
| Framework | Vue 3 (Composition API) |
| Language | TypeScript |
| Build Tool | Vite |
| Styling | Tailwind CSS v4 |
| UI Components | Shadcn Vue / Reka UI |
| State Management | Pinia |
| Routing | Vue Router 4 |
| Real-time | WebSocket (STOMP via `@stomp/stompjs`) |
| Video / Voice Calls | ZegoCloud WebRTC (`@zegocloud/zego-uikit-prebuilt`) |
| HTTP Client | Axios |
| Animations | GSAP |
| Containerization | Docker + Nginx |

---

## Project Structure

```
frontend/
├── src/
│   ├── assets/             # Static assets (images, icons, fonts)
│   ├── components/         # Shared reusable UI components
│   ├── features/           # Feature-scoped logic & components
│   │   ├── auth/           # Login / register flows
│   │   ├── chats/          # Chat channel UI & message rendering
│   │   ├── voice-chat/     # Voice / video call interface
│   │   └── room-settings/  # Room configuration panel
│   ├── layouts/            # App shell and page layouts
│   ├── lib/                # Utility wrappers and shared config
│   ├── pages/              # Top-level route pages
│   │   ├── auth/           # Auth pages (login, register, etc.)
│   │   ├── LandingPage.vue # Public landing page
│   │   ├── MainPage.vue    # Main app shell (post-login)
│   │   ├── MePage.vue      # User profile & settings
│   │   ├── FriendPage.vue  # Friend list & requests
│   │   ├── InvitePage.vue  # Room invite handling
│   │   └── SubscriptionPage.vue  # Subscription & billing
│   ├── routers/            # Vue Router configuration & route guards
│   ├── services/           # API service layer (Axios calls)
│   ├── stores/             # Pinia state stores
│   │   ├── userStore.ts
│   │   ├── roomStore.ts
│   │   ├── spaceStore.ts
│   │   ├── taskStore.ts
│   │   ├── noteStore.ts
│   │   ├── calendarStore.ts
│   │   ├── friendStore.ts
│   │   ├── notificationStore.ts
│   │   └── themeStore.ts
│   ├── types/              # TypeScript type definitions
│   ├── utils/              # Helper functions & utilities
│   ├── style.css           # Global base styles
│   └── theme.css           # Design tokens & CSS variables
├── public/                 # Static public assets
├── .env                    # Local environment variables (gitignored)
├── Dockerfile              # Multi-stage Docker build (Node → Nginx)
├── docker-compose.yml      # Compose config for containerized run
├── vite.config.ts          # Vite configuration
└── package.json            # Dependencies and scripts
```

---

## Prerequisites

- **Node.js v22 or higher**
- **npm** (bundled with Node.js)

---

## Getting Started

### 1. Navigate to the frontend directory

```sh
cd Synkork/frontend
```

### 2. Install dependencies

```sh
npm install
```

### 3. Configure environment variables

Create a `.env` file in the `frontend/` directory:

```env
# Backend API base URL
VITE_BACKEND_URL=http://localhost:8080

# ZegoCloud — Video/Voice calls
VITE_ZEGO_APP_ID=your_zego_app_id
VITE_ZEGO_SERVER_URL=wss://your_zego_server_url/ws
```

### 4. Start the development server

```sh
npm run dev
```

The app will be available at `http://localhost:5173`.

---

## Available Scripts

| Script | Description |
|---|---|
| `npm run dev` | Start development server with hot-reload |
| `npm run build` | Build the production bundle to `dist/` |
| `npm run preview` | Preview the production build locally |

---

## Docker Deployment

Build and serve the app via Nginx using Docker:

```sh
# Build the image
docker build -t synkork-frontend .

# Or use Docker Compose
docker-compose up --build
```

The container serves the built static files with Nginx on port `5173`.

---

## Key Environment Variables Reference

| Variable | Description |
|---|---|
| `VITE_BACKEND_URL` | Base URL of the Synkork backend API (without `/api` prefix) |
| `VITE_ZEGO_APP_ID` | ZegoCloud application ID for WebRTC video/voice calls |
| `VITE_ZEGO_SERVER_URL` | ZegoCloud WebSocket server URL |

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
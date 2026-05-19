# Synkork Workspace

<p align="center">
  <strong>Personal repository for a collaborative workspace platform focused on real-time communication, scheduling, and AI-assisted workflows.</strong>
</p>

<p align="center">
  <a href="./README.vi.md">🇻🇳 Tiếng Việt</a>
  ·
  <a href="./README.en.md">🇺🇸 English</a>
  ·
  <a href="./README.zh-CN.md">🇨🇳 中文</a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Frontend-Vue%203%20%2B%20Vite-42b883?style=flat-square" alt="Frontend Vue 3 + Vite" />
  <img src="https://img.shields.io/badge/Backend-Spring%20Boot%203.5-6db33f?style=flat-square" alt="Backend Spring Boot 3.5" />
  <img src="https://img.shields.io/badge/Database-MySQL-4479a1?style=flat-square" alt="Database MySQL" />
  <img src="https://img.shields.io/badge/Realtime-WebSocket-f59e0b?style=flat-square" alt="Realtime WebSocket" />
  <img src="https://img.shields.io/badge/AI-OpenRouter%20%2F%20Gemini-111827?style=flat-square" alt="AI OpenRouter and Gemini" />
</p>

## Project Snapshot

Synkork is a team collaboration platform designed around shared rooms and functional spaces such as chat, notes, tasks, calls, and calendar scheduling.

This repository is my personal working copy of the broader project, curated to present both the full workspace architecture and my implementation focus areas:

- Calendar module
- LLM-powered event extraction and meeting summarization

## Why This Repository Exists

- Present a clean technical overview of the system
- Highlight my ownership in `calendar` and `llm`-related features
- Keep a personal version of the workspace for continued iteration

## Ownership Highlights

### Calendar

- Event CRUD by collaborative space
- Month, week, and year calendar views
- Recurring event expansion on the backend
- Conflict detection for overlapping schedules
- Realtime synchronization over WebSocket
- Event attendees and attachment-ready form flow
- Chat-to-calendar draft bridging from message suggestions

### LLM Functions

- Event extraction from Vietnamese chat messages
- Suggested calendar draft generation on the frontend
- Audio transcription pipeline for meeting voice notes
- Structured Vietnamese meeting summaries in JSON
- Integration path for OpenRouter-based models and Gemini configuration

## Architecture at a Glance

```text
Frontend (Vue 3)
  -> REST API (Spring Boot)
  -> WebSocket realtime updates
  -> Calendar suggestion bridge from chat

Backend (Spring Boot)
  -> MySQL persistence
  -> OAuth2 / JWT security
  -> Cloudinary file handling
  -> Zego video integration
  -> OpenRouter / Gemini-backed AI utilities
```

## Repository Map

```text
backend/       Spring Boot API, security, realtime, calendar, collaboration modules
frontend/      Main end-user application
portal-admin/  Admin portal based on shadcn-vue-admin
database/      Database assets and supporting schema work
llm/           AI integration notes, refactor summaries, implementation guides
```

## Read the Full Documentation

- [README.vi.md](./README.vi.md): full Vietnamese version
- [README.en.md](./README.en.md): full English version
- [README.zh-CN.md](./README.zh-CN.md): full Simplified Chinese version

## Attribution

This repository originates from a team graduation project and is maintained here as a personal technical presentation and development branch. The system overview remains faithful to the shared project, while the documentation emphasizes my implementation focus on calendar and LLM-assisted collaboration flows.

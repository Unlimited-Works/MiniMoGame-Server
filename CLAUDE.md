# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
sbt run       # Start the server
sbt test      # Run tests
sbt compile   # Compile only
```

First run takes longer due to dependency downloads.

## Configuration

- Default config: `src/main/resources/reference.conf`
- Override with `local.conf` or `online.conf` in the same directory
- Two network ports: `10001` (JSON protocol), `10002` (frame sync)
- Database: PostgreSQL at `localhost:5432`, database `game`
- Schema init: `game.sql`

## Architecture

Frame-synchronized multiplayer game server using DDD + reactive streams (Monix).

**Layer flow:**

```
Client → Network (dual-port) → Router → Entity/Service → DAO (PostgreSQL via Quill)
```

**Key layers:**

- `network/` — Dual-port server. `jsonsocket/` handles JSON protocol; `syncsocket/` handles frame sync. `jsession/` manages per-connection sessions.
- `route/` — Protocol handlers: `LoginRouter` → `LobbyRouter` → `TurnSyncInitRouter` → `TurnSyncRouter` → `SceneRouter`
- `entity/` — Domain state: `RoomEntity` (room lifecycle), `FrameEntity` (game loop + frame broadcast), `PlayerEntity`, `SceneEntity`
- `service/` — Domain logic: `UserService`, `RoomService`, `PositionService`, `SceneService`
- `dao/` — DB access via Quill: `UserDao`, `RoomDao`
- `rxsocket/` — Custom reactive socket library (Monix-based, actor-style sessions)

**Game session lifecycle:**

1. Login via `LoginRouter` → stores `UserInfo` in session
2. Room join/create via `LobbyRouter` → emits events via Monix `Observable`
3. Frame sync init via `TurnSyncInitRouter` → creates `FrameEntity` with game loop thread
4. Frame commands via `TurnSyncRouter` → `FrameEntity` broadcasts via `ReplaySubject`
5. Disconnect → `LobbyRouter` handles room leave and event stream cleanup

## Conventions (from README/codebase)

- Entity classes use `Entity` suffix; factory methods prefer `apply` over `create`/`fromXXX`
- Business errors use `BizException` + `BizCode`; return `Try`/`Either`/`Option` where possible
- Exception-throwing variants use `Ex` or `Ex2` suffix
- Check/validation functions start with `check`
- Session keys are case objects for type safety
- Concurrency: `ConcurrentHashMap` + `synchronized` for shared state; `JProtocol` guarantees per-user message ordering; domain layer must be concurrent-safe

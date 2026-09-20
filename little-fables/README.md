# Little Fables

Restaurant reservation website and kitchen display connected to the Sky Takeout backend.

## Run

```bash
npm run dev
```

Start the Spring Boot backend on `http://127.0.0.1:8080` first. The frontend server proxies `/api/*` to it. Set `BACKEND_URL` if the backend uses another address.

The site starts at:

```text
http://127.0.0.1:4173
```

## Views

- `/` or `/#home`: restaurant landing page and reservation flow.
- `/#kds`: kitchen display board with drag-and-drop ticket stages.

## Browser Storage

- `little-fables-bookings-v1`: reservations created in the current browser.

## Kitchen Display

Open `/#kds`, sign in with a backend admin account, and the board loads real orders in statuses:

- `2`: To accept
- `3`: Accepted
- `4`: Delivering

Buttons and drag-and-drop call the Spring Boot admin order APIs. Completed orders leave the board.

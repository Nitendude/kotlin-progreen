# XAMPP Backend Setup

This folder contains a PHP + MySQL backend for the Android app.

## 1. Copy API folder into XAMPP

Copy:

- `backend/progreen_api`

to:

- `C:\xampp\htdocs\progreen_api`

## 2. Create database

1. Open `http://localhost/phpmyadmin`
2. Import [schema.sql](c:/Users/John%20Philip%20Maniego/Desktop/kotlin-progreen/backend/schema.sql)

## 3. Configure database connection

Copy:

- `backend/progreen_api/config.sample.php`

to:

- `backend/progreen_api/config.php`

Then set your MySQL credentials.

## 4. Point Android app to backend

In `local.properties` add:

```properties
API_BASE_URL=http://10.0.2.2/progreen_api/
```

Use `10.0.2.2` for Android emulator.

If using a real phone on the same Wi-Fi, use your PC LAN IP, for example:

```properties
API_BASE_URL=http://192.168.1.20/progreen_api/
```

## 5. Seed first LGU/Admin account

Use phpMyAdmin to insert a user manually, or register one through the app and then update `role` in the `users` table to `LGU` or `ADMIN`.

## Notes

- The app now expects real backend auth and data.
- Reward images are stored as Base64 in MySQL for simplicity. For production, move them to file storage.
- Session tokens are stored in `user_sessions`.

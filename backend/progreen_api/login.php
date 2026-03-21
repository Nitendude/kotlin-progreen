<?php

require_once __DIR__ . '/helpers.php';

$body = json_body();
$email = strtolower(trim($body['email'] ?? ''));
$password = $body['password'] ?? '';

if (!filter_var($email, FILTER_VALIDATE_EMAIL) || $password === '') {
    respond(false, null, 'Email and password are required', 422);
}

$stmt = db()->prepare("SELECT id, name, email, role, points, password_hash FROM users WHERE email = :email LIMIT 1");
$stmt->execute(['email' => $email]);
$user = $stmt->fetch();

if (!$user || !password_verify($password, $user['password_hash'])) {
    respond(false, null, 'Invalid credentials', 401);
}

$token = create_session_token((int) $user['id']);

respond(true, [
    'token' => $token,
    'user' => user_payload($user),
]);

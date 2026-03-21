<?php

require_once __DIR__ . '/helpers.php';

$body = json_body();
$name = trim($body['name'] ?? '');
$email = strtolower(trim($body['email'] ?? ''));
$password = $body['password'] ?? '';
$role = strtoupper(trim($body['role'] ?? 'USER'));

if ($name === '' || !filter_var($email, FILTER_VALIDATE_EMAIL) || strlen($password) < 6) {
    respond(false, null, 'Provide a valid name, email, and password with at least 6 characters', 422);
}

if (!in_array($role, ['USER', 'LGU', 'COMPANY', 'ADMIN'], true)) {
    $role = 'USER';
}

$check = db()->prepare("SELECT id FROM users WHERE email = :email LIMIT 1");
$check->execute(['email' => $email]);
if ($check->fetch()) {
    respond(false, null, 'Email already registered', 409);
}

$insert = db()->prepare(
    "INSERT INTO users (name, email, password_hash, role, points, is_verified) VALUES (:name, :email, :password_hash, :role, 0, 0)"
);
$insert->execute([
    'name' => $name,
    'email' => $email,
    'password_hash' => password_hash($password, PASSWORD_DEFAULT),
    'role' => $role,
]);

$userId = (int) db()->lastInsertId();
$otp = upsert_email_verification($userId);

if (!send_verification_email($email, $name, $otp)) {
    respond(false, null, 'Account created, but OTP email could not be sent. Configure XAMPP/PHP mail first.', 500);
}

respond(true, [
    'email' => $email,
    'message' => 'OTP sent to your email address',
]);

<?php

require_once __DIR__ . '/helpers.php';

$body = json_body();
$email = strtolower(trim($body['email'] ?? ''));

if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
    respond(false, null, 'Valid email is required', 422);
}

$stmt = db()->prepare("SELECT id, name, email, is_verified FROM users WHERE email = :email LIMIT 1");
$stmt->execute(['email' => $email]);
$user = $stmt->fetch();

if (!$user) {
    respond(false, null, 'Account not found', 404);
}

if ((int) $user['is_verified'] === 1) {
    respond(false, null, 'Email is already verified', 422);
}

$otp = upsert_email_verification((int) $user['id']);

if (!send_verification_email($user['email'], $user['name'], $otp)) {
    respond(false, null, 'Could not resend OTP email. Configure XAMPP/PHP mail first.', 500);
}

respond(true, [
    'email' => $user['email'],
    'message' => 'OTP resent successfully',
]);

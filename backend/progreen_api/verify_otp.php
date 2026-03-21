<?php

require_once __DIR__ . '/helpers.php';

$body = json_body();
$email = strtolower(trim($body['email'] ?? ''));
$otp = trim($body['otp'] ?? '');

if (!filter_var($email, FILTER_VALIDATE_EMAIL) || $otp === '') {
    respond(false, null, 'Email and OTP are required', 422);
}

$stmt = db()->prepare("SELECT id, name, email, role, points FROM users WHERE email = :email LIMIT 1");
$stmt->execute(['email' => $email]);
$user = $stmt->fetch();

if (!$user) {
    respond(false, null, 'Account not found', 404);
}

$verifyStmt = db()->prepare(
    "SELECT otp_code, expires_at
     FROM email_verifications
     WHERE user_id = :user_id
     ORDER BY created_at DESC
     LIMIT 1"
);
$verifyStmt->execute(['user_id' => $user['id']]);
$verification = $verifyStmt->fetch();

if (!$verification) {
    respond(false, null, 'No OTP found for this account', 404);
}

if ($verification['otp_code'] !== $otp) {
    respond(false, null, 'Invalid OTP', 422);
}

if (strtotime($verification['expires_at']) < time()) {
    respond(false, null, 'OTP expired. Request a new one.', 422);
}

$pdo = db();
$pdo->beginTransaction();

try {
    $update = $pdo->prepare("UPDATE users SET is_verified = 1 WHERE id = :id");
    $update->execute(['id' => $user['id']]);

    $deleteStmt = $pdo->prepare("DELETE FROM email_verifications WHERE user_id = :user_id");
    $deleteStmt->execute(['user_id' => $user['id']]);

    $token = create_session_token((int) $user['id']);

    $pdo->commit();

    respond(true, [
        'token' => $token,
        'user' => user_payload($user),
    ]);
} catch (Throwable $e) {
    $pdo->rollBack();
    respond(false, null, 'Could not verify OTP', 500);
}

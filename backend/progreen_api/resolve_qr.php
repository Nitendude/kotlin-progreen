<?php

require_once __DIR__ . '/helpers.php';

$actor = require_auth();
require_role($actor, ['LGU', 'ADMIN']);

$body = json_body();
$payload = trim($body['qr_payload'] ?? '');
$email = extract_email_from_qr($payload);

if (!$email) {
    respond(false, null, 'Invalid QR payload', 422);
}

$stmt = db()->prepare("SELECT name, email, role FROM users WHERE email = :email LIMIT 1");
$stmt->execute(['email' => $email]);
$user = $stmt->fetch();

if (!$user || $user['role'] !== 'USER') {
    respond(false, null, 'User account not found', 404);
}

respond(true, [
    'name' => $user['name'],
    'email' => $user['email'],
]);

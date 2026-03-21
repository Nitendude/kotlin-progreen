<?php

require_once __DIR__ . '/helpers.php';

$actor = require_auth();
require_role($actor, ['LGU', 'ADMIN']);

$body = json_body();
$payload = trim($body['qr_payload'] ?? '');
$categoryId = trim($body['category_id'] ?? '');
$weightKg = (float) ($body['weight_kg'] ?? 0);

if ($payload === '' || $categoryId === '' || $weightKg <= 0) {
    respond(false, null, 'QR payload, category, and weight are required', 422);
}

$email = extract_email_from_qr($payload);
if (!$email) {
    respond(false, null, 'Invalid QR payload', 422);
}

$userStmt = db()->prepare("SELECT id, name, email, role, points FROM users WHERE email = :email LIMIT 1");
$userStmt->execute(['email' => $email]);
$user = $userStmt->fetch();

if (!$user || $user['role'] !== 'USER') {
    respond(false, null, 'User account not found', 404);
}

$catStmt = db()->prepare("SELECT id, name, points_per_kg FROM categories WHERE id = :id LIMIT 1");
$catStmt->execute(['id' => $categoryId]);
$category = $catStmt->fetch();

if (!$category) {
    respond(false, null, 'Category not found', 404);
}

$pointsEarned = (int) floor($weightKg * (int) $category['points_per_kg']);
$pdo = db();
$pdo->beginTransaction();

try {
    $donation = $pdo->prepare(
        "INSERT INTO donations (user_id, lgu_user_id, category_id, weight_kg, points_earned, notes)
         VALUES (:user_id, :lgu_user_id, :category_id, :weight_kg, :points_earned, :notes)"
    );
    $donation->execute([
        'user_id' => $user['id'],
        'lgu_user_id' => $actor['id'],
        'category_id' => $category['id'],
        'weight_kg' => $weightKg,
        'points_earned' => $pointsEarned,
        'notes' => 'Recorded by LGU ' . $actor['name'],
    ]);

    $update = $pdo->prepare("UPDATE users SET points = points + :points WHERE id = :id");
    $update->execute([
        'points' => $pointsEarned,
        'id' => $user['id'],
    ]);

    $pointsStmt = $pdo->prepare("SELECT points FROM users WHERE id = :id");
    $pointsStmt->execute(['id' => $user['id']]);
    $newBalance = (int) $pointsStmt->fetchColumn();

    $pdo->commit();

    respond(true, [
        'user_name' => $user['name'],
        'user_email' => $user['email'],
        'points_earned' => $pointsEarned,
        'new_balance' => $newBalance,
    ]);
} catch (Throwable $e) {
    $pdo->rollBack();
    respond(false, null, 'Could not credit donation', 500);
}

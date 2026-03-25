<?php

require_once __DIR__ . '/helpers.php';

$user = require_auth();
$body = json_body();
$rewardId = trim($body['reward_id'] ?? '');

if ($rewardId === '') {
    respond(false, null, 'Reward is required', 422);
}

$rewardStmt = db()->prepare("SELECT id, cost_points, redeem_code FROM rewards WHERE id = :id AND is_active = 1 LIMIT 1");
$rewardStmt->execute(['id' => $rewardId]);
$reward = $rewardStmt->fetch();

if (!$reward) {
    respond(false, null, 'Reward not found', 404);
}

$costPoints = (int) $reward['cost_points'];
if ((int) $user['points'] < $costPoints) {
    respond(false, null, 'Not enough points', 422);
}

$pdo = db();
$pdo->beginTransaction();

try {
    $claimToken = 'CLM-' . strtoupper(bin2hex(random_bytes(4)));

    $update = $pdo->prepare("UPDATE users SET points = points - :points WHERE id = :user_id");
    $update->execute([
        'points' => $costPoints,
        'user_id' => $user['id'],
    ]);

    $redemption = $pdo->prepare(
        "INSERT INTO reward_redemptions (user_id, reward_id, redeem_code, claim_token, points_spent)
         VALUES (:user_id, :reward_id, :redeem_code, :claim_token, :points_spent)"
    );
    $redemption->execute([
        'user_id' => $user['id'],
        'reward_id' => $rewardId,
        'redeem_code' => $reward['redeem_code'],
        'claim_token' => $claimToken,
        'points_spent' => $costPoints,
    ]);

    $pointsStmt = $pdo->prepare("SELECT points FROM users WHERE id = :id");
    $pointsStmt->execute(['id' => $user['id']]);
    $newPoints = (int) $pointsStmt->fetchColumn();

    $pdo->commit();

    respond(true, [
        'redeem_code' => $reward['redeem_code'],
        'claim_token' => $claimToken,
        'new_points' => $newPoints,
    ]);
} catch (Throwable $e) {
    $pdo->rollBack();
    respond(false, null, 'Could not redeem reward', 500);
}

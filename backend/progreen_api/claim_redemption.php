<?php

require_once __DIR__ . '/helpers.php';

$actor = require_auth();
require_role($actor, ['LGU', 'ADMIN']);

$body = json_body();
$claimToken = trim($body['claim_token'] ?? '');

if ($claimToken === '') {
    respond(false, null, 'Claim token is required', 422);
}

$sql = "SELECT rr.id, rr.status, rr.claim_token, u.name AS user_name, u.email AS user_email, r.title AS reward_title
        FROM reward_redemptions rr
        INNER JOIN users u ON u.id = rr.user_id
        INNER JOIN rewards r ON r.id = rr.reward_id
        WHERE rr.claim_token = :claim_token
        LIMIT 1";
$stmt = db()->prepare($sql);
$stmt->execute(['claim_token' => $claimToken]);
$record = $stmt->fetch();

if (!$record) {
    respond(false, null, 'Claim token not found', 404);
}

if ($record['status'] !== 'PENDING') {
    respond(false, null, 'Reward already claimed or cancelled', 422);
}

$update = db()->prepare(
    "UPDATE reward_redemptions
     SET status = 'CLAIMED', claimed_by_lgu_user_id = :claimed_by, claimed_at = NOW()
     WHERE id = :id"
);
$update->execute([
    'claimed_by' => $actor['id'],
    'id' => $record['id'],
]);

respond(true, [
    'message' => 'Reward claim validated',
    'user_name' => $record['user_name'],
    'user_email' => $record['user_email'],
    'reward_title' => $record['reward_title'],
]);

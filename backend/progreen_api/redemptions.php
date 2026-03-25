<?php

require_once __DIR__ . '/helpers.php';

$user = require_auth();

$sql = "SELECT r.title AS reward_title, rr.redeem_code, rr.claim_token, rr.status, rr.points_spent,
               provider.name AS provider_name, UNIX_TIMESTAMP(rr.created_at) * 1000 AS timestamp
        FROM reward_redemptions rr
        INNER JOIN rewards r ON r.id = rr.reward_id
        LEFT JOIN users provider ON provider.id = r.provider_user_id
        WHERE rr.user_id = :user_id
        ORDER BY rr.created_at DESC";
$stmt = db()->prepare($sql);
$stmt->execute(['user_id' => $user['id']]);
$rows = $stmt->fetchAll();

$items = array_map(static function ($row) {
    return [
        'reward_title' => $row['reward_title'],
        'provider_name' => $row['provider_name'],
        'redeem_code' => $row['redeem_code'],
        'claim_token' => $row['claim_token'],
        'status' => $row['status'],
        'points_spent' => (int) $row['points_spent'],
        'timestamp' => (int) $row['timestamp'],
    ];
}, $rows);

respond(true, $items);

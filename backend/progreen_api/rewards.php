<?php

require_once __DIR__ . '/helpers.php';

require_auth();

$sql = "SELECT r.id, r.title, r.cost_points, r.reward_type, r.description, r.image_base64, r.redeem_code,
               provider.name AS provider
        FROM rewards r
        LEFT JOIN users provider ON provider.id = r.provider_user_id
        WHERE r.is_active = 1
        ORDER BY r.cost_points ASC, r.created_at DESC";
$stmt = db()->query($sql);
$rows = $stmt->fetchAll();

$items = array_map(static function ($row) {
    return [
        'id' => $row['id'],
        'title' => $row['title'],
        'cost_points' => (int) $row['cost_points'],
        'provider' => $row['provider'],
        'reward_type' => $row['reward_type'],
        'description' => $row['description'],
        'image_base64' => $row['image_base64'],
        'redeem_code' => $row['redeem_code'],
    ];
}, $rows);

respond(true, $items);

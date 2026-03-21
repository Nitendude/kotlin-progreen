<?php

require_once __DIR__ . '/helpers.php';

$user = require_auth();

$sql = "SELECT d.category_id, c.name AS category_name, d.weight_kg, d.points_earned, d.notes,
               UNIX_TIMESTAMP(d.created_at) * 1000 AS timestamp
        FROM donations d
        INNER JOIN categories c ON c.id = d.category_id
        WHERE d.user_id = :user_id
        ORDER BY d.created_at DESC";
$stmt = db()->prepare($sql);
$stmt->execute(['user_id' => $user['id']]);
$rows = $stmt->fetchAll();

$items = array_map(static function ($row) {
    return [
        'category_id' => $row['category_id'],
        'category_name' => $row['category_name'],
        'weight_kg' => (float) $row['weight_kg'],
        'points_earned' => (int) $row['points_earned'],
        'notes' => $row['notes'] ?? '',
        'timestamp' => (int) $row['timestamp'],
    ];
}, $rows);

respond(true, $items);

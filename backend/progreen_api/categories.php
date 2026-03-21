<?php

require_once __DIR__ . '/helpers.php';

$stmt = db()->query("SELECT id, name, description, points_per_kg FROM categories ORDER BY sort_order ASC, name ASC");
$items = array_map(static function ($row) {
    return [
        'id' => $row['id'],
        'name' => $row['name'],
        'description' => $row['description'],
        'points_per_kg' => (int) $row['points_per_kg'],
    ];
}, $stmt->fetchAll());

respond(true, $items);

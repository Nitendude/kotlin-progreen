<?php

require_once __DIR__ . '/helpers.php';

require_auth();

$stmt = db()->query("SELECT name, address, distance_km, map_query FROM lgu_sites ORDER BY distance_km ASC, name ASC");
$items = array_map(static function ($row) {
    return [
        'name' => $row['name'],
        'address' => $row['address'],
        'distance_km' => (float) $row['distance_km'],
        'map_query' => $row['map_query'],
    ];
}, $stmt->fetchAll());

respond(true, $items);

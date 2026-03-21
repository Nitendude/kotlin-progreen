<?php

require_once __DIR__ . '/helpers.php';

$actor = require_auth();
require_role($actor, ['LGU', 'ADMIN']);

$statsStmt = db()->prepare(
    "SELECT COALESCE(SUM(weight_kg), 0) AS donated_kg_total,
            COALESCE(SUM(points_earned), 0) AS points_credited_total,
            COUNT(*) AS donations_count
     FROM donations
     WHERE lgu_user_id = :lgu_user_id"
);
$statsStmt->execute(['lgu_user_id' => $actor['id']]);
$stats = $statsStmt->fetch();

$rewardCountStmt = db()->prepare("SELECT COUNT(*) FROM rewards WHERE provider_user_id = :provider_user_id");
$rewardCountStmt->execute(['provider_user_id' => $actor['id']]);
$rewardsCount = (int) $rewardCountStmt->fetchColumn();

$recordsStmt = db()->prepare(
    "SELECT u.name AS user_name, u.email AS user_email, c.name AS category_name, d.weight_kg,
            d.points_earned, UNIX_TIMESTAMP(d.created_at) * 1000 AS timestamp
     FROM donations d
     INNER JOIN users u ON u.id = d.user_id
     INNER JOIN categories c ON c.id = d.category_id
     WHERE d.lgu_user_id = :lgu_user_id
     ORDER BY d.created_at DESC
     LIMIT 20"
);
$recordsStmt->execute(['lgu_user_id' => $actor['id']]);
$records = array_map(static function ($row) {
    return [
        'user_name' => $row['user_name'],
        'user_email' => $row['user_email'],
        'category_name' => $row['category_name'],
        'weight_kg' => (float) $row['weight_kg'],
        'points_earned' => (int) $row['points_earned'],
        'timestamp' => (int) $row['timestamp'],
    ];
}, $recordsStmt->fetchAll());

$rewardsStmt = db()->prepare(
    "SELECT id, title, cost_points, reward_type, description, image_base64, redeem_code
     FROM rewards
     WHERE provider_user_id = :provider_user_id
     ORDER BY created_at DESC"
);
$rewardsStmt->execute(['provider_user_id' => $actor['id']]);
$managedRewards = array_map(static function ($row) use ($actor) {
    return [
        'id' => $row['id'],
        'title' => $row['title'],
        'cost_points' => (int) $row['cost_points'],
        'provider' => $actor['name'],
        'reward_type' => $row['reward_type'],
        'description' => $row['description'],
        'image_base64' => $row['image_base64'],
        'redeem_code' => $row['redeem_code'],
    ];
}, $rewardsStmt->fetchAll());

respond(true, [
    'stats' => [
        'donated_kg_total' => (float) $stats['donated_kg_total'],
        'points_credited_total' => (int) $stats['points_credited_total'],
        'donations_count' => (int) $stats['donations_count'],
        'rewards_count' => $rewardsCount,
    ],
    'records' => $records,
    'managed_rewards' => $managedRewards,
]);

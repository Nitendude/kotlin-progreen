<?php

require_once __DIR__ . '/helpers.php';

$actor = require_auth();
require_role($actor, ['COMPANY', 'ADMIN']);

$statsStmt = db()->prepare(
    "SELECT COUNT(*) AS active_campaigns
     FROM rewards
     WHERE provider_user_id = :provider_user_id AND is_active = 1"
);
$statsStmt->execute(['provider_user_id' => $actor['id']]);
$activeCampaigns = (int) $statsStmt->fetchColumn();

$redemptionsStmt = db()->prepare(
    "SELECT COUNT(*) AS total_redemptions
     FROM reward_redemptions rr
     INNER JOIN rewards r ON r.id = rr.reward_id
     WHERE r.provider_user_id = :provider_user_id"
);
$redemptionsStmt->execute(['provider_user_id' => $actor['id']]);
$totalRedemptions = (int) $redemptionsStmt->fetchColumn();

$pendingClaimsStmt = db()->prepare(
    "SELECT COUNT(*) AS pending_claims
     FROM reward_redemptions rr
     INNER JOIN rewards r ON r.id = rr.reward_id
     WHERE r.provider_user_id = :provider_user_id AND rr.status = 'PENDING'"
);
$pendingClaimsStmt->execute(['provider_user_id' => $actor['id']]);
$pendingClaims = (int) $pendingClaimsStmt->fetchColumn();

$recentStmt = db()->prepare(
    "SELECT u.name AS user_name, u.email AS user_email, r.title AS reward_title,
            rr.claim_token, rr.status, UNIX_TIMESTAMP(rr.created_at) * 1000 AS timestamp
     FROM reward_redemptions rr
     INNER JOIN rewards r ON r.id = rr.reward_id
     INNER JOIN users u ON u.id = rr.user_id
     WHERE r.provider_user_id = :provider_user_id
     ORDER BY rr.created_at DESC
     LIMIT 20"
);
$recentStmt->execute(['provider_user_id' => $actor['id']]);
$recentRedemptions = array_map(static function ($row) {
    return [
        'user_name' => $row['user_name'],
        'user_email' => $row['user_email'],
        'reward_title' => $row['reward_title'],
        'claim_token' => $row['claim_token'],
        'status' => $row['status'],
        'timestamp' => (int) $row['timestamp'],
    ];
}, $recentStmt->fetchAll());

respond(true, [
    'stats' => [
        'active_campaigns' => $activeCampaigns,
        'total_redemptions' => $totalRedemptions,
        'pending_claims' => $pendingClaims,
    ],
    'recent_redemptions' => $recentRedemptions,
]);

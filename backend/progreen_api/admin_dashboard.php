<?php

require_once __DIR__ . '/helpers.php';

$actor = require_auth();
require_role($actor, ['ADMIN']);

$userCount = (int) db()->query("SELECT COUNT(*) FROM users")->fetchColumn();
$lguCount = (int) db()->query("SELECT COUNT(*) FROM users WHERE role = 'LGU'")->fetchColumn();
$companyCount = (int) db()->query("SELECT COUNT(*) FROM users WHERE role = 'COMPANY'")->fetchColumn();
$pendingCount = (int) db()->query("SELECT COUNT(*) FROM users WHERE approval_status = 'PENDING'")->fetchColumn();

$pendingStmt = db()->query(
    "SELECT id, name, email, role, approval_status, is_verified, UNIX_TIMESTAMP(created_at) * 1000 AS created_at
     FROM users
     WHERE approval_status = 'PENDING'
     ORDER BY created_at DESC
     LIMIT 20"
);
$pendingAccounts = array_map(static function ($row) {
    return [
        'id' => (int) $row['id'],
        'name' => $row['name'],
        'email' => $row['email'],
        'role' => $row['role'],
        'approval_status' => $row['approval_status'],
        'is_verified' => (int) $row['is_verified'] === 1,
        'created_at' => (int) $row['created_at'],
    ];
}, $pendingStmt->fetchAll());

respond(true, [
    'stats' => [
        'users_count' => $userCount,
        'lgus_count' => $lguCount,
        'companies_count' => $companyCount,
        'pending_count' => $pendingCount,
    ],
    'pending_accounts' => $pendingAccounts,
]);

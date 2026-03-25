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

$applicationsStmt = db()->query(
    "SELECT ra.id, ra.application_type, ra.organization_name, ra.office_address, ra.contact_person,
            ra.contact_email, ra.document_name, ra.status, u.name AS applicant_name, u.email AS applicant_email,
            u.is_verified, UNIX_TIMESTAMP(ra.created_at) * 1000 AS created_at
     FROM role_applications ra
     INNER JOIN users u ON u.id = ra.user_id
     WHERE ra.status = 'PENDING'
     ORDER BY ra.created_at DESC
     LIMIT 20"
);
$pendingApplications = array_map(static function ($row) {
    return [
        'id' => (int) $row['id'],
        'application_type' => $row['application_type'],
        'organization_name' => $row['organization_name'],
        'office_address' => $row['office_address'],
        'contact_person' => $row['contact_person'],
        'contact_email' => $row['contact_email'],
        'document_name' => $row['document_name'],
        'status' => $row['status'],
        'applicant_name' => $row['applicant_name'],
        'applicant_email' => $row['applicant_email'],
        'is_verified' => (int) $row['is_verified'] === 1,
        'created_at' => (int) $row['created_at'],
    ];
}, $applicationsStmt->fetchAll());

respond(true, [
    'stats' => [
        'users_count' => $userCount,
        'lgus_count' => $lguCount,
        'companies_count' => $companyCount,
        'pending_count' => $pendingCount,
    ],
    'pending_accounts' => $pendingAccounts,
    'pending_applications' => $pendingApplications,
]);

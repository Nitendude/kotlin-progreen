<?php

require_once __DIR__ . '/helpers.php';

$actor = require_auth();
require_role($actor, ['ADMIN']);

$body = json_body();
$userId = (int) ($body['user_id'] ?? 0);
$approvalStatus = strtoupper(trim($body['approval_status'] ?? ''));

if ($userId <= 0 || !in_array($approvalStatus, ['APPROVED', 'REJECTED', 'PENDING'], true)) {
    respond(false, null, 'User and approval status are required', 422);
}

$stmt = db()->prepare("UPDATE users SET approval_status = :approval_status WHERE id = :id");
$stmt->execute([
    'approval_status' => $approvalStatus,
    'id' => $userId,
]);

respond(true, [
    'user_id' => $userId,
    'approval_status' => $approvalStatus,
]);

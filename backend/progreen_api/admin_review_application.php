<?php

require_once __DIR__ . '/helpers.php';

$actor = require_auth();
require_role($actor, ['ADMIN']);

$body = json_body();
$applicationId = (int) ($body['application_id'] ?? 0);
$decision = strtoupper(trim($body['decision'] ?? ''));

if ($applicationId <= 0 || !in_array($decision, ['APPROVED', 'REJECTED'], true)) {
    respond(false, null, 'Application id and valid decision are required', 422);
}

$stmt = db()->prepare(
    "SELECT ra.id, ra.user_id, ra.status
     FROM role_applications ra
     WHERE ra.id = :id
     LIMIT 1"
);
$stmt->execute(['id' => $applicationId]);
$application = $stmt->fetch();

if (!$application) {
    respond(false, null, 'Application not found', 404);
}

if ($application['status'] !== 'PENDING') {
    respond(false, null, 'Application already reviewed', 422);
}

$pdo = db();
$pdo->beginTransaction();

try {
    $updateApplication = $pdo->prepare(
        "UPDATE role_applications
         SET status = :status, reviewed_by_user_id = :reviewed_by, reviewed_at = NOW()
         WHERE id = :id"
    );
    $updateApplication->execute([
        'status' => $decision,
        'reviewed_by' => $actor['id'],
        'id' => $applicationId,
    ]);

    $updateUser = $pdo->prepare(
        "UPDATE users SET approval_status = :approval_status WHERE id = :user_id"
    );
    $updateUser->execute([
        'approval_status' => $decision,
        'user_id' => $application['user_id'],
    ]);

    $pdo->commit();

    respond(true, [
        'application_id' => $applicationId,
        'decision' => $decision,
    ]);
} catch (Throwable $e) {
    $pdo->rollBack();
    respond(false, null, 'Could not review application', 500);
}

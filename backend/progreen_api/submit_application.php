<?php

require_once __DIR__ . '/helpers.php';

$body = json_body();
$name = trim($body['name'] ?? '');
$email = strtolower(trim($body['email'] ?? ''));
$password = $body['password'] ?? '';
$applicationType = strtoupper(trim($body['application_type'] ?? ''));
$organizationName = trim($body['organization_name'] ?? '');
$officeAddress = trim($body['office_address'] ?? '');
$contactPerson = trim($body['contact_person'] ?? '');
$contactEmail = strtolower(trim($body['contact_email'] ?? ''));
$documentName = trim($body['document_name'] ?? '');
$documentBase64 = $body['document_base64'] ?? null;

if (
    $name === '' ||
    !filter_var($email, FILTER_VALIDATE_EMAIL) ||
    strlen($password) < 6 ||
    !in_array($applicationType, ['LGU', 'COMPANY'], true) ||
    $organizationName === '' ||
    $officeAddress === '' ||
    $contactPerson === '' ||
    !filter_var($contactEmail, FILTER_VALIDATE_EMAIL)
) {
    respond(false, null, 'Complete all required application fields with valid emails.', 422);
}

$check = db()->prepare("SELECT id FROM users WHERE email = :email LIMIT 1");
$check->execute(['email' => $email]);
if ($check->fetch()) {
    respond(false, null, 'Email already registered', 409);
}

$pdo = db();
$pdo->beginTransaction();

try {
    $insertUser = $pdo->prepare(
        "INSERT INTO users (name, email, password_hash, role, points, is_verified, approval_status)
         VALUES (:name, :email, :password_hash, :role, 0, 0, 'PENDING')"
    );
    $insertUser->execute([
        'name' => $name,
        'email' => $email,
        'password_hash' => password_hash($password, PASSWORD_DEFAULT),
        'role' => $applicationType,
    ]);

    $userId = (int) $pdo->lastInsertId();

    $insertApplication = $pdo->prepare(
        "INSERT INTO role_applications
         (user_id, application_type, organization_name, office_address, contact_person, contact_email, document_name, document_base64, status)
         VALUES
         (:user_id, :application_type, :organization_name, :office_address, :contact_person, :contact_email, :document_name, :document_base64, 'PENDING')"
    );
    $insertApplication->execute([
        'user_id' => $userId,
        'application_type' => $applicationType,
        'organization_name' => $organizationName,
        'office_address' => $officeAddress,
        'contact_person' => $contactPerson,
        'contact_email' => $contactEmail,
        'document_name' => $documentName !== '' ? $documentName : null,
        'document_base64' => $documentBase64,
    ]);

    $otp = upsert_email_verification($userId);
    if (!send_verification_email($email, $name, $otp)) {
        throw new RuntimeException('Could not send OTP email');
    }

    $pdo->commit();

    respond(true, [
        'email' => $email,
        'message' => 'Application submitted. Verify email with OTP, then wait for admin approval.',
    ]);
} catch (Throwable $e) {
    $pdo->rollBack();
    respond(false, null, 'Could not submit application. Check SMTP/database setup.', 500);
}

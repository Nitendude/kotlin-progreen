<?php

require_once __DIR__ . '/db.php';

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Headers: Content-Type, Authorization');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(204);
    exit;
}

function respond(bool $success, $data = null, ?string $message = null, int $status = 200): void
{
    http_response_code($status);
    echo json_encode([
        'success' => $success,
        'message' => $message,
        'data' => $data,
    ]);
    exit;
}

function json_body(): array
{
    $raw = file_get_contents('php://input');
    if (!$raw) {
        return [];
    }

    $decoded = json_decode($raw, true);
    return is_array($decoded) ? $decoded : [];
}

function bearer_token(): ?string
{
    $header = $_SERVER['HTTP_AUTHORIZATION'] ?? '';
    if (preg_match('/Bearer\s+(.+)/i', $header, $matches)) {
        return trim($matches[1]);
    }
    return null;
}

function require_auth(): array
{
    $token = bearer_token();
    if (!$token) {
        respond(false, null, 'Missing authorization token', 401);
    }

    $sql = "SELECT u.id, u.name, u.email, u.role, u.points
            FROM user_sessions s
            INNER JOIN users u ON u.id = s.user_id
            WHERE s.token = :token AND s.expires_at > NOW()";
    $stmt = db()->prepare($sql);
    $stmt->execute(['token' => $token]);
    $user = $stmt->fetch();

    if (!$user) {
        respond(false, null, 'Invalid or expired session', 401);
    }

    return $user;
}

function require_role(array $user, array $allowedRoles): void
{
    if (!in_array($user['role'], $allowedRoles, true)) {
        respond(false, null, 'You do not have permission for this action', 403);
    }
}

function create_session_token(int $userId): string
{
    $token = bin2hex(random_bytes(32));
    $stmt = db()->prepare(
        "INSERT INTO user_sessions (user_id, token, expires_at) VALUES (:user_id, :token, DATE_ADD(NOW(), INTERVAL 30 DAY))"
    );
    $stmt->execute([
        'user_id' => $userId,
        'token' => $token,
    ]);
    return $token;
}

function upsert_email_verification(int $userId): string
{
    $otp = (string) random_int(100000, 999999);

    $deleteStmt = db()->prepare("DELETE FROM email_verifications WHERE user_id = :user_id");
    $deleteStmt->execute(['user_id' => $userId]);

    $stmt = db()->prepare(
        "INSERT INTO email_verifications (user_id, otp_code, expires_at)
         VALUES (:user_id, :otp_code, DATE_ADD(NOW(), INTERVAL 10 MINUTE))"
    );
    $stmt->execute([
        'user_id' => $userId,
        'otp_code' => $otp,
    ]);

    return $otp;
}

function send_verification_email(string $email, string $name, string $otp): bool
{
    $subject = 'CycleMint Email Verification Code';
    $message = "Hello {$name},\r\n\r\nYour CycleMint OTP is: {$otp}\r\n\r\nThis code expires in 10 minutes.";
    $headers = "From: noreply@cyclemint.local\r\n";

    return mail($email, $subject, $message, $headers);
}

function user_payload(array $user): array
{
    $stmt = db()->prepare("SELECT COUNT(*) FROM donations WHERE user_id = :user_id");
    $stmt->execute(['user_id' => $user['id']]);

    return [
        'id' => (int) $user['id'],
        'name' => $user['name'],
        'email' => $user['email'],
        'role' => $user['role'],
        'points' => (int) $user['points'],
        'submission_count' => (int) $stmt->fetchColumn(),
    ];
}

function extract_email_from_qr(string $payload): ?string
{
    $payload = trim($payload);
    if (strpos($payload, '|') !== false) {
        $parts = explode('|', $payload);
        if (count($parts) >= 4 && filter_var($parts[3], FILTER_VALIDATE_EMAIL)) {
            return strtolower($parts[3]);
        }
    }

    if (preg_match('/[A-Z0-9._%+\-]+@[A-Z0-9.\-]+\.[A-Z]{2,}/i', $payload, $matches)) {
        return strtolower($matches[0]);
    }

    return null;
}

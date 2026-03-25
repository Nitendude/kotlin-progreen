<?php

require_once __DIR__ . '/helpers.php';

$actor = require_auth();
require_role($actor, ['LGU', 'COMPANY', 'ADMIN']);

$body = json_body();
$title = trim($body['title'] ?? '');
$costPoints = (int) ($body['cost_points'] ?? 0);
$rewardType = trim($body['reward_type'] ?? 'Item');
$description = trim($body['description'] ?? '');
$imageBase64 = $body['image_base64'] ?? null;
$redeemCode = trim($body['redeem_code'] ?? '');

if ($title === '' || $description === '' || $costPoints <= 0) {
    respond(false, null, 'Title, description, and positive points are required', 422);
}

$rewardId = 'reward_' . bin2hex(random_bytes(8));
$stmt = db()->prepare(
    "INSERT INTO rewards (id, provider_user_id, title, cost_points, reward_type, description, image_base64, redeem_code, is_active)
     VALUES (:id, :provider_user_id, :title, :cost_points, :reward_type, :description, :image_base64, :redeem_code, 1)"
);
$stmt->execute([
    'id' => $rewardId,
    'provider_user_id' => $actor['id'],
    'title' => $title,
    'cost_points' => $costPoints,
    'reward_type' => $rewardType,
    'description' => $description,
    'image_base64' => $imageBase64,
    'redeem_code' => $redeemCode !== '' ? $redeemCode : null,
]);

respond(true, [
    'id' => $rewardId,
    'title' => $title,
    'cost_points' => $costPoints,
    'provider' => $actor['name'],
    'reward_type' => $rewardType,
    'description' => $description,
    'image_base64' => $imageBase64,
    'redeem_code' => $redeemCode !== '' ? $redeemCode : null,
]);

<?php

require_once __DIR__ . '/helpers.php';

$user = require_auth();
respond(true, user_payload($user));

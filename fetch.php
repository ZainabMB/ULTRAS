<?php

require_once 'config.php';

/*
|--------------------------------------------------------------------------
| Fetch teams from SportMonks
|--------------------------------------------------------------------------
*/

$teamsResponse = sportmonksRequest('/teams');

if (!isset($teamsResponse['data'])) {
    die("Failed to fetch teams from SportMonks");
}

$teams = $teamsResponse['data'];


/*
|--------------------------------------------------------------------------
| Insert only team_id into Supabase
|--------------------------------------------------------------------------
*/

foreach ($teams as $team) {

    $team_id = $team['id'];

    $result = supabase_upsert('teams', [
        'team_id' => $team_id
    ]);

    print_r($result); // debug
}

echo "Import complete.";

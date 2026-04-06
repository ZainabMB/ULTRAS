<?php 
require_once 'config.php';
$leagueResponse = sportmonksRequest('/leagues');

if (!isset($leagueResponse['data'])) {
    die("Failed to fetch teams from SportMonks");
}

$leagues = $leagueResponse['data'];
echo json_encode($leagues);
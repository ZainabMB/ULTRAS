<?php
/*
|--------------------------------------------------------------------------
| SUPABASE CONFIG (REST API)
|--------------------------------------------------------------------------
| Use the service role key ONLY in backend PHP.
| Never expose it in JavaScript.
*/

define('SUPABASE_URL', 'https://dhwevbvoicvxgauqibna.supabase.co');
define('SUPABASE_SERVICE_KEY', 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImRod2V2YnZvaWN2eGdhdXFpYm5hIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc2MDk3NzU5OSwiZXhwIjoyMDc2NTUzNTk5fQ.qBbL_I-q8INFaE8Lqw-T4ml6ug-ZtZnUC67dU-v5WXA'); // replace with your real key
define('SUPABASE_REST_URL', SUPABASE_URL . '/rest/v1');


/*
|--------------------------------------------------------------------------
| SPORTMONKS API CONFIG
|--------------------------------------------------------------------------
*/

define('SPORTMONKS_API_KEY', '4gQl5O58MpYglphuUPQiDwgPVl8ePToa3X7OAhWZ1YaN7etOWnbtpoWVpBXF');
define('SPORTMONKS_BASE_URL', 'https://api.sportmonks.com/v3/football');


/*
|--------------------------------------------------------------------------
| SUPABASE HELPER (INSERT / UPSERT)
|--------------------------------------------------------------------------
*/

function supabase_upsert($table, $data) {

    $url = SUPABASE_REST_URL . '/' . $table;

    $ch = curl_init($url);

    curl_setopt($ch, CURLOPT_HTTPHEADER, [
        'apikey: ' . SUPABASE_SERVICE_KEY,
        'Authorization: Bearer ' . SUPABASE_SERVICE_KEY,
        'Content-Type: application/json',
        'Prefer: resolution=merge-duplicates'
    ]);

    // Wrap data in array (IMPORTANT)
    curl_setopt($ch, CURLOPT_POSTFIELDS, json_encode([$data]));

    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

    $result = curl_exec($ch);

    curl_close($ch);

    return json_decode($result, true);
}



/*
|--------------------------------------------------------------------------
| SPORTMONKS API HELPER
|--------------------------------------------------------------------------
*/

function sportmonksRequest($endpoint, $params = []) {
    $params['api_token'] = SPORTMONKS_API_KEY;

    $url = SPORTMONKS_BASE_URL . $endpoint . '?' . http_build_query($params);

    $ch = curl_init($url);
    curl_setopt_array($ch, [
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_TIMEOUT => 30
    ]);

    $response = curl_exec($ch);
    curl_close($ch);

    return json_decode($response, true);
}
?>

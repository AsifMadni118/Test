param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$ComposeFile = (Join-Path $PSScriptRoot "..\docker-compose.yml")
)

$ErrorActionPreference = "Stop"

function Assert-True([bool]$Condition, [string]$Message) {
    if (-not $Condition) { throw $Message }
}

function Invoke-LoginWithRetry([string]$Email, [string]$Password) {
    for ($attempt = 1; $attempt -le 30; $attempt++) {
        try {
            return Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/auth/login" -ContentType "application/json" `
                -Body (@{ email = $Email; password = $Password } | ConvertTo-Json)
        } catch {
            if ($attempt -eq 30) { throw }
            Start-Sleep -Seconds 1
        }
    }
}

$docker = Get-Command docker -ErrorAction Stop
$expectedServices = @(
    "mysql", "mongodb", "kafka", "protein-api", "config-server", "discovery-server",
    "authentication-service", "user-profile-service", "protein-service", "bookmark-service",
    "api-gateway", "frontend"
)
$runningServices = @(& $docker.Source compose -f $ComposeFile ps --services --filter status=running)
if ($LASTEXITCODE -ne 0) { throw "Docker Compose stack is not running." }
foreach ($service in $expectedServices) {
    Assert-True ($runningServices -contains $service) "Compose service '$service' is not running."
}

$gatewayReady = $false
for ($attempt = 1; $attempt -le 60; $attempt++) {
    try {
        $health = Invoke-RestMethod -Uri "$BaseUrl/actuator/health"
        if ($health.status -eq "UP") { $gatewayReady = $true; break }
    } catch {
        Start-Sleep -Seconds 2
    }
}
Assert-True $gatewayReady "API Gateway did not become healthy."

$expectedTopics = @(
    "authentication-events", "password-reset-events", "user-created", "user-updated",
    "bookmark-created", "bookmark-updated", "bookmark-deleted"
)
$actualTopics = & $docker.Source compose -f $ComposeFile exec -T kafka `
    /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list
if ($LASTEXITCODE -ne 0) { throw "Kafka topic inspection failed." }
foreach ($topic in $expectedTopics) {
    Assert-True ($actualTopics -contains $topic) "Kafka topic '$topic' is missing."
}

$email = "proteinpro.e2e.$([DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds())@example.com"
$oldPassword = "ProteinPro123!"
$newPassword = "ProteinPro456!"

$proteins = Invoke-RestMethod -Uri "$BaseUrl/api/proteins"
if ($proteins -is [array] -and $proteins.Count -eq 1 -and $proteins[0] -is [array]) { $proteins = $proteins[0] }
Assert-True ($proteins.Count -gt 0) "The real external Protein API returned no records."
$protein = $proteins[0]
foreach ($field in @("id", "source", "cost_grams", "cost_package", "protein_ per_pack", "vegetarian", "vegen")) {
    Assert-True ($null -ne $protein.PSObject.Properties[$field]) "Protein response is missing '$field'."
}

$registration = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/profiles/register" -ContentType "application/json" `
    -Body (@{ firstName = "Protein"; lastName = "Tester"; email = $email; password = $oldPassword } | ConvertTo-Json)
Assert-True ($registration.email -eq $email) "Registration returned the wrong identity."

$login = Invoke-LoginWithRetry $email $oldPassword
Assert-True (-not [string]::IsNullOrWhiteSpace($login.accessToken)) "Login did not return a JWT."
$headers = @{ Authorization = "Bearer $($login.accessToken)" }

$profile = Invoke-RestMethod -Uri "$BaseUrl/api/profiles/me" -Headers $headers
Assert-True ($profile.id -eq $registration.id) "JWT userId does not resolve to the registered profile."
$updatedProfile = Invoke-RestMethod -Method Put -Uri "$BaseUrl/api/profiles/me" -Headers $headers `
    -ContentType "application/json" -Body (@{ firstName = "Protein"; lastName = "Verified" } | ConvertTo-Json)
Assert-True ($updatedProfile.lastName -eq "Verified") "Profile update was not persisted."

$snapshot = @{
    id = [string]$protein.id
    source = [string]$protein.source
    cost_grams = $protein.cost_grams
    cost_package = [string]$protein.cost_package
    "protein_ per_pack" = $protein.'protein_ per_pack'
    vegetarian = [string]$protein.vegetarian
    vegen = [string]$protein.vegen
}
$bookmarkBody = @{ proteinId = [string]$protein.id; proteinData = $snapshot; comment = "E2E comparison" }
$bookmark = Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/bookmarks" -Headers $headers `
    -ContentType "application/json" -Body ($bookmarkBody | ConvertTo-Json -Depth 4)
Assert-True ($bookmark.proteinId -eq [string]$protein.id) "Bookmark did not retain the Protein ID."
$bookmarks = Invoke-RestMethod -Uri "$BaseUrl/api/bookmarks" -Headers $headers
if ($bookmarks -is [array] -and $bookmarks.Count -eq 1 -and $bookmarks[0] -is [array]) { $bookmarks = $bookmarks[0] }
Assert-True ($bookmarks.id -contains $bookmark.id) "Created bookmark was not listed for its owner."
$bookmark = Invoke-RestMethod -Method Put -Uri "$BaseUrl/api/bookmarks/$($bookmark.id)/comment" -Headers $headers `
    -ContentType "application/json" -Body (@{ comment = "E2E updated" } | ConvertTo-Json)
Assert-True ($bookmark.comment -eq "E2E updated") "Bookmark comment update failed."
Invoke-RestMethod -Method Delete -Uri "$BaseUrl/api/bookmarks/$($bookmark.id)" -Headers $headers | Out-Null

Invoke-RestMethod -Method Post -Uri "$BaseUrl/api/auth/password-reset" -Headers $headers `
    -ContentType "application/json" -Body (@{ newPassword = $newPassword } | ConvertTo-Json) | Out-Null
$newLogin = Invoke-LoginWithRetry $email $newPassword
Assert-True (-not [string]::IsNullOrWhiteSpace($newLogin.accessToken)) "Login with reset password failed."

$unauthorizedStatus = $null
try {
    Invoke-RestMethod -Uri "$BaseUrl/api/bookmarks" | Out-Null
} catch {
    if ($_.Exception.Response) {
        $unauthorizedStatus = [int]$_.Exception.Response.StatusCode
    }
}
Assert-True ($unauthorizedStatus -eq 401) "Protected Bookmark API did not return 401 without JWT."

[pscustomobject]@{
    Result = "PASS"
    UserId = $registration.id
    ProteinId = [string]$protein.id
    KafkaTopicsVerified = $expectedTopics.Count
    Flows = "protein, registration, Kafka activation, login, profile, bookmark CRUD, password reset, JWT rejection"
} | Format-List

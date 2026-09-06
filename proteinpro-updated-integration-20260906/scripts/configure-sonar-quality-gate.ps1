param(
    [Parameter(Mandatory = $true)][string]$Token,
    [string]$HostUrl = "http://localhost:9000",
    [string]$ProjectKey = "protein-pro-app",
    [string]$ProjectName = "Protein Pro App",
    [string]$GateName = "ProteinPro 90 Percent Coverage"
)

$ErrorActionPreference = "Stop"
$headers = @{ Authorization = "Bearer $Token" }
$hostBase = $HostUrl.TrimEnd('/')

for ($attempt = 1; $attempt -le 30; $attempt++) {
    try {
        $status = Invoke-RestMethod -Uri "$hostBase/api/system/status" -Headers $headers
        if ($status.status -eq "UP") { break }
    } catch {
        if ($attempt -eq 30) { throw }
    }
    Start-Sleep -Seconds 2
}

$encodedGate = [uri]::EscapeDataString($GateName)
$gateList = Invoke-RestMethod -Uri "$hostBase/api/qualitygates/list" -Headers $headers
if (@($gateList.qualitygates | Where-Object { $_.name -eq $GateName }).Count -eq 0) {
    Invoke-RestMethod -Method Post -Uri "$hostBase/api/qualitygates/create?name=$encodedGate" -Headers $headers | Out-Null
}

$gate = Invoke-RestMethod -Uri "$hostBase/api/qualitygates/show?name=$encodedGate" -Headers $headers
$coverageCondition = @($gate.conditions | Where-Object { $_.metric -eq "coverage" })
if ($coverageCondition.Count -eq 0) {
    Invoke-RestMethod -Method Post `
        -Uri "$hostBase/api/qualitygates/create_condition?gateName=$encodedGate&metric=coverage&op=LT&error=90" `
        -Headers $headers | Out-Null
} elseif ($coverageCondition[0].error -ne "90") {
    $conditionId = [uri]::EscapeDataString([string]$coverageCondition[0].id)
    Invoke-RestMethod -Method Post `
        -Uri "$hostBase/api/qualitygates/update_condition?id=$conditionId&metric=coverage&op=LT&error=90" `
        -Headers $headers | Out-Null
}

$encodedProject = [uri]::EscapeDataString($ProjectKey)
$encodedProjectName = [uri]::EscapeDataString($ProjectName)
$projects = Invoke-RestMethod -Uri "$hostBase/api/projects/search?projects=$encodedProject" -Headers $headers
if (@($projects.components | Where-Object { $_.key -eq $ProjectKey }).Count -eq 0) {
    Invoke-RestMethod -Method Post `
        -Uri "$hostBase/api/projects/create?project=$encodedProject&name=$encodedProjectName" `
        -Headers $headers | Out-Null
}
Invoke-RestMethod -Method Post `
    -Uri "$hostBase/api/qualitygates/select?projectKey=$encodedProject&gateName=$encodedGate" `
    -Headers $headers | Out-Null

Write-Output "Quality gate '$GateName' is assigned to '$ProjectKey' with coverage >= 90%."

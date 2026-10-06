$inputData = $input | Out-String
if ([string]::IsNullOrWhiteSpace($inputData)) {
    @{ decision = "ask" } | ConvertTo-Json -Compress | Write-Output
    exit
}

try {
    $inputJson = $inputData | ConvertFrom-Json
    $toolName = $inputJson.toolCall.name
    $cmd = $inputJson.toolCall.args.CommandLine
    
    if ($toolName -eq "run_command" -and $cmd -match "^\s*Get-ChildItem") {
        $response = @{
            decision = "allow"
            reason = "Auto-allowed Get-ChildItem via hooks"
        }
    } else {
        $response = @{
            decision = "ask"
        }
    }
} catch {
    $response = @{
        decision = "ask"
    }
}

$response | ConvertTo-Json -Compress | Write-Output

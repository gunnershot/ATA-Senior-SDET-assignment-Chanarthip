<#
.SYNOPSIS
    Automated Test Runner and Allure Single-File HTML Report Generator for SauceDemo UI Automation.

.DESCRIPTION
    Executes Playwright for Java test suites with JUnit 5 Jupiter, handles runtime environment configuration,
    archives raw test results, and generates a self-contained, portable Single-File Allure HTML report.

.PARAMETER Test
    Optional filter for test class or method (e.g., "LoginTest", "*#shouldDisplayInventoryWhenStandardUserLogsInWithValidCredentials").

.PARAMETER Tag
    Optional JUnit 5 tag expression filter (e.g., "P0", "P1", "Happy", "Negative", "Diagnostic").

.PARAMETER Browser
    Optional browser target override (e.g., "chromium", "firefox", "webkit"). Default is configured in playwright.json.

.PARAMETER Headed
    Switch to run tests with browser window visible (sets headless=false). Default is headless=true.

.PARAMETER Clean
    Switch to execute 'mvn clean test' instead of 'mvn test'.

.PARAMETER ReportOnly
    Switch to skip test execution and generate the Allure report directly from existing results.

.PARAMETER ResultsDir
    Optional custom path to raw Allure results directory (useful when generating reports from allure-archive).

.PARAMETER NoOpen
    Switch to prevent opening the generated Allure report automatically in the default browser (ideal for CI/CD).

.PARAMETER Threads
    Optional number of parallel processes (forks) to run test classes concurrently (sets -DforkCount=<N>).

.EXAMPLE
    .\run-tests.ps1
    Runs all 22 scenarios headlessly and generates the Allure single-file report.

.EXAMPLE
    .\run-tests.ps1 -Threads 3
    Runs test classes in parallel across 3 JVM fork processes.

.EXAMPLE
    .\run-tests.ps1 -Test "LoginTest" -Headed
    Runs LoginTest in headed mode (browser visible).

.EXAMPLE
    .\run-tests.ps1 -Tag "P0" -Browser "firefox"
    Runs all P0 critical tests on Firefox.

.EXAMPLE
    .\run-tests.ps1 -ReportOnly
    Generates the latest Allure report from existing results without re-executing tests.
#>

[CmdletBinding()]
param(
    [string]$Test       = "",
    [string]$Tag        = "",
    [string]$Browser    = "",
    [switch]$Headed,
    [switch]$Clean,
    [switch]$ReportOnly,
    [string]$ResultsDir = "",
    [switch]$NoOpen,
    [int]$Threads       = 0
)

$ErrorActionPreference = "Stop"

# ==============================================================================
# 1. Environment & Path Resolution
# ==============================================================================
$scriptDir   = Split-Path -Parent $MyInvocation.MyCommand.Path
$bundledJdk  = Join-Path $scriptDir "jdk-17.0.10+7"
$bundledMvn  = Join-Path $scriptDir "apache-maven-3.9.6"
$allureBat   = Join-Path $scriptDir ".allure\allure-2.25.0\bin\allure.bat"

$resultsDir  = Join-Path $scriptDir "target\allure-results"
if (-not (Test-Path $resultsDir) -and (Test-Path (Join-Path $scriptDir "allure-results"))) {
    $resultsDir = Join-Path $scriptDir "allure-results"
}
$reportDir   = Join-Path $scriptDir "allure-report"
$historyDir  = Join-Path $scriptDir "allure-history"
$archiveRoot = Join-Path $scriptDir "allure-archive"

# Configure Java: Prefer bundled tools if complete, otherwise detect system JDK
if (Test-Path "$bundledJdk\bin\java.exe") {
    $env:JAVA_HOME = $bundledJdk
    $env:PATH = "$bundledJdk\bin;$env:PATH"
} elseif (Test-Path "C:\Program Files\Java\jdk-17\bin\java.exe") {
    $env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
    $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
} elseif (-not $env:JAVA_HOME) {
    Write-Warning "JAVA_HOME is not set and bundled JDK was not found. Relying on system PATH."
}

if (Test-Path $bundledMvn) {
    $env:PATH = "$bundledMvn\bin;$env:PATH"
}

# Determine Maven executable (prefer mvnw.cmd wrapper if global mvn is absent)
$mvnCmd = "mvn"
if (-not (Get-Command "mvn" -ErrorAction SilentlyContinue)) {
    $mvnWrapper = Join-Path $scriptDir "mvnw.cmd"
    if (Test-Path $mvnWrapper) {
        $mvnCmd = $mvnWrapper
    } else {
        Write-Error "[ERROR] Maven ('mvn' or 'mvnw.cmd') was not found on PATH or in bundled directories. Please install Maven or configure PATH."
        exit 1
    }
}

Write-Host ""
Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " SauceDemo Senior SDET UI Automation (Playwright + Java) " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$testExitCode = 0

# ==============================================================================
# 2. Test Execution
# ==============================================================================
if (-not $ReportOnly) {
    # Clean previous raw results to avoid stale test records
    if (Test-Path $resultsDir) {
        Remove-Item $resultsDir -Recurse -Force
    }

    $mvnArgs = @()
    if ($Clean) { $mvnArgs += "clean" }
    $mvnArgs += "test"

    if ($Test -ne "") {
        $mvnArgs += "-Dtest=$Test"
        Write-Host " Filter [Test]   : $Test" -ForegroundColor Yellow
    }

    if ($Tag -ne "") {
        $mvnArgs += "-Dgroups=$Tag"
        Write-Host " Filter [Tag]    : $Tag" -ForegroundColor Yellow
    }

    if ($Browser -ne "") {
        $mvnArgs += "-Dbrowser=$Browser"
        Write-Host " Target [Browser]: $Browser" -ForegroundColor Yellow
    }

    if ($Headed) {
        $mvnArgs += "-Dheadless=false"
        Write-Host " Execution Mode  : Headed (Browser Window Visible)" -ForegroundColor Magenta
    } else {
        Write-Host " Execution Mode  : Headless (Default for CI/Automation)" -ForegroundColor Gray
    }

    if ($Threads -gt 0) {
        $mvnArgs += "-DforkCount=$Threads"
        Write-Host " Parallel Forks  : $Threads JVM processes (-DforkCount=$Threads)" -ForegroundColor Cyan
    }

    if ($Test -eq "" -and $Tag -eq "") {
        Write-Host " Target Suite    : All 22 Scenarios across 5 Test Classes" -ForegroundColor Yellow
    }
    Write-Host ""

    $ErrorActionPreference = "Continue"
    & $mvnCmd @mvnArgs
    $testExitCode = $LASTEXITCODE
    $ErrorActionPreference = "Stop"

    Write-Host ""
    if ($testExitCode -eq 0) {
        Write-Host " [Tests] BUILD SUCCESS" -ForegroundColor Green
    } else {
        Write-Host " [Tests] BUILD FAILED (exit code: $testExitCode) -- Proceeding to generate report" -ForegroundColor Red
    }

    # Archive raw results for regression history
    if (Test-Path $resultsDir) {
        $timestamp = Get-Date -Format "yyyy-MM-dd_HH-mm-ss"
        $archiveDir = Join-Path $archiveRoot $timestamp
        New-Item -ItemType Directory -Force -Path $archiveDir | Out-Null
        Copy-Item "$resultsDir\*" $archiveDir -Recurse -Force
        Write-Host " [Archive] Raw results archived to: $archiveDir" -ForegroundColor Gray
    }
}

# ==============================================================================
# 3. Source Results Validation
# ==============================================================================
$sourceResults = $resultsDir
if ($ResultsDir -ne "") {
    $sourceResults = (Resolve-Path $ResultsDir).Path
}

if (-not (Test-Path $sourceResults) -or -not (Get-ChildItem $sourceResults -Filter "*-result.json" -ErrorAction SilentlyContinue)) {
    Write-Host " [ERROR] No Allure results found in: $sourceResults" -ForegroundColor Red
    exit 1
}

# ==============================================================================
# 4. Allure CLI Installation (First-run bootstrap)
# ==============================================================================
if (-not (Test-Path $allureBat)) {
    Write-Host " Downloading & installing Allure CLI (First time only)..." -ForegroundColor Cyan
    $ErrorActionPreference = "Continue"
    & $mvnCmd -q io.qameta.allure:allure-maven:2.12.0:install
    $ErrorActionPreference = "Stop"
}

if (-not (Test-Path $allureBat)) {
    Write-Error "[ERROR] Failed to locate Allure CLI at '$allureBat'. Ensure internet connectivity or install Allure globally."
    exit 1
}

# ==============================================================================
# 5. Metadata Preparation & Report Generation
# ==============================================================================
$workDir = Join-Path $env:TEMP ("allure-work-" + [guid]::NewGuid())
New-Item -ItemType Directory -Force -Path $workDir | Out-Null

try {
    Copy-Item "$sourceResults\*" $workDir -Recurse -Force

    # Preserve test history across runs
    if (Test-Path $historyDir) {
        Copy-Item $historyDir (Join-Path $workDir "history") -Recurse -Force
    }

    # Extract runtime metadata for Allure Environment widget
    $playwrightConfigFile = Join-Path $scriptDir "src\test\resources\playwright.json"
    $pwJson = Get-Content $playwrightConfigFile -Raw | ConvertFrom-Json
    $activeBrowser  = if ($Browser -ne "") { $Browser } else { $pwJson.browser }
    $activeHeadless = if ($Headed) { "false" } else { [string]$pwJson.headless }

    @(
        "Project=SauceDemo UI Automation",
        "Framework=Playwright for Java 1.46.0",
        "Test.Runner=JUnit 5 Jupiter",
        "Browser=$activeBrowser",
        "Headless=$activeHeadless",
        "Base.URL=$($pwJson.baseUrl)",
        "Java.Version=17.0.10",
        "OS=$([System.Environment]::OSVersion.VersionString)"
    ) | Set-Content (Join-Path $workDir "environment.properties") -Encoding UTF8

    Write-Host ""
    Write-Host " Generating Allure Single-File HTML Report..." -ForegroundColor Cyan

    # Generate standard intermediate report to update trend history
    if (-not $ReportOnly) {
        $tmpReport = Join-Path $workDir "_full-report"
        & cmd /c "`"$allureBat`" generate `"$workDir`" --clean -o `"$tmpReport`" > nul 2>&1"
        if (Test-Path "$tmpReport\history") {
            if (Test-Path $historyDir) { Remove-Item $historyDir -Recurse -Force }
            Copy-Item "$tmpReport\history" $historyDir -Recurse -Force
        }
        Remove-Item $tmpReport -Recurse -Force -ErrorAction SilentlyContinue
    }

    # Generate portable single-file HTML report
    $ErrorActionPreference = "Continue"
    & cmd /c "`"$allureBat`" generate `"$workDir`" --single-file --clean -o `"$reportDir`""
    $reportExitCode = $LASTEXITCODE
    $ErrorActionPreference = "Stop"

    $indexPath = Join-Path $reportDir "index.html"
    if ($reportExitCode -eq 0 -and (Test-Path $indexPath)) {
        Write-Host ""
        Write-Host "==========================================================" -ForegroundColor Green
        Write-Host " Allure Single-File Report Successfully Generated!         " -ForegroundColor Green
        Write-Host "==========================================================" -ForegroundColor Green
        Write-Host " Report  : $indexPath" -ForegroundColor White
        Write-Host " Results : $sourceResults" -ForegroundColor Gray
        Write-Host " Archive : $archiveRoot" -ForegroundColor Gray
        Write-Host ""

        if (-not $NoOpen) {
            Write-Host " Opening report in your default browser..." -ForegroundColor Cyan
            Start-Process $indexPath
        }
    } else {
        Write-Host " [ERROR] Failed to generate Allure report." -ForegroundColor Red
        exit 1
    }
}
finally {
    # Ensure temporary working directory is cleanly scrubbed
    if (Test-Path $workDir) {
        Remove-Item $workDir -Recurse -Force -ErrorAction SilentlyContinue
    }
}

exit $testExitCode


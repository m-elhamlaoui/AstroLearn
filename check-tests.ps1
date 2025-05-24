Write-Host "Checking integration test files..."

$testFiles = Get-ChildItem -Path ".\Backend\demo\src\test\java\com\example\demo\service\integration" -Filter "*Test.java"

foreach ($file in $testFiles) {
    $content = Get-Content $file.FullName -Raw
    
    Write-Host "`nChecking $($file.Name)..."
    
    # Check if file extends BaseIntegrationTest
    if ($content -match "extends BaseIntegrationTest") {
        Write-Host "  ❌ Extends BaseIntegrationTest" -ForegroundColor Red
    } else {
        Write-Host "  ✅ Does not extend BaseIntegrationTest" -ForegroundColor Green
    }
    
    # Check if file has SpringBootTest and Transactional annotations
    if ($content -match "@SpringBootTest" -and $content -match "@Transactional") {
        Write-Host "  ✅ Has @SpringBootTest and @Transactional annotations" -ForegroundColor Green
    } else {
        Write-Host "  ❌ Missing @SpringBootTest or @Transactional annotations" -ForegroundColor Red
    }
    
    # Check if file imports TestLogger
    if ($content -match "import.*TestLogger" -or $content -match "import static.*TestLogger") {
        Write-Host "  ✅ Imports TestLogger" -ForegroundColor Green
    } else {
        Write-Host "  ❌ Does not import TestLogger" -ForegroundColor Red
    }
}

Write-Host "`nChecking BaseIntegrationTest.java..."
$baseFile = ".\Backend\demo\src\test\java\com\example\demo\util\BaseIntegrationTest.java"
if (Test-Path $baseFile) {
    Write-Host "  ⚠️ BaseIntegrationTest.java exists but is not being used" -ForegroundColor Yellow
} else {
    Write-Host "  ✅ BaseIntegrationTest.java does not exist" -ForegroundColor Green
}

Write-Host "`nChecking TestLogger.java..."
$loggerFile = ".\Backend\demo\src\test\java\com\example\demo\util\TestLogger.java"
if (Test-Path $loggerFile) {
    Write-Host "  ✅ TestLogger.java exists" -ForegroundColor Green
} else {
    Write-Host "  ❌ TestLogger.java does not exist" -ForegroundColor Red
}

Write-Host "`nDone!"

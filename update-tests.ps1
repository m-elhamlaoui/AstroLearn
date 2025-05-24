$testFiles = Get-ChildItem -Path ".\Backend\demo\src\test\java\com\example\demo\service\integration" -Filter "*Test.java"

foreach ($file in $testFiles) {
    Write-Host "Processing $($file.Name)..."
    
    # Read the file content
    $content = Get-Content $file.FullName -Raw
    
    # Add import for BaseIntegrationTest
    $content = $content -replace "import org\.springframework\.boot\.test\.context\.SpringBootTest;", 
        "import org.springframework.boot.test.context.SpringBootTest;`r`nimport com.example.demo.util.BaseIntegrationTest;`r`nimport static com.example.demo.util.TestLogger.*;"
    
    # Remove @SpringBootTest and @Transactional annotations and extend BaseIntegrationTest
    $content = $content -replace "@SpringBootTest\r?\n@Transactional\r?\npublic class (\w+) \{", 
        "public class `$1 extends BaseIntegrationTest {"
    
    # Add logging to setUp method if it exists
    $content = $content -replace "(\s+)@BeforeEach\r?\n\s+void setUp\(\) \{", 
        "`$1@BeforeEach`r`n`$1void setUp() {`r`n`$1    logStep(""Setting up test data for $($file.BaseName)"");"
    
    # Add logging to test methods
    $content = $content -replace "(\s+)@Test\r?\n\s+void (\w+)\(\) \{", 
        "`$1@Test`r`n`$1void `$2() {`r`n`$1    logStep(""Executing test: `$2"");"
    
    # Write the modified content back to the file
    Set-Content -Path $file.FullName -Value $content
}

Write-Host "All integration test files have been updated!"

Write-Host "========================================================" -ForegroundColor Cyan
Write-Host "Starting MySQL 8.4 Server for Derma Bliss Clinic..." -ForegroundColor Green
Write-Host "Data Directory: $PSScriptRoot\mysql-data" -ForegroundColor Gray
Write-Host "Port: 3306" -ForegroundColor Gray
Write-Host "========================================================" -ForegroundColor Cyan

$running = Get-Process mysqld -ErrorAction SilentlyContinue
if ($running) {
    Write-Host "[INFO] MySQL Server is ALREADY running (PID: $($running.Id)) on port 3306." -ForegroundColor Yellow
} else {
    $mysqld = "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysqld.exe"
    $dataDir = "$PSScriptRoot\mysql-data"
    Start-Process -FilePath $mysqld -ArgumentList "--datadir=`"$dataDir`"", "--console" -WindowStyle Minimized
    Start-Sleep -Seconds 3
    Write-Host "[SUCCESS] MySQL Server started successfully in background!" -ForegroundColor Green
}

Write-Host "`nYou can now connect in this terminal by running:" -ForegroundColor White
Write-Host "  mysql -u root`n" -ForegroundColor Yellow

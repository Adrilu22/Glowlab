param(
    [int]$RequestsPerSecond = 3,
    [int]$DurationSeconds   = 120
)

$BaseUrl  = "http://localhost:8080"
$Interval = 1.0 / $RequestsPerSecond
$EndTime  = (Get-Date).AddSeconds($DurationSeconds)

$Endpoints = @(
    "/api/categorias",
    "/api/productos",
    "/api/usuarios",
    "/api/rutinas",
    "/api/productos/1",
    "/api/categorias/1"
)

$CompraBody = '[{"productoId":1},{"productoId":2}]'

Write-Host "Generando trafico: $RequestsPerSecond req/s durante ${DurationSeconds}s"
Write-Host "API: $BaseUrl`n"

$Total = 0

while ((Get-Date) -lt $EndTime) {
    try {
        if ((Get-Random -Maximum 10) -eq 0) {
            $r = Invoke-WebRequest -Uri "$BaseUrl/api/compras" -Method POST `
                 -Body $CompraBody -ContentType "application/json" -UseBasicParsing -TimeoutSec 5
            Write-Host "[$($r.StatusCode)] POST /api/compras"
        } else {
            $path = $Endpoints | Get-Random
            $r = Invoke-WebRequest -Uri "$BaseUrl$path" -Method GET -UseBasicParsing -TimeoutSec 5
            Write-Host "[$($r.StatusCode)] GET $path"
        }
        $Total++
    } catch {
        Write-Host "[ERROR] $($_.Exception.Message)"
    }

    Start-Sleep -Milliseconds ([int]($Interval * 1000))
}

Write-Host "`nFinalizado. Total peticiones: $Total"

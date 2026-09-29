# Prueba de flujo EP1 completo (API Gateway -> BFF -> microservicios)
#
# Uso:
#   1. Levantá backend: .\run-all-services.ps1 -SkipBuild
#   2. En el navegador: login Microsoft, F12 -> Red -> petición "me" -> copiar Bearer (solo eyJ...)
#   3. En PowerShell:
#        $env:EP1_BEARER_TOKEN = "eyJ...."
#        .\docs\test-flujo-ep1-completo.ps1
#
# Opcional: $env:EP1_GATEWAY = "http://localhost:8080"

param(
    [string]$Gateway = $(if ($env:EP1_GATEWAY) { $env:EP1_GATEWAY.TrimEnd('/') } else { "http://localhost:8080" }),
    [string]$Token = $env:EP1_BEARER_TOKEN
)

function Get-Coalesce {
    param($Primary, $Fallback)
    if ($null -ne $Primary -and "$Primary".Length -gt 0) { return $Primary }
    return $Fallback
}

$ErrorActionPreference = "Stop"
$passed = 0
$failed = 0
$skipped = 0

function Write-Step($num, $title) {
    Write-Host ""
    Write-Host "=== Paso $num : $title ===" -ForegroundColor Cyan
}

function Assert-Ok($condition, $okMsg, $failMsg) {
    if ($condition) {
        Write-Host "  OK  $okMsg" -ForegroundColor Green
        $script:passed++
        return $true
    }
    Write-Host "  FAIL $failMsg" -ForegroundColor Red
    $script:failed++
    return $false
}

function Invoke-Gateway {
    param(
        [string]$Method = "GET",
        [string]$Path,
        [string]$Bearer,
        [object]$Body = $null
    )
    $uri = "$Gateway$Path"
    $headers = @{ Accept = "application/json" }
    if ($Bearer) {
        $headers.Authorization = "Bearer $Bearer"
    }
    $params = @{
        Uri         = $uri
        Method      = $Method
        Headers     = $headers
        TimeoutSec  = 30
        ErrorAction = "Stop"
    }
    if ($null -ne $Body) {
        $params.ContentType = "application/json"
        $params.Body = ($Body | ConvertTo-Json -Depth 10 -Compress)
    }
    try {
        $r = Invoke-WebRequest @params -UseBasicParsing
        $json = $null
        if ($r.Content) {
            try { $json = $r.Content | ConvertFrom-Json } catch { $json = $r.Content }
        }
        return @{ Status = $r.StatusCode; Json = $json; Raw = $r.Content }
    } catch {
        $resp = $_.Exception.Response
        if ($resp) {
            $reader = New-Object System.IO.StreamReader($resp.GetResponseStream())
            $text = $reader.ReadToEnd()
            return @{ Status = [int]$resp.StatusCode; Json = $null; Raw = $text; Error = $true }
        }
        throw
    }
}

function Show-TokenClaimsPreview([string]$jwt) {
    $parts = $jwt -split '\.'
    if ($parts.Count -lt 2) {
        Write-Host "  (token no parece JWT)" -ForegroundColor DarkYellow
        return
    }
    function Decode-Payload([string]$b64) {
        $p = $b64.Replace('-', '+').Replace('_', '/')
        switch ($p.Length % 4) {
            2 { $p += '==' }
            3 { $p += '=' }
        }
        [Text.Encoding]::UTF8.GetString([Convert]::FromBase64String($p))
    }
    try {
        $payload = Decode-Payload $parts[1] | ConvertFrom-Json
        Write-Host "  Token (sin imprimir secreto): iss=$($payload.iss)" -ForegroundColor DarkGray
        Write-Host "  aud=$($payload.aud) | roles=$($payload.roles -join ',')" -ForegroundColor DarkGray
        Write-Host "  exp=$([DateTimeOffset]::FromUnixTimeSeconds($payload.exp).LocalDateTime)" -ForegroundColor DarkGray
    } catch {
        Write-Host "  (no se pudo decodificar payload)" -ForegroundColor DarkYellow
    }
}

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Test flujo EP1 - Mil Sabores" -ForegroundColor Cyan
Write-Host "  Gateway: $Gateway" -ForegroundColor DarkGray
Write-Host "============================================" -ForegroundColor Cyan

# --- Infra ---
Write-Step "0" "Salud de servicios"
$ports = @{ gateway = 8080; bff = 8085; usuario = 8081; producto = 8082; carrito = 8083; ventas = 8084 }
foreach ($kv in $ports.GetEnumerator()) {
    try {
        $h = Invoke-WebRequest -Uri "http://localhost:$($kv.Value)/actuator/health" -UseBasicParsing -TimeoutSec 5
        Assert-Ok ($h.StatusCode -eq 200) "$($kv.Key) :$($kv.Value) health 200" "$($kv.Key) no responde"
    } catch {
        Assert-Ok $false "$($kv.Key) health" "$($kv.Key) :$($kv.Value) DOWN"
    }
}

# --- Público: Gateway -> BFF -> producto-service ---
Write-Step "1" "Catálogo público (sin token) - Gateway -> BFF -> producto-service"
$prod = Invoke-Gateway -Path "/api/productos"
$productList = @()
if ($prod.Json -is [Array]) { $productList = $prod.Json }
elseif ($prod.Json.content) { $productList = $prod.Json.content }
Assert-Ok ($prod.Status -eq 200 -and $productList.Count -gt 0) "GET /api/productos -> $($prod.Status), $($productList.Count) productos" "GET /api/productos -> $($prod.Status)"
$sample = $productList[0]

# --- Protegido sin token ---
Write-Step "2" "Ruta protegida sin Bearer (debe fallar en gateway)"
$noAuth = Invoke-Gateway -Path "/api/usuarios/me"
Assert-Ok ($noAuth.Status -eq 401) "GET /api/usuarios/me sin token -> 401" "Esperaba 401, obtuvo $($noAuth.Status)"

if (-not $Token -or $Token.Trim().Length -lt 20) {
    Write-Host ""
    Write-Host "=== Pasos 3-8 OMITIDOS (falta token Entra) ===" -ForegroundColor Yellow
    Write-Host "Copiá el Bearer desde F12 (petición 'me') y ejecutá:" -ForegroundColor Yellow
    Write-Host '  $env:EP1_BEARER_TOKEN = "eyJ..."' -ForegroundColor White
    Write-Host "  .\docs\test-flujo-ep1-completo.ps1" -ForegroundColor White
    $skipped += 6
} else {
    $Token = $Token.Trim()

    Write-Step "3" "Claims del access token (vista previa)"
    Show-TokenClaimsPreview $Token

    Write-Step "4" "Perfil Neon - Gateway -> BFF -> usuario-service (1ª+2ª JWT en BFF vía /api)"
    $me = Invoke-Gateway -Path "/api/usuarios/me" -Bearer $Token
    $usuarioId = $null
    if ($me.Json -and $me.Json.id) { $usuarioId = [long]$me.Json.id }
    Assert-Ok ($me.Status -eq 200 -and $usuarioId) "/api/usuarios/me -> $($me.Status), neon id=$usuarioId" "/api/usuarios/me -> $($me.Status) $($me.Raw)"

    Write-Step "5" "Segunda validación explícita BFF - GET /bff/me"
    $bffMe = Invoke-Gateway -Path "/bff/me" -Bearer $Token
    Assert-Ok ($bffMe.Status -eq 200) "/bff/me -> $($bffMe.Status)" "/bff/me -> $($bffMe.Status) $($bffMe.Raw)"

    Write-Step "6" "Carrito - POST /api/carritos/agregar"
    if ($sample -and $usuarioId) {
        $code = Get-Coalesce $sample.code (Get-Coalesce $sample.productoCode "TC001")
        $nombre = Get-Coalesce $sample.nombre (Get-Coalesce $sample.productoNombre "Producto prueba")
        $precio = [int](Get-Coalesce $sample.precioCLP 10000)
        $stock = [int](Get-Coalesce $sample.stock 99)
        $cartBody = @{
            usuarioId        = $usuarioId
            productoCode     = $code
            productoNombre   = $nombre
            precioCLP        = $precio
            productoImagen   = (Get-Coalesce $sample.imagen "")
            cantidad         = 1
            stockDisponible  = $stock
        }
        $cart = Invoke-Gateway -Method POST -Path "/api/carritos/agregar" -Bearer $Token -Body $cartBody
        Assert-Ok ($cart.Status -eq 200 -or $cart.Status -eq 201) "Carrito agregar -> $($cart.Status)" "Carrito -> $($cart.Status) $($cart.Raw)"
    } else {
        Write-Host "  SKIP (sin producto o usuarioId)" -ForegroundColor Yellow
        $skipped++
    }

    Write-Step "7" "Venta - POST /api/ventas (checkout backend)"
    if ($usuarioId -and $sample) {
        $precio = [int](Get-Coalesce $sample.precioCLP 10000)
        $subtotal = $precio
        $iva = [math]::Round($subtotal * 0.19)
        $total = $subtotal + $iva
        $ventaBody = @{
            usuarioId     = $usuarioId
            usuarioNombre = (Get-Coalesce $me.Json.nombre "Cliente EP1 Test")
            usuarioEmail  = (Get-Coalesce $me.Json.email "test@duocuc.cl")
            detalles      = @(
                @{
                    productoCode   = (Get-Coalesce $sample.code "TC001")
                    productoNombre = (Get-Coalesce $sample.nombre "Test")
                    productoImagen = ""
                    cantidad       = 1
                    precioUnitario = $precio
                    subtotal       = $subtotal
                }
            )
            subtotal      = $subtotal
            iva           = $iva
            total         = $total
        }
        $venta = Invoke-Gateway -Method POST -Path "/api/ventas" -Bearer $Token -Body $ventaBody
        $ventaId = $venta.Json.id
        Assert-Ok (($venta.Status -eq 200 -or $venta.Status -eq 201) -and $ventaId) "Venta creada id=$ventaId status=$($venta.Status)" "Venta -> $($venta.Status) $($venta.Raw)"

        Write-Step "8" "Pago Transbank (integración) - POST /api/ventas/{id}/pagar"
        if ($ventaId) {
            $pago = Invoke-Gateway -Method POST -Path "/api/ventas/$ventaId/pagar" -Bearer $Token
            $hasUrl = $pago.Json.url -or ($pago.Raw -match "webpay")
            Assert-Ok (($pago.Status -eq 200) -and $hasUrl) "Inicio pago Transbank -> $($pago.Status)" "Pago -> $($pago.Status) $($pago.Raw)"
            Write-Host "  (El pago en navegador lo completás en Webpay; no se automatiza aquí.)" -ForegroundColor DarkGray
        }
    } else {
        Write-Host "  SKIP venta/pago" -ForegroundColor Yellow
        $skipped += 2
    }
}

Write-Host ""
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  Resumen: OK=$passed  FAIL=$failed  SKIP/Omitido=$skipped" -ForegroundColor $(if ($failed -eq 0) { "Green" } else { "Yellow" })
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Flujo UI manual (complemento):" -ForegroundColor DarkGray
Write-Host "  Login MSAL -> productos -> carrito (guest o logueado) -> checkout -> Webpay" -ForegroundColor DarkGray

if ($failed -gt 0) { exit 1 }
exit 0

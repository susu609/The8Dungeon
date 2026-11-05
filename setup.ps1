# Tai Gradle cache dung san tu GitHub Release "gradle-cache" va cai vao may.
# Chay trong PowerShell:
#   irm https://raw.githubusercontent.com/susu609/The8Dungeon/main/setup.ps1 | iex
# Sau do mo IntelliJ, dat Gradle user home = thu muc in ra o cuoi.

$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'   # Invoke-WebRequest nhanh hon nhieu khi tat thanh tien do

$Repo = 'susu609/The8Dungeon'
$Dest = Join-Path $env:LOCALAPPDATA 'the8dungeons-gradle'
$Tmp  = Join-Path $env:TEMP 'the8dungeons-cache'

New-Item -ItemType Directory -Force -Path $Dest, $Tmp | Out-Null

Write-Host "Lay danh sach file tu release gradle-cache..."
$release = Invoke-RestMethod "https://api.github.com/repos/$Repo/releases/tags/gradle-cache"
$asset = $release.assets | Where-Object { $_.name -eq 'gradle-cache.zip' }
if (-not $asset) { throw "Release gradle-cache khong co gradle-cache.zip. Hay chay workflow 'Gradle cache for IntelliJ' truoc." }

$archive = Join-Path $Tmp 'gradle-cache.zip'
Write-Host ("Tai gradle-cache.zip ({0:N0} MB)..." -f ($asset.size / 1MB))
Invoke-WebRequest $asset.browser_download_url -OutFile $archive

Write-Host "Giai nen vao $Dest ..."
Expand-Archive -Path $archive -DestinationPath $Dest -Force
Remove-Item $archive

[Environment]::SetEnvironmentVariable('GRADLE_USER_HOME', $Dest, 'User')
$env:GRADLE_USER_HOME = $Dest

Write-Host ""
Write-Host "Xong. Gradle user home: $Dest"
Write-Host "Trong IntelliJ: Settings > Build Tools > Gradle > Gradle user home = $Dest"
Write-Host "Nen dong va mo lai IntelliJ de nhan bien moi truong."
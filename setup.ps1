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
$parts = $release.assets | Where-Object { $_.name -like 'gradle-cache.tgz.part*' } | Sort-Object name
if (-not $parts) { throw "Release gradle-cache khong co file. Hay chay workflow 'Gradle cache for IntelliJ' truoc." }

$archive = Join-Path $Tmp 'gradle-cache.tgz'
$out = [System.IO.File]::Create($archive)
try {
    foreach ($p in $parts) {
        $file = Join-Path $Tmp $p.name
        Write-Host ("Tai {0} ({1:N0} MB)..." -f $p.name, ($p.size / 1MB))
        Invoke-WebRequest $p.browser_download_url -OutFile $file
        $in = [System.IO.File]::OpenRead($file)
        try { $in.CopyTo($out) } finally { $in.Close() }
        Remove-Item $file
    }
} finally { $out.Close() }

Write-Host "Giai nen vao $Dest ..."
tar -xzf $archive -C $Dest
Remove-Item $archive

[Environment]::SetEnvironmentVariable('GRADLE_USER_HOME', $Dest, 'User')
$env:GRADLE_USER_HOME = $Dest

Write-Host ""
Write-Host "Xong. Gradle user home: $Dest"
Write-Host "Trong IntelliJ: Settings > Build Tools > Gradle > Gradle user home = $Dest"
Write-Host "Nen dong va mo lai IntelliJ de nhan bien moi truong."

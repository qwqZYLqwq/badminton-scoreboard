Add-Type -AssemblyName System.Drawing

$root = $PSScriptRoot
$resDir = Join-Path $root "app\src\main\res"

$sizes = @{
    "mipmap-mdpi" = 48;
    "mipmap-hdpi" = 72;
    "mipmap-xhdpi" = 96;
    "mipmap-xxhdpi" = 144;
    "mipmap-xxxhdpi" = 192
}

foreach ($folder in $sizes.Keys) {
    $size = $sizes[$folder]
    $dir = Join-Path $resDir $folder
    if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Force -Path $dir }

    # 1. Full launcher icon with deep athletic blue background
    $bmp = New-Object System.Drawing.Bitmap $size, $size
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $g.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit

    $bgBrush = New-Object System.Drawing.SolidBrush ([System.Drawing.Color]::FromArgb(255, 15, 23, 42))
    $g.FillRectangle($bgBrush, 0, 0, $size, $size)

    $fontSize = [int]($size * 0.58)
    $font = New-Object System.Drawing.Font("Segoe UI Emoji", $fontSize, [System.Drawing.FontStyle]::Regular, [System.Drawing.GraphicsUnit]::Pixel)
    $textBrush = [System.Drawing.Brushes]::White
    $sf = New-Object System.Drawing.StringFormat
    $sf.Alignment = [System.Drawing.StringAlignment]::Center
    $sf.LineAlignment = [System.Drawing.StringAlignment]::Center
    $rect = New-Object System.Drawing.RectangleF 0, 0, $size, $size
    $emoji = [char]::ConvertFromUtf32(0x1F3F8)

    $g.DrawString($emoji, $font, $textBrush, $rect, $sf)

    $bmp.Save((Join-Path $dir "ic_launcher.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Save((Join-Path $dir "ic_launcher_round.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $g.Dispose()
    $bmp.Dispose()

    # 2. Foreground icon with transparent background (for adaptive icon)
    $bmpFg = New-Object System.Drawing.Bitmap $size, $size
    $gFg = [System.Drawing.Graphics]::FromImage($bmpFg)
    $gFg.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $gFg.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit
    $gFg.Clear([System.Drawing.Color]::Transparent)

    $gFg.DrawString($emoji, $font, $textBrush, $rect, $sf)
    $bmpFg.Save((Join-Path $dir "ic_launcher_foreground.png"), [System.Drawing.Imaging.ImageFormat]::Png)
    $gFg.Dispose()
    $bmpFg.Dispose()
}

# 3. Create mipmap-anydpi-v26 for Android 8.0+
$anydpiDir = Join-Path $resDir "mipmap-anydpi-v26"
if (-not (Test-Path $anydpiDir)) { New-Item -ItemType Directory -Force -Path $anydpiDir }

$adaptiveXml = @"
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background"/>
    <foreground android:drawable="@mipmap/ic_launcher_foreground"/>
</adaptive-icon>
"@
$utf8NoBom = New-Object System.Text.UTF8Encoding $false
[System.IO.File]::WriteAllText((Join-Path $anydpiDir "ic_launcher.xml"), $adaptiveXml, $utf8NoBom)
[System.IO.File]::WriteAllText((Join-Path $anydpiDir "ic_launcher_round.xml"), $adaptiveXml, $utf8NoBom)

Write-Host "All launcher icons generated successfully with 🏸 emoji!"

[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string] $Serial,

    [switch] $AllowDisposablePhysicalDevice
)

$ErrorActionPreference = 'Stop'

$sdkCandidates = @(
    $env:ANDROID_HOME,
    $env:ANDROID_SDK_ROOT,
    (Join-Path $env:LOCALAPPDATA 'Android\Sdk')
) | Where-Object { -not [string]::IsNullOrWhiteSpace($_) } | Select-Object -Unique

$androidSdkRoot = $sdkCandidates |
    Where-Object { Test-Path -LiteralPath (Join-Path $_ 'platform-tools\adb.exe') -PathType Leaf } |
    Select-Object -First 1
if ([string]::IsNullOrWhiteSpace($androidSdkRoot)) {
    throw "SDK Android introuvable. Chemins contrôlés : $($sdkCandidates -join ', ')"
}
$androidSdkRoot = (Resolve-Path -LiteralPath $androidSdkRoot).Path
$adbPath = Join-Path $androidSdkRoot 'platform-tools\adb.exe'

$javaCandidates = @($env:JAVA_HOME)
$androidStudioJbr = Join-Path $env:ProgramFiles 'Android\Android Studio\jbr'
$javaCandidates += $androidStudioJbr
$javaCommand = Get-Command java.exe -ErrorAction SilentlyContinue
if ($null -ne $javaCommand) {
    $javaCandidates += Split-Path -Parent (Split-Path -Parent $javaCommand.Source)
}
$javaHomeRoot = $javaCandidates |
    Where-Object {
        -not [string]::IsNullOrWhiteSpace($_) -and
        (Test-Path -LiteralPath (Join-Path $_ 'bin\java.exe') -PathType Leaf)
    } |
    Select-Object -Unique |
    Select-Object -First 1
if ([string]::IsNullOrWhiteSpace($javaHomeRoot)) {
    throw 'Java introuvable. Installez Android Studio ou définissez JAVA_HOME vers un JDK valide.'
}
$javaHomeRoot = (Resolve-Path -LiteralPath $javaHomeRoot).Path

$deviceState = (& $adbPath -s $Serial get-state 2>&1 | Out-String).Trim()
if ($LASTEXITCODE -ne 0 -or $deviceState -ne 'device') {
    throw "Appareil ADB indisponible : $Serial ($deviceState)"
}

$qemu = (& $adbPath -s $Serial shell getprop ro.kernel.qemu | Out-String).Trim()
$isEmulator = $Serial.StartsWith('emulator-', [System.StringComparison]::OrdinalIgnoreCase) -or $qemu -eq '1'
if (-not $isEmulator -and -not $AllowDisposablePhysicalDevice) {
    throw @"
Refus : $Serial est un appareil physique. Le runner Android Gradle Plugin
désinstalle Maer Chat à la fin des tests connected* et supprime ses données
privées. Utilisez un émulateur ou un appareil jetable explicitement autorisé avec
-AllowDisposablePhysicalDevice.
"@
}

if (-not $isEmulator) {
    Write-Warning 'Appareil physique jetable autorisé : ses données Maer Chat seront supprimées.'
}

$previousSerial = $env:ANDROID_SERIAL
$previousAndroidHome = $env:ANDROID_HOME
$previousAndroidSdkRoot = $env:ANDROID_SDK_ROOT
$previousJavaHome = $env:JAVA_HOME
$gradleExitCode = $null
try {
    $env:ANDROID_SERIAL = $Serial
    $env:ANDROID_HOME = $androidSdkRoot
    $env:ANDROID_SDK_ROOT = $androidSdkRoot
    $env:JAVA_HOME = $javaHomeRoot
    $gradleArguments = @('--no-daemon', '--console=plain', 'connectedConversationsFreeDebugAndroidTest')
    if (-not $isEmulator) {
        $gradleArguments += '-PallowDisposableConnectedTests=true'
    }
    & (Join-Path $PSScriptRoot '..\gradlew.bat') @gradleArguments
    $gradleExitCode = $LASTEXITCODE
} finally {
    $env:ANDROID_SERIAL = $previousSerial
    $env:ANDROID_HOME = $previousAndroidHome
    $env:ANDROID_SDK_ROOT = $previousAndroidSdkRoot
    $env:JAVA_HOME = $previousJavaHome
}

if ($gradleExitCode -ne 0) {
    throw "Les tests connectés ont échoué (code $gradleExitCode)."
}

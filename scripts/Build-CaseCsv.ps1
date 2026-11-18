<#
.SYNOPSIS
  Builds (or checks) the master CSV of every test case document in docs/test-cases.

.DESCRIPTION
  The markdown documents are the source of truth.
    pwsh scripts/Build-CaseCsv.ps1           writes docs/test-cases/<module>.csv next to every <module>.md
    pwsh scripts/Build-CaseCsv.ps1 -Check    only reports if a CSV is out of date (exit code 1 if so)
  A row that did not change keeps its "Last Updated" date; a new or changed row gets today's date.
  Do not edit the CSV files by hand. Works in Windows PowerShell 5.1 and PowerShell 7.
#>
param([switch]$Check)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$casesDir = Join-Path $root 'docs/test-cases'
$today = (Get-Date).ToString('dd-MM-yyyy')
$columns = @('ID', 'Module', 'Sub-module', 'Title', 'Priority', 'Type', 'Preconditions', 'Test Data', 'Steps',
    'Expected Result', 'Automation Status', 'Test File', 'Test Method', 'Last Updated')

function Split-Row([string]$line) {
    $cells = [regex]::Split($line.Trim().Trim('|'), '(?<!\\)\|')
    return @($cells | ForEach-Object { $_.Trim().Replace('\|', '|') })
}

function Read-Cases([string]$path) {
    $module = [IO.Path]::GetFileNameWithoutExtension($path)
    $group = ''
    $rows = New-Object System.Collections.Generic.List[object]
    foreach ($line in [IO.File]::ReadAllLines($path, [Text.Encoding]::UTF8)) {
        if ($line -match '^# (.+) Test Cases\s*$') { $module = $Matches[1] }
        if ($line -match '^## (?:[A-Z]\. )?(.+)$') { $group = $Matches[1] }
        if ($line -notmatch '^\| TC_[A-Z0-9]+_\d+(\.\d+)? \|') { continue }
        $c = Split-Row $line
        if ($c.Count -ne 9) { throw "$([IO.Path]::GetFileName($path)): expected 9 columns, found $($c.Count): $($line.Substring(0, 60))" }
        if ($c[8] -notmatch '^(\w[\w ]*?) `([^`]+)::([^`]+)`$') { throw "Bad Automation cell for $($c[0]): $($c[8])" }
        $rows.Add([ordered]@{
                'ID' = $c[0]; 'Module' = $module; 'Sub-module' = $group; 'Title' = $c[1]; 'Priority' = $c[2]; 'Type' = $c[3]
                'Preconditions' = $c[4]; 'Test Data' = $c[5]; 'Steps' = ($c[6] -replace '<br>', ' '); 'Expected Result' = $c[7]
                'Automation Status' = $Matches[1]; 'Test File' = $Matches[2]; 'Test Method' = $Matches[3]
            })
    }
    return , $rows
}

function Quote([string]$v) { return '"' + $v.Replace('"', '""') + '"' }

$stale = 0
$total = 0
foreach ($md in Get-ChildItem $casesDir -Filter '*.md' | Sort-Object Name) {
    $target = [IO.Path]::ChangeExtension($md.FullName, '.csv')
    $old = @{}
    if (Test-Path $target) {
        foreach ($r in (Import-Csv $target -Encoding UTF8)) { $old[$r.ID] = $r }
    }
    $rows = Read-Cases $md.FullName
    $total += $rows.Count
    $changed = ($rows.Count -ne $old.Count)
    $lines = New-Object System.Collections.Generic.List[string]
    $lines.Add(($columns | ForEach-Object { Quote $_ }) -join ',')
    foreach ($row in $rows) {
        $date = $today
        $previous = $old[$row['ID']]
        $same = $null -ne $previous
        if ($same) { foreach ($k in $row.Keys) { if ($previous.$k -ne $row[$k]) { $same = $false; break } } }
        if ($same) { $date = $previous.'Last Updated' } else { $changed = $true }
        $values = @($row.Values) + $date
        $lines.Add(($values | ForEach-Object { Quote ([string]$_) }) -join ',')
    }
    $name = [IO.Path]::GetFileName($target)
    if ($Check) {
        if ($changed) { $stale++; Write-Host "OUT OF DATE: docs/test-cases/$name (run: pwsh scripts/Build-CaseCsv.ps1)" }
    }
    elseif ($changed -or -not (Test-Path $target)) {
        [IO.File]::WriteAllText($target, ($lines -join "`r`n") + "`r`n", (New-Object Text.UTF8Encoding($true)))
        Write-Host "written docs/test-cases/$name ($($rows.Count) cases)"
    }
    else { Write-Host "unchanged docs/test-cases/$name ($($rows.Count) cases)" }
}
if ($Check) { Write-Host "$total cases checked, $stale master CSV file(s) out of date" }
if ($stale -gt 0) { exit 1 }

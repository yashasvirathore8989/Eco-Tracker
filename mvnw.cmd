@echo off
setlocal
if not defined JAVA_HOME (
    if exist "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.4\jbr" (
        set "JAVA_HOME=C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.4\jbr"
    )
)
if exist "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.4\plugins\maven-plugin\lib\maven3\bin\mvn.cmd" (
    call "C:\Program Files\JetBrains\IntelliJ IDEA 2026.2.4\plugins\maven-plugin\lib\maven3\bin\mvn.cmd" %*
) else (
    call mvn %*
)

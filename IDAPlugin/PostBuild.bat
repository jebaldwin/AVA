@echo off

if %1 == DEBUG goto Debug
if %1 == RELEASE goto Release

echo You must run PostBuild.bat with DEBUG or RELEASE as arguments.
goto End

:Debug
echo Copy output files (.plw and .pdb) to IDA pro plugins path. (DEBUG)
copy /y ".\Debug\SequenceDiagramJavaPlugin.plw" "%IDAPATH%\plugins"
copy /y ".\Debug\SequenceDiagramJavaPlugin.pdb" "%IDAPATH%\plugins"
goto End

:Release
echo Copy output files (.plw) to IDA pro plugins path. (RELEASE)
copy /y ".\Release\SequenceDiagramJavaPlugin.plw" "%IDAPATH%\plugins"
goto End

:Common

:End

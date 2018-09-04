@echo off
SET sdir=%~dp0
SET bdir=%sdir%\..
if not exist "%bdir%\out" mkdir "%bdir%\out"
javac "%bdir%"\src\io\josephkim\BigTalkCore.java "%bdir%"\src\io\josephkim\BigTalkDesktop.java "%bdir%"\src\io\josephkim\BigTalkSwing.java -Xlint -d "%bdir%"\out

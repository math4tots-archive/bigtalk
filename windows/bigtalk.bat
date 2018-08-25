@echo off
SET sdir=%~dp0
SET bdir=%sdir%\..
if not exist "%bdir%\out" build-bigtalk
set BIGTALK_PATH=%bdir%\modules
java -ea -cp "%bdir%"\out io.josephkim.BigTalkDesktop %*

@echo off

@REM #
@REM # Licensed to the Apache Software Foundation (ASF) under one
@REM # or more contributor license agreements.  See the NOTICE file
@REM # distributed with this work for additional information
@REM # regarding copyright ownership.  The ASF licenses this file
@REM # to you under the Apache License, Version 2.0 (the
@REM # "License"); you may not use this file except in compliance
@REM # with the License.  You may obtain a copy of the License at
@REM #
@REM #   http://www.apache.org/licenses/LICENSE-2.0
@REM #
@REM # Unless required by applicable law or agreed to in writing,
@REM # software distributed under the License is distributed on an
@REM # "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM # KIND, either express or implied.  See the License for the
@REM # specific language governing permissions and limitations
@REM # under the License.
@REM #

@REM Decide how to startup depending on the version of windows

@REM -- Win98ME
if NOT "%OS%"=="Windows_NT" goto Win9xArg

@REM -- 4NT shell
if "%eval[2+2]" == "4" goto 4NTArgs

@REM -- Regular WinNT shell
set MAVEN_CMD_LINE_ARGS=%*
goto endInit

@REM The 4NT Shell from jp software
:4NTArgs
set MAVEN_CMD_LINE_ARGS=%$
goto endInit

:Win9xArg
@REM Slurp the command line arguments.  This loop allows for an unlimited number
@REM of agruments (up to the command line limit, anyway).
set MAVEN_CMD_LINE_ARGS=
:Win9xApp
if %1a==a goto endInit
set MAVEN_CMD_LINE_ARGS=%MAVEN_CMD_LINE_ARGS% %1
shift
goto Win9xApp

@REM Reaching here means variables are defined and arguments have been captured
:endInit

call mvn -Denv=test clean:clean install %MAVEN_CMD_LINE_ARGS%

@echo off
REM ============================================================================
REM generate_proto.bat - Copies .proto definitions into this service and compiles
REM                     them into the Python gRPC stubs required by the GenAI
REM                     service.
REM Usage: double-click or run from PowerShell / cmd
REM ============================================================================

SETLOCAL

REM ---- Configuration ---------------------------------------------------------
REM Source directory that contains proto files (relative to this script)
set "PROTO_SRC_DIR=%~dp0..\proto"

REM Destination directory inside this service where generated code will live
set "DST_DIR=%~dp0app\proto"

REM ---- Prepare destination ----------------------------------------------------
if not exist "%DST_DIR%" mkdir "%DST_DIR%"

REM ---- Copy proto files -------------------------------------------------------
xcopy /Y /Q "%PROTO_SRC_DIR%\*.proto" "%DST_DIR%\"

REM ---- Generate Python stubs --------------------------------------------------
python -m grpc_tools.protoc ^
  -I "%DST_DIR%" ^
  --python_out="%DST_DIR%" ^
  --grpc_python_out="%DST_DIR%" ^
  "%DST_DIR%\ai.proto"

IF %ERRORLEVEL% NEQ 0 (
    echo Failed to generate gRPC stubs. Ensure grpcio-tools is installed: pip install grpcio grpcio-tools
    EXIT /B %ERRORLEVEL%
)

echo [OK] Python gRPC stubs generated in %DST_DIR%

ENDLOCAL

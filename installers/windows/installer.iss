#define MyAppName "Kovadlo"
#define MyAppURL "https://t.me/cubeuniverseua"
#define MyAppVersion "1.0.0"

[Setup]
AppId={{9422F016-A0F0-4150-B2C5-DBA87B4CAF9D}
AppName={#MyAppName}
AppVerName={#MyAppName}
AppPublisher={#MyAppName}
AppVersion={#MyAppVersion}
VersionInfoVersion={#MyAppVersion}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}
DefaultDirName={userappdata}\{#MyAppName}
DisableDirPage=auto
DisableWelcomePage=no
DefaultGroupName={#MyAppName}
DisableProgramGroupPage=yes
PrivilegesRequired=lowest
Compression=lzma2
SetupIconFile=..\..\src\main\resources\logo.ico
SolidCompression=yes
OutputBaseFilename={#MyAppName}
UninstallDisplayIcon={app}\{#MyAppName}.exe
UninstallDisplayName={#MyAppName} Setup
WizardStyle=modern
ChangesAssociations=yes

[Run]
Filename: {tmp}\7za.exe; Parameters: "x ""{tmp}\jre.zip"" -o""{app}\java"" * -r -aoa"; Flags: runhidden runascurrentuser
Filename: {app}\java\java-runtime-gamma\bin\java.exe; Parameters: "-jar {app}\Kovadlo.jar"; Description: {cm:LaunchProgram,{#MyAppName}}; Flags: nowait postinstall skipifsilent

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: checkedonce

[Files]
Source: "7za.exe"; DestDir: "{tmp}"; Flags: deleteafterinstall
Source: "{tmp}\{#MyAppName}.jar"; DestDir: "{app}"; Flags: external ignoreversion
Source: "{tmp}\jre.zip"; DestDir: "{tmp}"; Flags: external deleteafterinstall;

[Components]
Name: "launcher"; Description: "Запускач лаунчера + Java 17"; ExtraDiskSpaceRequired: 152268800; Types: full compact custom; Flags: fixed

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"
Name: "ukrainian"; MessagesFile: Ukrainian.isl

[Icons]
Name: "{group}\{#MyAppName}"; Filename: "{app}\java\java-runtime-gamma\bin\java.exe"; Parameters: "-jar {app}\Kovadlo.jar"; IconFilename:"{uninstallexe}"
Name: "{userdesktop}\{#MyAppName}"; Filename: "{app}\java\java-runtime-gamma\bin\java.exe"; Parameters: "-jar {app}\Kovadlo.jar"; Tasks: desktopicon; IconFilename:"{uninstallexe}"
Name: "{group}\{cm:UninstallProgram,{#MyAppName}}"; Filename: "{uninstallexe}"

[InstallDelete]
Type: filesandordirs; Name: "{app}\java"

[UninstallDelete]
Type: filesandordirs; Name: "{app}\java"

[Code]
var
  DownloadPage: TDownloadWizardPage;

procedure InitializeWizard;
begin
  DownloadPage := CreateDownloadPage(SetupMessage(msgWizardPreparing), SetupMessage(msgPreparingDesc), nil);
end;

procedure CurStepChanged(CurStep: TSetupStep);
begin
  if (CurStep = ssPostInstall) then begin
    if not RenameFile(ExpandConstant('{app}') + '\java\jdk-17.0.3+7-jre', ExpandConstant('{app}/java/java-runtime-gamma')) then begin
      MsgBox('Не вдалося переіменувати JRE', mbError, MB_OK);
      WizardForm.Close;
    end
  end
end;

function NextButtonClick(CurPageID: Integer): Boolean;
begin
  if CurPageID = wpReady then begin
    DownloadPage.Clear;

    DownloadPage.Add('https://omhms.com/share/Kovadlo.jar', '{#MyAppName}.jar', '');

 
    if IsWin64 then begin
      DownloadPage.Add('https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.3%2B7/OpenJDK17U-jre_x64_windows_hotspot_17.0.3_7.zip', 'jre.zip', 'd77745fdb57b51116f7b8fabd7d251067edbe3c94ea18fa224f64d9584b41a97');
    end else begin
      DownloadPage.Add('https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.3%2B7/OpenJDK17U-jre_x86-32_windows_hotspot_17.0.3_7.zip', 'jre.zip', 'e29e311e4200a32438ef65637a75eb8eb09f73a37cef3877f08d02b6355cd221');
    end;

    DownloadPage.Show;
    try
      try
        DownloadPage.Download;
        Result := True;
      except
          SuppressibleMsgBox(AddPeriod(GetExceptionMessage), mbCriticalError, MB_OK, IDOK);
        Result := False;
      end;
    finally
      DownloadPage.Hide;
    end;
  end else
    Result := True;
end;


procedure CurUninstallStepChanged(CurUninstallStep: TUninstallStep);
begin
  if CurUninstallStep = usPostUninstall then
  begin
    if MsgBox('Ви хочете видалити всі дані лаунчера (збірки, завантаження, збереження тощо)?', mbConfirmation, MB_YESNO) = IDYES then begin
        if DelTree(ExpandConstant('{app}/'), True, True, True) then
        begin
        end else
        begin
            MsgBox('Помилка при видаленні даних користувача. Будь ласка, видаліть їх вручну.', mbError, MB_OK);
        end;
    end;
  end;
end;

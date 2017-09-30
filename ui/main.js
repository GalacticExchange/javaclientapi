const electron = require('electron');
const app = electron.app;
const BrowserWindow = electron.BrowserWindow;
const {dialog} = require('electron');
const path = require('path');
const eventHandlers = require('./events_handler');
const fixPathMac = require('fix-path');

global['programProperties'] = require('./properties').loadProperties();
const appMenu = require('./applicationMenu');

// Keep a global reference of the window object, if you don't, the window will
// be closed automatically when the JavaScript object is garbage collected.
let mainWindow;
function createWindow() {
    mainWindow = new BrowserWindow({title: "ClusterGX", icon: path.join(__dirname, '/icons/gexlogo.png'), show: false});
    mainWindow.maximize();
    mainWindow.show();
    require('./context_menu')({
        window: mainWindow
    });

    //mainWindow.openDevTools();
    mainWindow.loadURL('http://' + global['programProperties'].websiteHost);

    mainWindow.webContents.on("did-fail-load", function (event, errCode, errDescription) {
        if (-3 === errCode || 0 === errCode) return; //redirect throw -3 error. 0 - when page haven't loaded completely, but another page start loading

        dialog.showMessageBox({
                type: 'error', buttons: ['Reload page', 'Cancel'], defaultId: 0,
                title: 'Can not load page', message: 'Can not load page. Error: ' + errCode + ' ' + errDescription
            },
            function (response) {
                if (response === 0) {
                    mainWindow.webContents.reload();
                }
            });
    });

    // Emitted when the window is closed.
    mainWindow.on('closed', function () {
        // Dereference the window object, usually you would store windows
        // in an array if your app supports multi windows, this is the time
        // when you should delete the corresponding element.
        mainWindow = null;
    })
}


// This method will be called when Electron has finished
// initialization and is ready to create browser windows.
// Some APIs can only be used after this event occurs.
app.on('ready', function () {
    appMenu.createAppMenu();
    fixPathMac();
    eventHandlers.init();
    createWindow();
});

// Quit when all windows are closed.
app.on('window-all-closed', function () {
    // On OS X it is common for applications and their menu bar
    // to stay active until the user quits explicitly with Cmd + Q
    if (process.platform !== 'darwin') {
        app.quit();
    }
});

app.on('activate', function () {
    // On OS X it's common to re-create a window in the app when the
    // dock icon is clicked and there are no other windows open.
    if (mainWindow === null) {
        createWindow();
    }
});


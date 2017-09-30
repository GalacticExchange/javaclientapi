'use strict';

const {Menu} = require('electron');

const template = [
    {
        label: 'Edit',
        submenu: [
            {
                role: 'undo'
            },
            {
                role: 'redo'
            },
            {
                type: 'separator'
            },
            {
                role: 'cut'
            },
            {
                role: 'copy'
            },
            {
                role: 'paste'
            },
            {
                role: 'pasteandmatchstyle'
            },
            {
                role: 'delete'
            },
            {
                role: 'selectall'
            },
            //create local shortcuts for terminal
            {
                role: 'copy',
                accelerator: (function () {
                    return process.platform === 'darwin' ? undefined : 'Ctrl+Shift+C';
                })(),
                visible: false
            },
            {
                role: 'paste',
                accelerator: (function () {
                    return process.platform === 'darwin' ? undefined : 'Ctrl+Shift+V';
                })(),
                visible: false
            }
        ]
    },
    {
        label: 'View',
        submenu: [
            {
                label: 'Reload',
                accelerator: (function () {
                    if (process.platform === 'darwin') {
                        return 'Cmd+R'
                    } else {
                        return 'F5'
                    }
                })(),
                click(item, focusedWindow) {
                    if (focusedWindow) focusedWindow.reload();
                }
            },
            {
                label: 'Go back',
                click(item, focusedWindow) {
                    if (focusedWindow) focusedWindow.webContents.goBack();
                }
            },
            {
                label: 'Go Forward',
                click(item, focusedWindow) {
                    if (focusedWindow) focusedWindow.webContents.goForward();
                }
            },
            {
                type: 'separator'
            },
            {
                label: 'Toggle Full Screen',
                accelerator: (function () {
                    if (process.platform === 'darwin') {
                        return 'Ctrl+Command+F'
                    } else {
                        return 'F11'
                    }
                })(),
                click: function (item, focusedWindow) {
                    if (focusedWindow) {
                        focusedWindow.setFullScreen(!focusedWindow.isFullScreen())
                    }
                }
            },
            {
                label: 'Toggle Developer Tools',
                accelerator: process.platform === 'darwin' ? 'Alt+Command+I' : 'F12',
                click(item, focusedWindow) {
                    if (focusedWindow)
                        focusedWindow.webContents.toggleDevTools();
                }
            }
        ]
    },
    {
        role: 'window',
        submenu: [
            {
                role: 'minimize'
            },
            {
                role: 'close'
            }
        ]
    },
    {
        role: 'help',
        submenu: [
            {
                label: 'Learn More',
                click() {
                    require('electron').shell.openExternal('http://galacticexchange.io/');
                }
            }
        ]
    }
];

if (process.platform === 'darwin') {
    const name = require('electron').app.getName();
    template.unshift({
        label: name,
        submenu: [
            {
                role: 'about'
            },
            {
                type: 'separator'
            },
            {
                role: 'services',
                submenu: []
            },
            {
                type: 'separator'
            },
            {
                role: 'hide'
            },
            {
                role: 'hideothers'
            },
            {
                role: 'unhide'
            },
            {
                type: 'separator'
            },
            {
                role: 'quit'
            }
        ]
    });
    // Window menu.
    template[3].submenu = [
        {
            label: 'Close',
            accelerator: 'CmdOrCtrl+W',
            role: 'close'
        },
        {
            label: 'Minimize',
            accelerator: 'CmdOrCtrl+M',
            role: 'minimize'
        },
        {
            label: 'Zoom',
            role: 'zoom'
        },
        {
            type: 'separator'
        },
        {
            label: 'Bring All to Front',
            role: 'front'
        }
    ];
}

module.exports = {
    createAppMenu: function () {
        Menu.setApplicationMenu(Menu.buildFromTemplate(template));
    }
};
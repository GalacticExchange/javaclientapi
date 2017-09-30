'use strict';

const {ipcMain} = require('electron');
const gexdClient = require('./rest').getGexdClient();
const childProcess = require('child_process');
const toPlainObject = require('lodash/toPlainObject');
const cloneDeep = require('lodash/cloneDeep');
const isObject = require('lodash/isObject');
const SshClient = require('ssh2').Client;
const StringDecoder = require('string_decoder').StringDecoder;
const stripAnsi = require('strip-ansi');
const socks = require('socksv5');
const sshConnections = new Map();
const os = require('os');
const iconv = require('iconv-lite');
let consoleEncoding;
const http = require('http');
const urlLib = require('url');
const fs = require('fs');
const restify = require('restify-clients');
const ssh2Utils = require('ssh2').utils;

function createResponseEventName(suffics, arg) {
    const strPath = isObject(arg.url) ? arg.url.path : arg.url;
    return suffics + '-resp-' + arg.requestType + '-' + strPath + '-' + arg.time;
}

function moveInheritPropsToObject(obj/*, ... property names*/) {
    if (!obj) {
        return obj;
    }

    const newObj = toPlainObject(obj);
    for (let i = 1; i < arguments.length; i++)
        newObj[arguments[i]] = obj[arguments[i]];
    return newObj;
}

function emptySshConnection(connectionSettings, afterExecute, proxy) {
    const conn = new SshClient();
    conn.on('ready', function () {
        conn.exec('pwd', function (err, stream) {
            if (err) {
                afterExecute();
                conn.end();
                return;
            }
            stream.on('close', function () {
                afterExecute();
                conn.end();
            }).on('data', function (data) {
            }).stderr.on('data', function (data) {
            });
        });
    }).on('error', function () {
        afterExecute();
        conn.end();
    });

    if (proxy) {
        socks.connect({
            host: connectionSettings.host,
            port: connectionSettings.port,
            proxyHost: proxy.host,
            proxyPort: proxy.port,
            auths: getProxyAuth(proxy)
        }, function (socket) {
            conn.connect({
                username: connectionSettings.username,
                password: connectionSettings.password,
                sock: socket
            });
        }).on('error', function (err) {
            console.log('Failed to connect to socks5 proxy: ' + err.message);
            afterExecute();
        });
    } else {
        conn.connect(connectionSettings);
    }
}

function getProxyAuth(proxy) {
    return proxy.username ? [socks.auth.UserPassword(proxy.username, proxy.password)] : [socks.auth.None()];
}

function getConsoleEncoding() {
    let encoding = 'utf8';
    if (os.platform() === 'win32') {
        try {
            let output = childProcess.execSync('CHCP', {encoding: 'utf8'});
            let words = output.trim().split(/\s+/);
            let encodingTemp = words[words.length - 1];
            encoding = iconv.encodingExists(encodingTemp) ? encodingTemp : encoding;
        } catch (err) {
            console.log(err.message);
        }
    } else {
        try {
            let encodingTemp = childProcess.execSync('locale charmap', {encoding: 'utf8'}).trim();
            encoding = iconv.encodingExists(encodingTemp) ? encodingTemp : encoding;
        } catch (err) {
            console.log(err.message);
        }
    }
    return encoding;
}

function constructRequestConf(arg) {
    if (arg.token) {
        if (isObject(arg.url)) {
            if (arg.url.headers) {
                arg.url.headers.token = arg.token;
            } else {
                arg.url.headers = {'token': arg.token};
            }
            return arg.url;
        } else {
            return {path: arg.url, headers: {'token': arg.token}};
        }
    } else {
        return arg.url;
    }
}

module.exports = {
    init: function () {
        ipcMain.on('gexd-post-/nodes', (event, arg) => {
            const token = arg.token;
            delete arg.token;
            gexdClient.post({path: '/nodes', headers: {'token': token}}, arg, function (err, req, res, data) {
                if (err) {
                    event.sender.send('show-message', {
                        header: 'Error',
                        description: 'Failed to install local node.\n' + err.message
                    });
                }
            });
        });

        //arg{requestType: requestType, url: url, data: dataObject}
        ipcMain.on('gexd-req', (event, arg) => {
            if (arg.requestType === 'get') {
                gexdClient.get(constructRequestConf(arg), function (err, req, res, data) {
                    event.sender.send(createResponseEventName('gexd', arg), {err, req, res, data});
                });
            } else if (arg.requestType === 'post') {
                gexdClient.post(constructRequestConf(arg), arg.data, function (err, req, res, data) {
                    event.sender.send(createResponseEventName('gexd', arg), {err, req, res, data});
                });
            } else if (arg.requestType === 'put') {
                gexdClient.put(constructRequestConf(arg), arg.data, function (err, req, res, data) {
                    event.sender.send(createResponseEventName('gexd', arg), {err, req, res, data});
                });
            } else if (arg.requestType === 'del') {
                gexdClient.del(constructRequestConf(arg), function (err, req, res) {
                    event.sender.send(createResponseEventName('gexd', arg), {err, req, res});
                });
            } else {
                event.sender.send(createResponseEventName('gexd', arg), {
                    err: arg.requestType ? new Error('Incorrect request type ' + arg.requestType) : new Error('Empty request type')
                });
            }
        });


        ipcMain.on('ext-req', (event, arg) => {
            const client = restify.createJsonClient({
                url: arg.serverUrl
            });
            if (arg.requestType === 'get') {
                client.get(constructRequestConf(arg), function (err, req, res, data) {
                    event.sender.send(createResponseEventName('ext', arg), {err, req, res, data});
                });
            } else if (arg.requestType === 'post') {
                client.post(constructRequestConf(arg), arg.data, function (err, req, res, data) {
                    event.sender.send(createResponseEventName('ext', arg), {err, req, res, data});
                });
            } else if (arg.requestType === 'put') {
                client.put(constructRequestConf(arg), arg.data, function (err, req, res, data) {
                    event.sender.send(createResponseEventName('ext', arg), {err, req, res, data});
                });
            } else if (arg.requestType === 'del') {
                client.del(constructRequestConf(arg), function (err, req, res) {
                    event.sender.send(createResponseEventName('ext', arg), {err, req, res});
                });
            } else {
                event.sender.send(createResponseEventName('ext', arg), {
                    err: arg.requestType ? new Error('Incorrect request type ' + arg.requestType) : new Error('Empty request type')
                });
            }
        });

        ipcMain.on('exec', (event, arg) => {
            if (!consoleEncoding) {
                consoleEncoding = getConsoleEncoding();
            }
            let tokenArg = arg.token ? ' --token=' + arg.token : '';
            let command = arg.command + tokenArg;
            childProcess.exec(command, {encoding: undefined}, function (err, stdout, stderr) {
                stdout = stdout ? iconv.decode(stdout, consoleEncoding) : stdout;
                stderr = stderr ? iconv.decode(stderr, consoleEncoding) : stderr;

                if (err && err.message) err.message = stripAnsi('Command failed: ' + arg.command + '\n' + stderr);

                event.sender.send('exec-resp-' + arg.command + '-' + arg.time,
                    {err: moveInheritPropsToObject(err, 'message'), stdout, stderr});
            });
        });

        ipcMain.on('isSshKeyEncrypted', (event, key) => {
            let keyEncrypted, err;
            try {
                let parsedKey = ssh2Utils.parseKey(key);
                if (parsedKey instanceof Error) {
                    err = moveInheritPropsToObject(parsedKey, 'message');
                } else {
                    keyEncrypted = !!parsedKey.encryption;
                }
            } catch (errCatch) {
                err = moveInheritPropsToObject(errCatch, 'message');
            }

            event.returnValue = {err, keyEncrypted};
        });

        ipcMain.on('ssh-connect', (event, arg) => {
            const connection = new SshClient();
            connection.on('ready', function () {
                connection.shell(function (err, stream) {
                    if (err) {
                        event.sender.send('ssh-output-' + arg.id, err.message + '\r\n');
                        connection.end();
                        return;
                    }

                    const textDecoder = new StringDecoder("utf-8");
                    stream.on('close', function () {
                        connection.end();
                    }).on('data', function (data) {
                        event.sender.send('ssh-output-' + arg.id, textDecoder.write(data));
                    }).stderr.on('data', function (data) {
                        event.sender.send('ssh-output-' + arg.id, textDecoder.write(data));
                    });
                    sshConnections.set(arg.id, {connection, stream});
                });
            }).on('error', function (err) {
                event.sender.send('ssh-output-' + arg.id, err.message + '\r\n');
                connection.end();
            });

            if (arg.proxy) {
                emptySshConnection(arg.connectionSettings, () => {
                    socks.connect({
                        host: arg.connectionSettings.host,
                        port: arg.connectionSettings.port,
                        proxyHost: arg.proxy.host,
                        proxyPort: arg.proxy.port,
                        auths: getProxyAuth(arg.proxy)
                    }, function (socket) {
                        connection.connect({
                            username: arg.connectionSettings.username,
                            password: arg.connectionSettings.password,
                            sock: socket
                        });
                    }).on('error', function (err) {
                        event.sender.send('ssh-output-' + arg.id, err.message + '\r\n');
                    });
                }, arg.proxy);
            } else {
                emptySshConnection(arg.connectionSettings, () => {
                    connection.connect(arg.connectionSettings);
                });
            }
        });

        ipcMain.on('ssh-close', (event, arg) => {
            let connection = sshConnections.get(arg);
            if (connection) {
                connection.connection.end();
                sshConnections.delete(arg);
            }
        });

        ipcMain.on('ssh-close-all', () => {
            sshConnections.forEach((conn) => {
                conn.connection.end();
            });
            sshConnections.clear();
        });

        ipcMain.on('ssh-cmd', (event, arg) => {
            let connection = sshConnections.get(arg.id);
            if (connection) {
                connection.stream.write(arg.key);
            }
        });

        ipcMain.on('check-conn', (event, arg) => {
            let url = urlLib.parse(arg.url);
            let connTimeout = arg.connTimeout ? arg.connTimeout : 5000;
            http.get({hostname: url.hostname, port: url.port, timeout: connTimeout}, (res) => {
                res.destroy();
                event.sender.send('check-conn-resp-' + arg.time, {res: true});
            }).on('error', (e) => {
                event.sender.send('check-conn-resp-' + arg.time, {res: false});
            });
        });

        ipcMain.on('dwn-file', (event, args) => {
            const resEventName = 'dwn-file-resp-' + args.time;

            const file = fs.createWriteStream(args.filePath);
            http.get(args.url, function (response) {
                const successRequest = response.statusCode === 200;

                if (successRequest) {
                    file.on('finish', function () {
                        file.close(function () {
                            event.sender.send(resEventName, {savePath: file.path});
                        });
                    });
                    file.on('error', function (err) {
                        fs.unlink(args.filePath);
                        event.sender.send(resEventName, {err: moveInheritPropsToObject(err, 'message')});
                    });
                    response.pipe(file);
                } else {
                    fs.unlink(args.filePath);
                    response.setEncoding('utf8');
                    response.on('data', function (data) {
                        const isJson = response.headers['content-type'] && response.headers['content-type'].includes('application/json');
                        const parsedData = isJson && data ? JSON.parse(data) : undefined;
                        const err = {message: (parsedData && parsedData.message ? parsedData.message : 'Status code: ' + response.statusCode)};
                        event.sender.send(resEventName, {err});
                    });
                }
            }).on('error', function (err) {
                fs.unlink(args.filePath);
                event.sender.send(resEventName, {err: moveInheritPropsToObject(err, 'message')});
            });
        });
    }
};
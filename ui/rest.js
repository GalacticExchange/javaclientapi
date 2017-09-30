'use strict';

const os = require('os');
const fs = require('fs');
const path = require('path');
const restify = require('restify-clients');

let gexdClientparam;

function getGexdPort() {
    let gexdPort = 48746;
    try {
        gexdPort = JSON.parse(fs.readFileSync(path.join(os.platform() === 'win32' ? process.env.ProgramData : os.homedir(),
            '.gex', 'gexd.json'), 'utf8')).webServerPort;
    } catch (err) {
        console.log(err);
    }
    return gexdPort;
}

module.exports = {
    getGexdClient: function (token) {
        if (gexdClientparam === undefined) {
            gexdClientparam = restify.createJsonClient({
                url: 'http://localhost:' + getGexdPort()
            });
        }
        if (token) {
            gexdClientparam.headers.token = token;
        }
        return gexdClientparam;
    }
};

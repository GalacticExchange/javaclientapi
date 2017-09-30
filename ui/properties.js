'use strict';

const os = require('os');
const fs = require('fs');
const path = require('path');
const properties = require('properties');
const {app} = require('electron');

function loadProperties() {
    let propertiesPath = '/etc/gex/config.properties';
    if (os.platform() === 'win32') {
        let installProps = properties.parse(fs.readFileSync(path.join(process.env.ProgramData, '.gex', 'install.info'),
            'utf8'));
        propertiesPath = path.join(installProps.installation_path, propertiesPath);
    }

    let programProperties = fs.readFileSync(propertiesPath, 'utf8');
    let props = properties.parse(programProperties);
    props.version = app.getVersion();
    return props;
}


module.exports = {
    loadProperties: loadProperties
};

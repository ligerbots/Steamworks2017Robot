var ntcore = global.ntcore;
var SmartDashboard = global.SmartDashboard;
var console = SmartDashboard.console;

class SteamworksField extends Widget {
    render() {
        var self = this;
        console.log(exports.pluginDirectory);
        
        var fc = document.createElement('div');
        fc.classList.add('field-container');
        var field = document.createElement('img');
        field.classList.add('field');
        field.src = 'file:///' + exports.pluginDirectory + '/steamworks-field.png';
        fc.appendChild(field);
        this._robot = document.createElement('img');
        this._robot.classList.add('robot');
        this._robot.src = 'file:///' + exports.pluginDirectory + '/spaceship.png';
        fc.appendChild(this._robot);
        
        this.dom.appendChild(fc);
    }

    _update(k, v) {}

    attachListeners() {
        var self = this;
        var objectRoot = this.table.getTablePath() + "/" + this.key;
        this._valTable = ntcore.getTable(objectRoot);
        this._mainListener = function (k, v) {
            self.update();
        };
        this._valTable.onChange("x", this._mainListener);
        this._valTable.onChange("y", this._mainListener);
        this._valTable.onChange("direction", this._mainListener);
    }

    change(evt) {
        
    }

    update() {
        const PIXELS_PER_INCH = 494.0 / (54 * 12);
        const CENTER_X = 358;
        const CENTER_Y = 156;
        this._robot.style.top = (-this._valTable.get("y") * PIXELS_PER_INCH + CENTER_Y) + "px";
        this._robot.style.left = (this._valTable.get("x") * PIXELS_PER_INCH + CENTER_X) + "px";
        this._robot.style.transform = `translateX(-50%) translateY(-50%) rotate(${this._valTable.get("direction") - 90}deg)`;
    }
}

SmartDashboard.registerWidget(SteamworksField, "object", {objectDetect: ["_swfield"]});

exports.info = {
    name: "SteamworksField",
    version: "0.1.0",
    description: "Steamworks 2017 field and robot position display"
};
exports.assets = {
    icons: {
        "SteamworksField": "SteamworksField.png"
    },
    css: "SteamworksField.css"
};
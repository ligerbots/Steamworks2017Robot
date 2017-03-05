var ntcore = global.ntcore;
var SmartDashboard = global.SmartDashboard;
var console = SmartDashboard.console;

class SteamworksField extends Widget {
    render() {
        var self = this;
        console.log(exports.pluginDirectory);
        
        var fc = document.createElement('div');
        fc.classList.add('field-container');
        this._canvas = document.createElement("canvas");
        this._canvas.width = 3047;
        this._canvas.height = 1258;
        this._canvas.addEventListener("pointerdown", this.pointerdown.bind(this));
        this._canvas.addEventListener("pointermove", this.pointermove.bind(this));
        this._canvas.addEventListener("pointerup", this.pointerup.bind(this));
        this._canvas.addEventListener("pointercancel", this.pointerup.bind(this));
        this._canvas.addEventListener("pointerout", this.pointerup.bind(this));
        this._canvas.addEventListener("pointerleave", this.pointerup.bind(this));
        fc.appendChild(this._canvas);
        
        this._field = new Image();
        this._field.src = 'file:///' + exports.pluginDirectory + '/field-highres.png';
        this._robot = new Image();
        this._robot.src = 'file:///' + exports.pluginDirectory + '/robot.png';
        
        this.dom.appendChild(fc);
        this.renderLoop();
    }

    _update(k, v) {}

    attachListeners() {
        var self = this;
        var objectRoot = this.table.getTablePath() + "/" + this.key;
        this._valTable = ntcore.getTable(objectRoot);/*
        this._mainListener = function (k, v) {
            self.update();
        };
        this._valTable.onChange("x", this._mainListener);
        this._valTable.onChange("y", this._mainListener);
        this._valTable.onChange("direction", this._mainListener);*/
    }

    change(evt) {
        
    }

    update() {
        //const PIXELS_PER_INCH = 494.0 / (54 * 12);
        //const CENTER_X = 358;
        //const CENTER_Y = 156;
        //this._robot.style.top = (-this._valTable.get("y") * PIXELS_PER_INCH + CENTER_Y) + "px";
        //this._robot.style.left = (this._valTable.get("x") * PIXELS_PER_INCH + CENTER_X) + "px";
        //this._robot.style.transform = `translateX(-50%) translateY(-50%) rotate(${this._valTable.get("direction") - 90}deg)`;
    }
    
    onNew() {
      this._w = 300;
      this._h = 300 * 1258 / 3047;
      this.dom.style.width = this._w + "px";
      this.dom.style.height = this._h + "px";
    }
    
    pointerdown(evt) {
      evt.preventDefault();
      if (this.pointerTracking) return;
      
      this.pointerTracking = evt.pointerId;
      console.log("down", evt.pointerId);
    }
    
    pointermove(evt) {
      evt.preventDefault();
      if (evt.pointerId == this.pointerTracking) {
        console.log("move", evt);
      }
    }
    
    pointerup(evt) {
      evt.preventDefault();
      if (evt.pointerId == this.pointerTracking) {
        console.log("up", evt.pointerId);
        this.pointerTracking = false;
      }
    }
    
    renderLoop() {
      if(this._destroyed) return;
      
      const WIDTH = 3047;
      const HEIGHT = 1258;
      const PIXELS_PER_INCH = 2117 / (54 * 12);
      const CENTER_X = 1523;
      const CENTER_Y = 616;
      const ROBOT_CENTER_X = 118 / 2;
      const ROBOT_CENTER_Y = 131 - 54;
      
      if (!this._field) return;
      
      var ctx = this._canvas.getContext("2d");
      ctx.clearRect(0, 0, WIDTH, HEIGHT);
      ctx.drawImage(this._field, 0, 0);
      ctx.save();
      
      var x = this._valTable.get("x") || 0;
      var y = this._valTable.get("y") || 0;
      var direction = this._valTable.get("direction") || 0;
      
      ctx.translate(x * PIXELS_PER_INCH + CENTER_X, -y * PIXELS_PER_INCH + CENTER_Y);
      ctx.save()
      ctx.rotate(direction * Math.PI / 180);
      ctx.drawImage(this._robot, -ROBOT_CENTER_X, -ROBOT_CENTER_Y);
      ctx.restore();
      ctx.fillStyle = "yellow";
      ctx.fillRect(-10, -10, 20, 20);
      
      ctx.restore();
      
      setTimeout(this.renderLoop.bind(this), 50);
    }
    
    destroy() {
      this._destroyed = true;
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
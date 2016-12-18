//polyfill by Erik Möller
//http://my.opera.com/emoller/blog/2011/12/20/requestanimationframe-for-smart-er-animating//http://www.paulirish.com/2011/requestanimationframe-for-smart-animating/
(function () {
    var lastTime = 0;
    var vendors = ['webkit', 'moz'];
    for (var x = 0; x < vendors.length && !window.requestAnimationFrame; ++x) {
        window.requestAnimationFrame = window[vendors[x] + 'RequestAnimationFrame'];
        window.cancelAnimationFrame = window[vendors[x] + 'CancelAnimationFrame'] || window[vendors[x] + 'CancelRequestAnimationFrame'];
    }
    if (!window.requestAnimationFrame)
        window.requestAnimationFrame = function (callback, element) {
            var currTime = new Date().getTime();
            var timeToCall = Math.max(0, 16 - (currTime - lastTime));
            var id = window.setTimeout(function () { callback(currTime + timeToCall); }, timeToCall);
            lastTime = currTime + timeToCall;
            return id;
        };
    if (!window.cancelAnimationFrame) window.cancelAnimationFrame = function (id) { clearTimeout(id); };
}());

/** * Klasa obs³uguj¹ca ³adowanie obrazków. */
var kamyk = {};
kamyk.ImageLoader = Class.$extend({
    /**"Konstruktor"*/
    __init__: function (whoWaitsForMyJob) {
        this.numLoaded = 0;
        this.waiting = whoWaitsForMyJob;
        this.URLs = [];
        this.images = [];
        this.imageReceivers = [];
    },
    addURL: function (imageURL, imageReceiver) {
        this.imageReceivers.push(imageReceiver);
        return this.URLs.push(imageURL) - 1;
    },
    startLoading: function () {
        this.numLoaded = 0;
        var that = this;
        var loadedFuncWrapper = function () {
            that.__imageLoaded();
        };
        for (var i = 0; i < this.URLs.length; i++) {
            this.images[i] = new Image();
            this.images[i].onload = loadedFuncWrapper;
            this.images[i].src = this.URLs[i];
        }
    },
    getImage: function (ind) {
        return this.images[ind];
    },
    __imageLoaded: function () {
        this.numLoaded++;
        var isReady = (this.numLoaded === this.URLs.length);
        if (isReady === true) { this.__finishedLoading(); }
    },
    /**     * Wywo³ywane po za³adowaniu wszystkich obrazków.     */
    __finishedLoading: function () {
        for (var i = 0; i < this.URLs.length; i++) {
            if (this.imageReceivers[i] !== undefined) {
                this.imageReceivers[i].setImage(this.getImage(i));
            }
        }
        this.waiting.onLoaderReady(this);
    }
});

kamyk.AbstractException = "Brak implementacji metody w podklasie!";

kamyk.Game = Class.$extend({
    __init__: function (canvasId, width, height) {
        this.kanwa = document.getElementById(canvasId);
        this.context = this.kanwa.getContext("2d");
        this.kanwa.getAttribute("tabIndex", "0");
        this.kanwa.focus();
        this.imageLoader = new kamyk.ImageLoader(this);
        this.width = width;
        this.height = height;
        this.time = 0;
    },
    initResources: function () {
        throw kamyk.AbstractException;
    },
    update: function () {
        throw kamyk.AbstractException;
    },
    render: function () {
        throw kamyk.AbstractException;
    },
    draw: function () {
        requestAnimationFrame(gra.draw);
        var now = new Date().getTime(),
            dt = now - (this.time || now);
        this.time = now;
        gra.update(dt);
        gra.render();
    },
    onLoaderReady: function (imageLoader) {
        this.draw();
    },
    start: function () {
        this.initResources();
    }
});

kamyk.Sprite = Class.$extend({
    __init__: function (x, y) {
        this.x = x;
        this.y = y;
        this.sizeX = 1;
        this.sizeY = 1;
        this.midXOffset = 1;
        this.midYOffset = 1;
        this.speed = 0.1;
        this.isMoving = false;
        this.image = undefined;
    },
    setImage: function(img) {
        this.image = img;
        this.resize(this.image.width, this.image.height);
    },
    resize: function (newWidth, newHeight) {
        this.sizeX = newWidth;
        this.sizeY = newHeight;
        this.midXOffset = Math.floor(this.sizeX / 2);
        this.midYOffset = Math.floor(this.sizeY / 2);
        this.__middleCoordsInit();
    },
    __middleCoordsInit: function () {
        this.midX = this.x + this.midXOffset;
        this.midY = this.y + this.midYOffset;
    },
    /**Rysowanie*/    
    render: function(ctx){
        if(this.image!== undefined) {   
            ctx.drawImage(this.image, Math.floor(this.x), Math.floor(this.y),this.sizeX,this.sizeY);
        }
    },
    update: function (dt) {
        if (this.isMoving) {
            var distance = dt * this.speed;

            if (this.x < this.xTarget) {
                this.x += distance;
                if (this.x > this.xTarget)
                    this.x = this.xTarget;
            }
            else if (this.x > this.xTarget) {
                this.x -= distance;
                if (this.x < this.xTarget)
                    this.x = this.xTarget;
            }

            if (this.y < this.yTarget) {
                this.y += distance;
                if (this.y > this.yTarget)
                    this.y = this.yTarget;
            }
            else if (this.y > this.yTarget) {
                this.y -= distance;
                if (this.y < this.yTarget)
                    this.y = this.yTarget;
            }

            if (this.x == this.xTarget && this.y == this.yTarget)
                this.isMoving = false;
        }
    },
    moveTo: function (xTarg, yTarg) {
        this.xTarget = xTarg,
        this.yTarget = yTarg;
        this.isMoving = true;
    },
    stopMoving: function() {
        this.isMoving = false;
    }
});

kamyk.AnimatedSprite= kamyk.Sprite.$extend({
    __init__:function(x, y, numCol, numRow){
        this.$super(x, y);
        this.cols= numCol;
        this.rows= numRow;
        this.__frameChangeDt = 100;
        this.__curFrameTime = 0;
        this.__curFrameInd = 0;      
        this.frames = [0];
        this.loopAnim = true;
        this.stopAnim = false;

        var frameNumbers = [];
        for (var i = 0; i < (numRow * numCol) ; i++) {
            frameNumbers.push(i);
        }
        this.setAnimationFramesSeq(frameNumbers);
    },
    setImage:function(img){
        this.image= img;
        this.sizeX= Math.floor(this.image.width/this.cols);
        this.sizeY= Math.floor(this.image.height/this.rows);
        this.midXOffset=this.sizeX/2;
        this.midYOffset=this.sizeY/2;
        this.__middleCoordsInit();
    }, 
    setAnimationFramesSeq:function(frameNumbers){
        this.frames = frameNumbers;
        this.__curFrameTime = 0;
        this.__curFrameInd = 0;
        this.curFrame=this.frames[this.__curFrameInd];
    },
    render:function(ctx){
        if(this.image!== undefined){
            var xFrame = (this.curFrame%this.cols)*this.sizeX;
            var yFrame = Math.floor(this.curFrame/this.cols)*this.sizeY;            
            ctx.drawImage(this.image, xFrame, yFrame,this.sizeX,this.sizeY,Math.floor(this.x), Math.floor(this.y),this.sizeX,this.sizeY);
        }
    },
    update: function (dt) {
        this.$super(dt);
        this.__curFrameTime += dt;
        if (this.__curFrameTime > this.__frameChangeDt) {
            this.__curFrameTime = 0;
            if (this.__curFrameInd < this.frames.length - 1) {
                this.__curFrameInd++;
            } else if (this.loopAnim) {
                this.__curFrameInd = 0;
            }
            this.curFrame = this.frames[this.__curFrameInd];
        }
    }
});

kamyk.Player = kamyk.Sprite.$extend({
    __init__: function (x, y, baseHP) {
        this.$super(x, y);
        this.HP = baseHP;
    },
    hurt: function (power) {
        if (this.HP > 0)
            this.HP -= power;
    }
});

kamyk.Monster = kamyk.AnimatedSprite.$extend({
    __init__: function (x, y, image) {
        this.$super(x, y, 9, 4);
        this.attackInterval = 500;
        this.attackPower = 1;
        this.timeAfterLastAttack = 0;
        this.speed = 0.3;
        this.attackTarget = undefined;
        this.setImage(image);
    },
    attack: function (target) {
        this.attackTarget = target;
    },
    stopAttacking: function () {
        this.attackTarget = undefined;
    },
    update: function (dt) {
        this.$super(dt * this.speed);
        if (this.attackTarget != undefined) {
            this.timeAfterLastAttack += dt;
            if (this.timeAfterLastAttack > this.attackInterval) {
                this.timeAfterLastAttack = 0;
                this.attackTarget.hurt(this.attackPower);
            }
        }
    }
});

kamyk.Fireball = kamyk.AnimatedSprite.$extend({
    __init__: function(x, y, image, angle) {
        this.$super(x, y, 3, 4);
        this.angle = angle;
        this.speed = 1;
        this.setImage(image);
    },
    update: function (dt) {
        this.$super(dt);
        var vx = dt * this.speed * Math.cos(this.angle); //Math.cos(this.angle - (Math.PI / 2));
        var vy = dt * this.speed * Math.sin(this.angle); //Math.sin(this.angle - (Math.PI / 2));
        this.x += vx;
        this.y += vy;
    }
});

kamyk.CollisionDetector=Class.$extend({
    collide:function(spriteA, spriteB) {
        var result =this._intersectsRectRect(spriteA, spriteB);
        return result;
    },
    _intersectsRectRect:function(a, b){
        return (a.x <= b.x + b.sizeX && b.x <= a.x + a.sizeX && a.y <= b.y + b.sizeY && b.y <= a.y + a.sizeY);
    }
});

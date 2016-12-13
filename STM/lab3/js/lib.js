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
    if (!window.cancelAnimationFrame)
        window.cancelAnimationFrame = function (id) {
            clearTimeout(id);
        };
}());

/** * Klasa obs³uguj¹ca ³adowanie obrazków. */
var kamyk = {};
kamyk.ImageLoader = Class.$extend({
    /**"Konstruktor"*/
    __init__: function (whoWaitsForMyJob) {
        //Liczba obrazków, które za³adowano
        this.numLoaded = 0;
        /*         
        * Obiekt, który oczekuje na za³adowanie wszystkich obrazków         
        * Musi implementowaæ metodê onLoaderReady(imageLoader)         
        */
        this.waiting = whoWaitsForMyJob;
        //Adresy obrazków
        this.URLs = [];
        //Same obrazki
        this.images = [];
        //Obiekty oczekuj¹ce za³adowania poszczególnych obrazków.
        this.imageReceivers = [];
    },

    /**Dodaje url obrazka do wczytania i zwraca jego identyfikator.     
    * Opcjonalnie mo¿na zdefiniowaæ obiekt oczekuj¹cy za³adowania tego konkretnego obrazka -      
    * powinien on zawieraæ metodê "setImage(img)".*/
    addURL: function (imageURL, imageReceiver) {
        this.imageReceivers.push(imageReceiver);
        return this.URLs.push(imageURL) - 1;
    },

    /**      * Rozpoczyna ³adowanie obrazków.     */
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

    /**     * Zwraca i-ty obrazek.     */
    getImage: function (ind) {
        return this.images[ind];
    },
    /**     * Wywo³ywane po za³adowaniu ka¿dego z obrazków.     */
    __imageLoaded: function () {
        this.numLoaded++;
        var isReady = (this.numLoaded === this.URLs.length);
        if (isReady === true) {
            this.__finishedLoading();
        }
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

kamyk.AbstractException = "Zapomniales zaimplementowac te metode w podklasie!";
kamyk.Game = Class.$extend({
    /**"Konstruktor"*/
    __init__: function (canvasID, width, height) {
        this.gameLoaded = false;
        this.kanwa = document.getElementById(canvasID);
        this.context = this.kanwa.getContext("2d");
        this.kanwa.setAttribute('tabindex', '0');
        this.kanwa.focus();
        this.imageLoader = new kamyk.ImageLoader(this);
        // Stworzenie i inicjalizacja innych ciekawych pol
        this.height = height;
        this.width = width;
        this.time = undefined;
    },
    
    getContext: function() {
		return this.context;
	},
    
    initResources: function () {
        throw kamyk.AbstractException;
    },

    update: function (dt) {
        throw kamyk.AbstractException;
    },

    render: function () {
        throw kamyk.AbstractException;
    },

    onLoaderReady: function () {
        this.gameLoaded = true;
        this.render();
    },

    start: function () {
        //while (!this.gameLoaded);
        this.initResources();
        requestAnimationFrame(this.draw);
        // start main loop
    },

    stop: function () {
    },

    draw: function () {
        requestAnimationFrame(gra.draw);
        if (gra.gameLoaded) {
            var now = new Date().getTime(),
            dt = now - (gra.time || now);

            gra.time = now;
            gra.update(dt);
            gra.render();
        }
    }
})

kamyk.Sprite = Class.$extend({
    __init__: function (x, y) {
        this.x = x;
        this.y = y;
        this.sizeX = 1;
        this.sizeY = 1;
        this.midXOffset = 1;
        this.midYOffset = 1;
        this.speed = 1;
        this.isMoving = false;
        this.image = undefined;
        this.destinationList = [];
        this.destination = undefined;
    },
    setImage: function (img) {
        this.image = img;
        this.resize(this.image.width, this.image.height);        
    },
    resize: function (newWidth, newHeight) {
        this.sizeX = Math.min(newWidth, 400);
        this.sizeY = Math.min(newHeight, 400);
        this.midXOffset = Math.floor(this.sizeX / 2);
        this.midYOffset = Math.floor(this.sizeY / 2);
        this.__middleCoordsInit();
    },
    __middleCoordsInit: function () {
        this.midX = this.x + this.midXOffset;
        this.midY = this.y + this.midYOffset;
    },
    render: function (ctx) {
        if (this.image !== undefined) {
            ctx.drawImage(
                this.image,
                Math.floor(this.x),
                Math.floor(this.y),
                this.sizeX,
                this.sizeY);
        }
    },
    update: function (dt) {
        if (this.isMoving) {
            this.__middleCoordsInit();
            if (this.destination == undefined && this.destinationList.length > 0) {
                this.destination = this.destinationList.shift();
                this.destinationList.push(this.destination);
            }

            this.moveTo(this.destination, dt);
        }
    },
    moveTo: function (coords, dt) {
        x = Math.floor(this.midX - coords.x);
        y = Math.floor(this.midY - coords.y);
        if (x !== 0.0 || y !== 0.0) {
            if (Math.abs(x) > Math.abs(y)) {
                this.x += x < 0 ? this.speed * dt : -this.speed * dt;
            } else {
                this.y += y < 0 ? this.speed * dt : -this.speed * dt;
            }
        } else if (this.destinationList == 0) {
            this.isMoving = false;
            this.destination = undefined;
        } else {
            this.destination = undefined;
        }
    }
});

kamyk.AnimatedSprite = kamyk.Sprite.$extend({
    __init__: function (x, y, numCol, numRow) {
        this.$super(x, y);
        this.cols = numCol;
        this.rows = numRow;
        this.__frameChangeDt = 50;
        this.__curFrameTime = 0;
        this.__curFrameInd = 0;
        this.frames = [0];
        this.loopAnim = true;
        this.stopAnim = false;
        this.curFrame = 10;
        this.curFrameX = 0;
        this.curFrameY = 0;
    },
    setImage: function (img) {
        this.image = img;
        this.sizeX = Math.floor(this.image.width / this.cols);
        this.sizeY = Math.floor(this.image.height / this.rows);
        this.midXOffset = this.sizeX / 2;
        this.midYOffset = this.sizeY / 2;
        this.__middleCoordsInit();
    },
    setAnimationFramesSeq: function (frameNumbers) {
        this.frames = frameNumbers;
        this.__curFrameTime = 0;
        this.__curFrameInd = 0;
        this.curFrame = this.frames[this.__curFrameInd];
    },
    render: function (ctx) {
        if (this.image !== undefined) {
            var xFrame = (this.curFrame % this.cols) * this.sizeX;
            var yFrame = Math.floor(this.curFrame / this.cols) * this.sizeY;
            ctx.drawImage(
                this.image,
                xFrame,
                yFrame,
                this.sizeX,
                this.sizeY,
                Math.floor(this.x),
                Math.floor(this.y),
                this.sizeX,
                this.sizeY
            );
        }
    },

    update: function (dt) {
        if (this.__curFrameTime < this.__frameChangeDt)
        {
            this.__curFrameTime += dt;
            return;
        }
        else if (this.__curFrameTime > this.__frameChangeDt) {
            this.curFrameX = (this.curFrameX +1 ) % this.cols;
            this.curFrame = this.curFrameY * this.cols + this.curFrameX;
            if (this.isMoving) {
                this.__middleCoordsInit();
                if (this.destination == undefined && this.destinationList.length > 0) {
                    this.destination = this.destinationList.shift();
                    this.destinationList.push(this.destination);
                }

                this.moveTo(this.destination, dt);
            }
        }
    },
    moveTo: function (coords, dt) {
        x = Math.floor(this.midX - coords.x);
        y = Math.floor(this.midY - coords.y);
        dir = 0;
        if (x !== 0.0 || y !== 0.0) {
            if (Math.abs(x) > Math.abs(y)) {
                if (x < 0) {
                    this.x += this.speed * dt;
                    this.curFrameY = 3;
                }
                else if (x > 0) {
                    this.x -= this.speed * dt;
                    this.curFrameY = 1;
                }
            } else {
                if (y < 0) {
                    this.y += this.speed * dt;
                    this.curFrameY = 2;
                }
                else if (y > 0) {
                    this.y -= this.speed * dt;
                    this.curFrameY = 0;
                }
            }


        } else if (this.destinationList == 0) {
            this.isMoving = false;
            this.destination = undefined;
        } else {
            this.destination = undefined;
        }
    }
});

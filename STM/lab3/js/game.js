kamyk.monsterShooter={};
kamyk.monsterShooter.MonsterShooter= kamyk.Game.$extend({
    __init__:function(canvasID, width, height){
        this.$super(canvasID, width, height);
        this.sprites = [];
        this.x = 150;
        this.y = 150;
        this.dir = 1;
        this.counter = 0;
        this.background = new kamyk.Sprite(0, 0);
        this.hp = 100;
        this.player = new kamyk.Sprite(150, 150);
        var playerPos = { x: 200, y: 200 };
        this.rkt = new kamyk.Sprite(20, 20);
        this.shoot = false;
        
        this.kanwa.addEventListener("click", this.kboom, false);
        
        this.monster0 = new kamyk.AnimatedSprite(0, 200, 9, 4);
        this.monster1 = new kamyk.AnimatedSprite(200, 0, 9, 4);
        this.monster2 = new kamyk.AnimatedSprite(400, 200, 9, 4);
        this.monster3 = new kamyk.AnimatedSprite(200, 400, 9, 4);
        
        this.monster0.speed = 0.03;
        this.monster1.speed = 0.03;
        this.monster2.speed = 0.03;
        this.monster3.speed = 0.03;
        this.monster0.destinationList.unshift(playerPos);
        this.monster1.destinationList.unshift(playerPos);
        this.monster2.destinationList.unshift(playerPos);
        this.monster3.destinationList.unshift(playerPos);
        this.monster0.isMoving = true;
        this.monster1.isMoving = true;
        this.monster2.isMoving = true;
        this.monster3.isMoving = true;

        this.sprites.push(this.background);
        this.sprites.push(this.monster0);
        this.sprites.push(this.monster1);
        this.sprites.push(this.monster2);
        this.sprites.push(this.monster3);
        this.sprites.push(this.player);
    },
    
    initResources:function(){
        this.backgroundImgIndex = this.imageLoader.addURL("img/background.jpg", this.background);
        this.monsterImgIndex = this.imageLoader.addURL("img/hero.png", this.monster0);
        this.monsterImgIndex = this.imageLoader.addURL("img/hero.png", this.monster1);
        this.monsterImgIndex = this.imageLoader.addURL("img/hero.png", this.monster2);
        this.monsterImgIndex = this.imageLoader.addURL("img/hero.png", this.monster3);
        this.playerImgIndex = this.imageLoader.addURL("img/player.png", this.player);
        this.rktImgIndex = this.imageLoader.addURL("img/boom.png", this.rkt);
        this.imageLoader.startLoading();
    },
    
    render: function () {
        for (var i = 0; i < this.sprites.length; ++i) {
            this.sprites[i].render(this.context);
        }
        this.context.font="14px Georgia";
		this.context.fillText("HP: "+ this.hp, 5, 15);
    },

    update: function (dt, new_rkt) {
		this.monster0.update(dt);
		this.monster1.update(dt);
		this.monster2.update(dt);
		this.monster3.update(dt);
		this.rkt.update(dt);
	},
});

var gra = undefined;
kamyk.monsterShooter.letsGo = function () {
    gra = new kamyk.monsterShooter.MonsterShooter("kanwa", 400, 400);
    gra.start();
}

function handlerFunction(event) {
}

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
        this.monster = new kamyk.AnimatedSprite(40, 40, 9, 4);
        
        var leftBottom = { x: 25, y: 375 };
        var rightBottom = { x: 375, y: 375 };
        var rightTop = { x: 375, y: 25 };
        var leftTop = { x: 25, y: 25 };
        
        this.monster.speed = 0.05;
        this.monster.destinationList.unshift(rightTop);
        this.monster.destinationList.unshift(rightBottom);
        this.monster.destinationList.unshift(leftBottom);
        this.monster.destinationList.unshift(leftTop);
        
        this.monster.isMoving = true;

        this.sprites.push(this.background);
        this.sprites.push(this.monster);
        this.sprites.push(this.player);
    },
    
    initResources:function(){
        this.backgroundImgIndex = this.imageLoader.addURL("img/background.jpg", this.background);
        this.monsterImgIndex = this.imageLoader.addURL("img/hero.png", this.monster);
        this.playerImgIndex = this.imageLoader.addURL("img/player.png", this.player);
        this.imageLoader.startLoading();
    },
    
    render: function () {
        for (var i = 0; i < this.sprites.length; ++i) {
            this.sprites[i].render(this.context);
        }
        this.context.font="14px Georgia";
		this.context.fillText("HP: "+ this.hp ,5,15);
    },    

    update: function (dt) {
        this.monster.update(dt);
    }
});


var gra = undefined;
kamyk.monsterShooter.letsGo = function () {
    gra = new kamyk.monsterShooter.MonsterShooter("kanwa", 400, 400);
    gra.start();
}

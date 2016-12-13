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
        this.hero = new kamyk.AnimatedSprite(40, 40, 9, 4);

        this.randomSprite = new kamyk.AnimatedSprite(100, 100, 9, 4);

        var leftBottom = { x: 25, y: 375 };
        var rightBottom = { x: 375, y: 375 };
        var rightTop = { x: 375, y: 25 };
        var leftTop = { x: 25, y: 25 };
        
        this.hero.speed = 0.1;
        this.hero.destinationList.unshift(rightTop);
        this.hero.destinationList.unshift(rightBottom);
        this.hero.destinationList.unshift(leftBottom);
        this.hero.destinationList.unshift(leftTop);
        
        this.hero.isMoving = true;

        this.sprites.push(this.background);
        this.sprites.push(this.hero);
        this.sprites.push(this.randomSprite);
    },
    
    initResources:function(){
        this.backgroundImgIndex = this.imageLoader.addURL("img/background.jpg", this.background);
        this.characterImgIndex = this.imageLoader.addURL("img/hero.png", this.hero);
        this.randomIndex = this.imageLoader.addURL("img/hero.png", this.randomSprite);
        this.imageLoader.startLoading();

    },
    
    render: function () {
        for (var i = 0; i < this.sprites.length; ++i) {
            this.sprites[i].render(this.context);
        }
    },    

    update: function (dt) {
        this.hero.update(dt);
        this.randomSprite.update(dt);
    }
});


var gra = undefined;
kamyk.monsterShooter.letsGo = function () {
    gra = new kamyk.monsterShooter.MonsterShooter("kanwa", 400, 400);
    gra.start();
}
export class Cell{
    constructor(r, c){//存储蛇
        this.r = r;
        this.c = c;
        this.x = c + 0.5;// 转换为 canvas 的坐标
        this.y = r + 0.5;
    }
}
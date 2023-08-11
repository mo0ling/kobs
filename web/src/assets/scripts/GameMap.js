import { AcGameObject } from "./AcGameObject";
import { Wall } from "./Wall";
import { Snake } from './Snake';

export class GameMap extends AcGameObject {
    constructor(ctx, parent, store) {
        super();

        this.ctx = ctx;
        this.parent = parent;
        this.store = store;
        this.L = 0;

        this.rows = 13;
        this.cols = 14;//如果两条蛇，同时走到相同的格子，会造成平局，这种情况会对优势者不利！  需要把地图变为 偶数 乘 奇数
        
        this.inner_walls_count = 20;
        this.walls = [];//图

        this.snakes = [
            new Snake({id: 0, color: "#4876EC", r: this.rows - 2, c: 1}, this),
            new Snake({id: 1, color: "#F94848", r: 1, c: this.cols - 2}, this),
        ];
    }

    create_walls() {
        const g = this.store.state.pk.gamemap;

        for (let r = 0; r < this.rows; r ++ ) {
            for (let c = 0; c < this.cols; c ++ ) {
                if (g[r][c]) {//墙
                    this.walls.push(new Wall(r, c, this));
                }
            }
        }
    }

    add_listening_events() {
        if(this.store.state.record.is_record){
            let k = 0;//记录当前步数
            const a_steps = this.store.state.record.a_steps;
            const b_steps = this.store.state.record.b_steps;
            const [snake0, snake1] = this.snakes;
            const loser = this.store.state.record.record_loser; 
            const interval_id = setInterval(() => {// 将除死亡的操作每300ms泫渲染出来 setInterval每隔这个延迟时间 就去调用这个回调函数 会调用很多次
                if(k >= a_steps.length - 1){
                    if (loser === "all" || loser === "A") {
                        snake0.status = "die";
                      }
                      if (loser === "all" || loser === "B") {
                        snake1.status = "die";
                      }
                      clearInterval(interval_id);//可取消由 setInterval() 设置的 timeout。定时器关闭
                }else{
                    snake0.set_direction(parseInt(a_steps[k]));
                    snake1.set_direction(parseInt(b_steps[k]));
                }
                k++;
            }, 300)
        }else{
            this.ctx.canvas.focus();//聚焦
            this.ctx.canvas.addEventListener("keydown", e => {//键盘监听
                let d = -1;
                if (e.key === 'w') d = 0;
                else if (e.key === 'd') d = 1;
                else if (e.key === 's') d = 2;
                else if (e.key === 'a') d = 3;
                else if (e.key === 'ArrowUp') d = 0;
                else if (e.key === 'ArrowRight') d = 1;
                else if (e.key === 'ArrowDown') d = 2;
                else if (e.key === 'ArrowLeft') d = 3;

                if (d >= 0) {
                    this.store.state.pk.socket.send(JSON.stringify({
                        event: "move",
                        direction: d,
                    }));
                }
            });
        }

        
    }

    start() {
        this.create_walls();//创建连通的地图就退出
        
        this.add_listening_events();//创建监听事件
    }

    update_size() {// 计算小正方形的边长
        this.L = parseInt(Math.min(this.parent.clientWidth / this.cols, this.parent.clientHeight / this.rows));//根据父组件得边
        this.ctx.canvas.width = this.L * this.cols;//设图宽高
        this.ctx.canvas.height = this.L * this.rows;
    }

    check_ready() {  // 判断两条蛇是否都准备好下一回合了
        for (const snake of this.snakes) {
            if (snake.status !== "idle") return false;//非静止
            if (snake.direction === -1) return false;//有指令
        }
        return true;
    }

    next_step() {  // 让两条蛇进入下一回合
        for (const snake of this.snakes) {
            snake.next_step();
        }
    }

    check_valid(cell) {  // 检测目标位置是否合法：没有撞到两条蛇的身体和障碍物
        for (const wall of this.walls) {
            if (wall.r === cell.r && wall.c === cell.c)
                return false;
        }

        for (const snake of this.snakes) { // 判断是否会碰到自己
            let k = snake.cells.length;
            if (!snake.check_tail_increasing()) {  // 当蛇尾会前进的时候，蛇尾不要判断
                k -- ;
            }
            for (let i = 0; i < k; i ++ ) {
                if (snake.cells[i].r === cell.r && snake.cells[i].c === cell.c)
                    return false;
            }
        }

        return true;
    }

    update() {
        this.update_size();
        if (this.check_ready()) {
            this.next_step();//检查是否准备好，可以进入下一回合
        }
        this.render();
    }

    render() {// 渲染函数// 每一帧需要清空地图
        const color_even = "#AAD751", color_odd = "#A2D149";
        for (let r = 0; r < this.rows; r ++ ) {//染色
            for (let c = 0; c < this.cols; c ++ ) {
                if ((r + c) % 2 == 0) {
                    this.ctx.fillStyle = color_even;
                } else {
                    this.ctx.fillStyle = color_odd;
                }
                this.ctx.fillRect(c * this.L, r * this.L, this.L, this.L);//左上角左边，明确canvas坐标系
            }
        }
    }
}

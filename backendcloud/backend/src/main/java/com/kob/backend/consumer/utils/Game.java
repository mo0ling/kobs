package com.kob.backend.consumer.utils;

import com.alibaba.fastjson.JSONObject;
import com.kob.backend.consumer.WebSocketServer;
import com.kob.backend.pojo.Bot;
import com.kob.backend.pojo.Record;
import com.kob.backend.pojo.User;
import org.springframework.security.core.parameters.P;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantLock;

public class Game extends Thread{
    private final Integer rows;
    private final Integer cols;
    private final Integer inner_walls_count;

    private final int[][] g;
    private final static int[] dx = {-1, 0, 1, 0};
    private final static int[] dy = {0, 1, 0, -1};

    private final Player playerA, playerB;

    private Integer nextStepA = null;// A的操作
    private Integer nextStepB = null;// B的操作
    private ReentrantLock reentrantLock = new ReentrantLock();

    private String status = "playing"; //进行下一步操作// playing -> finished

    private String loser = "";//all：平局 ,A：A输 ,B：B输
    private final static String addBotUrl = "http://127.0.0.1:3002/bot/add/";
    public Game(Integer rows,
                Integer cols,
                Integer inner_walls_count,
                Integer idA,
                Bot botA,
                Integer idB,
                Bot botB){
        this.rows = rows;
        this.cols = cols;
        this.inner_walls_count = inner_walls_count;
        this.g = new int[rows][cols];

        Integer botIdA = -1, botIdB = -1;
        String botCodeA = "", botCodeB = "";
        if (botA != null) {
            botIdA = botA.getId();
            botCodeA = botA.getContent();
        }
        if (botB != null) {
            botIdB = botB.getId();
            botCodeB = botB.getContent();
        }
        playerA = new Player(idA,botIdA, botCodeA, rows - 2, 1, new ArrayList<>());//左下
        playerB = new Player(idB,botIdB, botCodeB, 1, cols - 2, new ArrayList<>());//右上
    }

    public Player getPlayerA(){
        return playerA;
    }
    public Player getPlayerB(){
        return playerB;
    }
    // 在主线程会读两个玩家的操作，并且玩家随时可能输入操作，存在读写冲突
    public void setNextStepA(Integer nextStepA){
        reentrantLock.lock();
        try{
            this.nextStepA = nextStepA;
        }finally {
            reentrantLock.unlock();
        }
    }
    public void setNextStepB(Integer nextStepB){
        reentrantLock.lock();
        try{
            this.nextStepB = nextStepB;
        }finally {
            reentrantLock.unlock();
        }
    }
    public  int[][] getG(){
        return g;
    }

    private boolean draw(){
        //画地图
        for(int i = 0; i < this.rows; i++){//初始化
            for(int j = 0; j < this.cols; j++){
                g[i][j] = 0;
            }
        }
        for(int r = 0; r < this.rows; r++){// 给四周加上墙
            g[r][0] = g[r][this.cols - 1] = 1;//外围
        }
        for(int c = 0; c < this.cols; c++){
            g[0][c] = g[this.rows - 1][c] = 1;//外围
        }
        Random random = new Random();
        //随机生成障碍
        for(int i = 0; i < this.inner_walls_count / 2; i++){
            for(int j = 0; j < 1000; j++){
                int r = random.nextInt(this.rows);
                int c = random.nextInt(this.cols);
                //存在继续创建
                if(g[r][c] == 1 || g[this.rows - 1 - r][this.cols - 1 - c] == 1){
                    continue;
                }
                //左下和右上不能创建
                if(r == this.rows - 2 && c == 1 || r == 1 && c == this.cols - 2){
                    continue;
                }
                g[r][c] = g[this.rows - 1 - r][this.cols - 1 - c] = 1;//对称创建
                break;
            }
        }

        return check_connectivity(this.rows - 2, 1, 1, this.cols - 2);
    }
    private boolean check_connectivity(int sx, int sy, int tx, int ty){
        if(sx == tx && sy == ty){
            return true;
        }
        g[sx][sy] = 1;//左下角
        for(int i = 0; i < 4; i++){//行从下往上，到1行向右 上右下左
            int x = sx + dx[i];
            int y = sy + dy[i];
            if(x >= 0 && x < this.rows && y >= 0 && y < this.cols && g[x][y] == 0){
                if(check_connectivity(x, y, tx, ty)) {//检查是否能连两个出发点
                    g[sx][sy] = 0;
                    return true;
                }
            }
        }
        g[sx][sy] = 0;
        return false;
    }
    public void createMap(){
        for(int i = 0; i < 1000; i++){
            if(draw()){//判断是否连通
                break;
            }
        }
    }

    private String getInput(Player player){//将当前局面编码成字符串
        //将当前的局面信息编码成字符串// 地图#my.sx#my.sy#my操作#you.sx#you.sy#you操作
        Player me, you;
        if(playerA.getId().equals(player.getId())){
            me = playerA;
            you = playerB;
        }else{
            me = playerB;
            you = playerA;
        }
        return getMapString() + "#"
                + me.getSx() + "#" +
                me.getSy() + "#(" +
                me.getStepString() + ")#"// 加()是为了预防操作序列为空
                + you.getSx() + "#" +
                you.getSy() + "#(" +
                you.getStepString() + ")";
    }
    private void sendBotCode(Player player){
        if(player.getBotId().equals(-1)){
            return;
        }
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user_id", player.getId().toString());
        data.add("bot_code", player.getBotCode());
        data.add("input", getInput(player));
        WebSocketServer.restTemplate.postForObject(addBotUrl, data, String.class);//向BotRunningSystem端发送请求
    }
    /*
     * 等待玩家下一步操作
     * */
    private boolean nextStep(){
        //等到两名玩家的下一步操作,每秒五步操作，2000ms后判断是否接收到输入。并给地图初始化时间
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        sendBotCode(playerA);
        sendBotCode(playerB);
        for(int i = 0; i < 50; i++){
                try {
                    Thread.sleep(100);
                    reentrantLock.lock();
                    try{
                        if(nextStepA != null && nextStepB != null){
                            playerA.getSteps().add(nextStepA);
                            playerB.getSteps().add(nextStepB);
                            return  true;
                        }
                    }finally {
                        reentrantLock.unlock();
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        return false;
    }
    private void sendAllMessage(String message){
        if(WebSocketServer.users.get(playerA.getId()) != null)
            WebSocketServer.users.get(playerA.getId()).sendMessage(message);
        if(WebSocketServer.users.get(playerB.getId()) != null)
            WebSocketServer.users.get(playerB.getId()).sendMessage(message);
    }
    private boolean check_valid(List<Cell> cellsA, List<Cell> cellsB){
        int n = cellsA.size();
        Cell cell = cellsA.get(n - 1);//取新增的方块
        if(g[cell.x][cell.y] == 1) return false;// 如果是墙，则非法
        for(int i = 0; i < n - 1; i++){// 遍历A除最后一个Cell
            if(cellsA.get(i).x == cell.x && cellsA.get(i).y == cell.y){// 和蛇身是否重合
                return false;
            }
        }
        for(int i = 0; i < n - 1; i ++){// 遍历B除最后一个Cell
            if(cellsB.get(i).x == cell.x && cellsB.get(i).y == cell.y){// 和B蛇身是否重合
                return false;
            }
        }
        return true;
    }
    private void judge(){//判断两名玩家的下一步操作是否合法
        List<Cell> cellsA = playerA.getCells();
        List<Cell> cellsB = playerB.getCells();

        boolean validA = check_valid(cellsA, cellsB);
        boolean validB = check_valid(cellsB, cellsA);
        if(!validA || !validB){
            status = "finished";
            if(!validA && !validB){
                loser = "all";
            }else if(!validA){
                loser = "A";
            }else{
                loser = "B";
            }
        }

    }

    private String getMapString(){
        StringBuilder res = new StringBuilder();
        for(int i = 0; i < rows; i++){
            for(int j = 0; j < cols; j++){
                res.append(g[i][j]);
            }
        }
        return res.toString();
    }

    private void updateUserRating(Player player, Integer rating){
        User user = WebSocketServer.userMapper.selectById(player.getId());//取出玩家
        user.setRating(rating);
        WebSocketServer.userMapper.updateById(user);
    }
    private void saveToDatabase(){//保存到数据库中
        Integer ratingA = WebSocketServer.userMapper.selectById(playerA.getId()).getRating();

        Integer ratingB = WebSocketServer.userMapper.selectById(playerB.getId()).getRating();

        if("A".equals(loser)){
            ratingA -= 2;
            ratingB += 5;
        }else if("B".equals(loser)){
            ratingA += 5;
            ratingB -= 2;
        }
        updateUserRating(playerA, ratingA);
        updateUserRating(playerB, ratingB);
        Record record = new Record(
                null,
                playerA.getId(),
                playerA.getSx(),
                playerA.getSy(),
                playerB.getId(),
                playerB.getSx(),
                playerB.getSy(),
                playerA.getStepString(),
                playerB.getStepString(),
                getMapString(),
                loser,
                new Date()
        );
        WebSocketServer.recordMapper.insert(record);
    }
    private void sendResult(){
        //向两个client返回结果
        JSONObject resp = new JSONObject();
        resp.put("event","result");
        resp.put("loser",loser);
        saveToDatabase();
        sendAllMessage(resp.toJSONString());

    }
    private void sendMove(){// 向两名玩家传递移动信息
        //因为需要读玩家的下一步操作，所以需要加锁
        reentrantLock.lock();
        try {
            JSONObject resp = new JSONObject();
            resp.put("event","move");
            resp.put("a_direction", nextStepA);
            resp.put("b_direction", nextStepB);
            nextStepA = nextStepB = null;
            sendAllMessage(resp.toJSONString());
        }finally {
            reentrantLock.unlock();
        }

    }
    @Override
    public void run() {
        for(int i = 0; i < 1000; i++){
            if(nextStep()){//是否读入下一条输入
                //是否获取两条蛇的下一步操作
                judge();
                if(status.equals("playing")){//广播操作
                    sendMove();
                }else{
                    sendResult();
                    break;
                }
            }
//            else{
//                status = "finished";
//                reentrantLock.lock();
//                try {
//                    if(nextStepA == null && nextStepB == null){
//                        loser = "all";
//                    }else if(nextStepA == null){
//                        loser = "A";
//                    }else{
//                        loser = "B";
//                    }
//                }finally {
//                    reentrantLock.unlock();
//                }
//                sendResult();
//                break;
//            }
        }
    }
}

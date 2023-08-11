package com.kob.backend;

import java.util.*;
import java.util.concurrent.ConcurrentMap;

public class test {
//    static List<List<Integer>> graph = new ArrayList<>();
//    static int n,m;
//    static int[] color;
//
//    static int ans = 0;
//    public static void main(String[] args) {
//        Scanner scanner = new Scanner(System.in);
//        n = scanner.nextInt();
//        m = scanner.nextInt();
//        color = new int[n + 1];
//        for(int i = 1; i <= n; i++) {
//            color[i] = scanner.nextInt();
//        }
//        for (int i = 0; i <= n; i++) {
//            graph.add(new ArrayList<>());
//        }
//        for (int i = 0; i < n - 1; i++) {
//            int u = scanner.nextInt(), v = scanner.nextInt();
//            graph.get(u).add(v);
//            graph.get(v).add(u);
//        }
//        dfs(1, 0, 0);
//        System.out.println(ans);  // 1 号节点为根节点
//    }
//    private static void dfs(int node, int parent, int cnt) {
//        if(color[node] == 1) cnt++;
//        else cnt = 0;
//        if(cnt > m) return;
//        int child_cnt = 0;
//            for (int child : graph.get(node)) {
//                if (child != parent) {
//                    child_cnt++;
//                    dfs(child, node, cnt);
//                }
//            }
//            if(child_cnt == 0) ans++;
//    }
      public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int x = scanner.nextInt();
          int cost = 0; // 总代价
          int n = 0; // 当前数值
          while (n < x) {
              int a = x - n; // 当前步骤所需增加的数值
              if ((n << 1) >= x) { // 判断乘2是否能够到达或超过x
                  cost += x - n; // 剩余数值直接加上去
                  break;
              }
              if ((a << 1) > x - n) { // 如果增加a再乘2无法到达x，则直接乘2
                  n <<= 1;
              } else { // 否则增加a
                  n += a;
              }
              cost += a; // 累计代价
          }
          System.out.println(cost);

      }

}

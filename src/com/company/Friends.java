package com.company;

import java.io.*;
import java.util.Scanner;

/**
 * Created by vlad on 01.03.2017.
 */
public class Friends {
    private static Friends f = new Friends();
    private final String friendsPath = "E:\\friends.txt";
    private int graph[][];
    private int n;


    private Friends() {
        upLoad();
    }
    private synchronized void upLoad() {
        n = Users.getInstance().getCurrentID();
        graph = new int[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                graph[i][j] = 0;

        try {
            Scanner scanner = new Scanner(new File(friendsPath));

            while(scanner.hasNextInt()) {
                int id1 = scanner.nextInt();
                int id2 = scanner.nextInt();
                graph[id1 - 1][id2 - 1] = 1;
                graph[id2 - 1][id1 - 1] = 1;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public synchronized void add(int id1, int id2) {
        File file = new File(friendsPath);
        BufferedWriter br = null;
        try {
            br = new BufferedWriter(new FileWriter(file,true));
            br.append(id1 + " " + id2);
            br.newLine();

        } catch (IOException e) {
            e.printStackTrace();
        } finally{
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (id1 <= n && id2 <= n) {
            graph[id1 - 1][id2 - 1] = 1;
            graph[id2 - 1][id1 - 1] = 1;
        }
    }
    public boolean isFriends(int id1, int id2) {
        if (id1 > n || id2 > n) upLoad();
        return graph[id1 - 1][id2 - 1] == 1 ? true : false;
    }
    public static Friends getInstance() {
        return f;
    }
    public int[]  getFrendsList(int id) {
        if (id > n) upLoad();
        int count = 0;
        for (int i = 0; i < n; i++) {
            if (graph[id - 1][i] == 1) count++;
        }
        int arr[] = new int[count];

        for (int i = 0; i < n; i++) {
            if (graph[id - 1][i] == 1){
                arr[count - 1] = i + 1;
                count--;
            }
        }
        return arr;
    }
}

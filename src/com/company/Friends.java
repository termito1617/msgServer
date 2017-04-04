package com.company;

import javafx.util.Pair;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

/**
 * Created by vlad on 01.03.2017.
 */
public class Friends {
    private final int MAX_SIZE_DELETE_LIST = 1;
    private static Friends f = new Friends();
    private final String friendsPath = "friends.txt";
    private List<Pair<Integer, Integer>> deleteList;
    private int graph[][];
    private int n;


    private Friends() {
        deleteList = new ArrayList<>();
        File f = new File(friendsPath);
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        upLoad();

    }

    private synchronized void upLoad() {
        n = Users.getInstance().getCurrentID();
        graph = new int[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                graph[i][j] = 0;

        File tmp = new File("tmp.txt");
        File f   = new File(friendsPath);
        try {
            if (!tmp.exists()) {
                tmp.createNewFile();
            }

            Scanner scanner = new Scanner(f);
            BufferedWriter br = new BufferedWriter(new FileWriter(tmp,false));
            while(scanner.hasNextInt()) {
                int id1 = scanner.nextInt();
                int id2 = scanner.nextInt();

                if (deleteList.size() == 0 ||
                        (!deleteList.contains(new Pair<>(id1, id2)) &&
                        !deleteList.contains(new Pair<>(id2, id1)))) {
                    br.append(id1 + " " + id2);
                    br.newLine();
                    graph[id1 - 1][id2 - 1] = 1;
                    graph[id2 - 1][id1 - 1] = 1;
                } else {
                    graph[id1 - 1][id2 - 1] = 0;
                    graph[id2 - 1][id1 - 1] = 0;
                }
            }

            br.flush();
            scanner.close();
            br.close();
            if (deleteList.size() > 0) {

                f.delete();
                tmp.renameTo(f);
            }
            deleteList.clear();
        } catch (IOException e) {
            GuiServerStatus.getInstance().addToLog("[Friends: ERROR]   " + "upload error");
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
            GuiServerStatus.getInstance().addToLog("[Friends: ERROR]   " + "add error  + id1: " + id1 + "    id2: " + id2);
        } finally{
            try {
                br.close();
            } catch (IOException e) {
                GuiServerStatus.getInstance().addToLog("[Friends: ERROR]   " + "add error(Закрытие файла)  + id1: " + id1 + "    id2: " + id2);
            }
        }
        if (id1 <= n && id2 <= n) {
            graph[id1 - 1][id2 - 1] = 1;
            graph[id2 - 1][id1 - 1] = 1;
        }
    }
    public boolean isFriends(int id1, int id2) {
        if (id1 > n || id2 > n) upLoad();
        if (deleteList != null && (deleteList.contains(new Pair<>(id1, id2)) || deleteList.contains(new Pair<>(id2, id1))))
            return false;

        return graph[id1 - 1][id2 - 1] == 1;
    }
    public static Friends getInstance() {
        return f;
    }
    public int[]  getFrendsList(int id) {
        if (id > n) upLoad();
        int count = 0;
        for (int i = 0; i < n; i++) {
            if (graph[id - 1][i] == 1) {
                count++;
                if (deleteList != null && (deleteList.contains(new Pair<>(id, i)) || deleteList.contains(new Pair<>(i, id))))
                    count--;
            }
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
    public void delete(int id1, int id2) {
        if (id1 <= n && id2 <= n && id1 >= 1 && id2 >= 1) {
            graph[id1 - 1][id2 - 1] = 0;
            graph[id2 - 1][id1 - 1] = 0;
        }
        Pair<Integer, Integer> p = new Pair<>(id1, id2);
        deleteList.add(p);
        if (deleteList.size() >= MAX_SIZE_DELETE_LIST) {
            upLoad();

        }
    }
}

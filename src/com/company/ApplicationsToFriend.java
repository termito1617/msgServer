package com.company;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vlad on 28.03.2017.
 */
public class ApplicationsToFriend {
    private String path;
    private int id;
    private List<String> msgsList;
    private List<Integer> idList;

    public ApplicationsToFriend(int id) {
        this.id = id;
        idList = null;
        msgsList = null;
        path = "E:\\MyICQserver\\ApplicationsToFriend\\" + id;
        File f = new File("E:\\MyICQserver");
        if (!f.exists()) f.mkdir();
        f = new File("E:\\MyICQserver\\ApplicationsToFriend");
        if (!f.exists()) f.mkdir();
    }

    public boolean add(String msg, int idFrom) {
        if (isContains(idFrom)) return true;
        File file  = new File(path + ".txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                return false;
            }
        }
        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter(file, true));
            bw.append(idFrom + " " + msg);
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            return false;
        }
        return true;

    }
    public boolean isContains(int id) {
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader(path + ".txt"));
        } catch (FileNotFoundException e) {
            return false;
        }

        String line;

        try {
            while ((line = reader.readLine()) != null) {
                String[] strs = line.split(" ", 2);
                if (id == Integer.parseInt(strs[0]))
                    return true;

            }
            reader.close();
        } catch (IOException e) {
            return false;
        }
        return false;
    }
    public List<String> getMsgsList() {
        if (msgsList == null) {
            BufferedReader reader;

            try {
                reader = new BufferedReader(new FileReader(path + ".txt"));
            } catch (FileNotFoundException e) {
                return msgsList;
            }

            String line;
            msgsList = new ArrayList<>();
            idList = new ArrayList<>();
            try {
                while ((line = reader.readLine()) != null) {
                    String[] strs = line.split(" ", 2);
                    idList.add(new Integer(strs[0]));
                    msgsList.add(strs[1]);

                }
                reader.close();
            } catch (IOException e) {
                msgsList = null;
                idList = null;
            }

        }
        return msgsList;
    }
    public List<Integer> getIdList() {
        if (idList == null) {
            getMsgsList();
        }
        return idList;
    }
    public void clear() {
        File file  = new File(path + ".txt");
        if (file.exists()) {
            file.delete();
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

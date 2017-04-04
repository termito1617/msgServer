package com.company;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vlad on 20.03.2017.
 */
public class MessageManager {
    private String path;
    private int idFrom;


    public MessageManager(int idFrom) {
        this.idFrom = idFrom;
        path = "E:\\MyICQserver\\Messages\\" + idFrom;
        File f = new File("E:\\MyICQserver");
        if (!f.exists()) f.mkdir();
        f = new File("E:\\MyICQserver\\Messages");
        if (!f.exists()) f.mkdir();
        f = new File(path);
        if (!f.exists()) f.mkdir();
    }

    public boolean saveMessage(int idTo, String text) {
        File file  = new File(path, idTo + ".txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                GuiServerStatus.getInstance().addToLog("[MessageManager: ERROR]   " + "файл не создан  + id1: " +
                        idFrom + "    idTo: " + idTo);

                return false;
            }
        }
        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter(file, true));
            bw.append(text);
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            GuiServerStatus.getInstance().addToLog("[MessageManager: ERROR]   " + "ошибка записи  + id1: " +
                    idFrom + "    idTo: " + idTo);
            return false;
        }
        return true;
    }

    public void clearMessages(int idTo) {
        File file  = new File(path, idTo + ".txt");
        if (file.exists()) {
            file.delete();
        }
    }

    public List<String> getMessages(int idTo) {
        BufferedReader reader;
        List<String> msgList = new ArrayList<>();

        try {
            reader = new BufferedReader(new FileReader(path + "\\" + idTo + ".txt"));
        } catch (FileNotFoundException e) {
            GuiServerStatus.getInstance().addToLog("[MessageManager: ERROR]   " + "файл не найден  + id1: " +
                    idFrom + "    idTo: " + idTo);
            return null;
        }

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                msgList.add(line);
            }
            reader.close();
            return msgList;
        } catch (IOException e) {
            GuiServerStatus.getInstance().addToLog("[MessageManager: ERROR]   " + "ошибка чтения  + id1: " +
                    idFrom + "    idTo: " + idTo);
            return null;
        }

    }
}

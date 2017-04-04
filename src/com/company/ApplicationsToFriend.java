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

    public synchronized boolean add(String msg, int idFrom) {
        if (isContains(idFrom)) return true;
        File file  = new File(path + ".txt");
        File fHistory = new File(path + "H.txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                GuiServerStatus.getInstance().addToLog("[ApplicateionsToFriend]  id: "  + id + " idFrom: " + idFrom +
                        "\n    Ошибка создания файла");
                return false;
            }
        }
        if (!fHistory.exists()) {
            try {
                fHistory.createNewFile();
            } catch (IOException e) {
                GuiServerStatus.getInstance().addToLog("[ApplicateionsToFriend]  id: "  + id + " idFrom: " + idFrom +
                        "\n    Ошибка создания файла Н");
                return false;
            }
        }
        BufferedWriter bw;
        BufferedWriter bwH;
        try {
            bw = new BufferedWriter(new FileWriter(file, true));
            bwH = new BufferedWriter(new FileWriter(fHistory, true));
            bwH.append("" + idFrom);
            bwH.newLine();
            bw.append(idFrom + " " + msg);
            bw.newLine();
            bw.close();
            bwH.close();
        } catch (IOException e) {
            GuiServerStatus.getInstance().addToLog("[ApplicateionsToFriend]  id: "  + id + " idFrom: " + idFrom +
                    "\n    Ошибка добавления");
            return false;
        }
        GuiServerStatus.getInstance().addToLog("[ApplicateionsToFriend]  id: "  + id + " idFrom: " + idFrom +
                "\n    Добавлено");
        return true;

    }
    public boolean isContains(int id) {
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader(path + "H.txt"));
        } catch (FileNotFoundException e) {
            GuiServerStatus.getInstance().addToLog("[ApplicateionsToFriend]  id: "  + this.id + " *id: " + id +
                    "\n    Файл Н не найден");
            return false;
        }

        String line;

        try {
            while ((line = reader.readLine()) != null) {
                if (id == Integer.parseInt(line)){
                    reader.close();
                    return true;
                }

            }
            reader.close();
        } catch (IOException e) {
            GuiServerStatus.getInstance().addToLog("[ApplicateionsToFriend]  id: "  + id +
                    "\n    Ошибка чтения файла");
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
                GuiServerStatus.getInstance().addToLog("[ApplicateionsToFriend]  id: "  + id +
                        "\n    Файл не найден");
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
                GuiServerStatus.getInstance().addToLog("[ApplicateionsToFriend]  id: "  + id +
                        "\n    Ошибка чтения файла");
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
    public void deleteFromHistory(int id) {
        File fileHistory = new File(path + "H.txt");
        File tmp         = new File(path + "Htmp.txt");

        BufferedReader reader;
        BufferedWriter bw;


        String line;
        try {
            bw = new BufferedWriter(new FileWriter(tmp, true));
            reader = new BufferedReader(new FileReader(fileHistory));
            while ((line = reader.readLine()) != null) {
                if (Integer.parseInt(line) != id) {
                    bw.append(line);
                    bw.newLine();
                }
            }
            reader.close();
            bw.close();
            fileHistory.delete();
            tmp.renameTo(fileHistory);
        } catch (IOException e) {
            GuiServerStatus.getInstance().addToLog("[ApplicateionsToFriend]  id: "  + this.id + " *id: " + id +
                    "\n    Ошибка чтения/записи файла");
            return;
        }


    }
    public int getId() {
        return id;
    }
    public void addToHistory(int id) {
        File fHistory = new File(path + "H.txt");
        if (!fHistory.exists()) {
            try {
                fHistory.createNewFile();
            } catch (IOException e) {
                GuiServerStatus.getInstance().addToLog("[ApplicateionsToFriend]  id: "  + this.id + " *id: " + id +
                        "\n    Ошибка создания файла Н");
                return;
            }
        }
        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter(fHistory, true));
            bw.append("" + id);
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            GuiServerStatus.getInstance().addToLog("[ApplicateionsToFriend]  id: "  + this.id + " *id: " + id +
                    "\n    Ошибка записи в файл");
            return;
        }
    }

    public void setId(int id) {
        this.id = id;
    }
}

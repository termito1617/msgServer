package com.company;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vlad on 22.02.2017.
 */
public class Users {
    public static final int ADD = 1;
    public static final int LOGIN = 2;
    public static final int SEARCH_BY_ID = 3;
    public static final int SEARCH_BY_NAME = 4;
    private static Users users = new Users();
    private final String usersPath = "E:\\users.txt";
    private int currentID;

    private Users() {
        currentID = initID();
    }

    private int initID() {
        try{
            File myFile =new File(usersPath);
            FileReader fileReader = new FileReader(myFile);
            LineNumberReader lineNumberReader = new LineNumberReader(fileReader);
            int lineNumber = 0;
            while (lineNumberReader.readLine() != null){
                lineNumber++;
            }
            lineNumberReader.close();
            return lineNumber;

        }catch(IOException e){
            GuiServerStatus.getInstance().addToLog("[MessageManager: ERROR]   " + "ошибка инициалищации(чтения)");
            System.exit(-1);
            return 0;
        }
    }



    private int login(User user) {
        User result = searchByLogin(user.getLogin());
        if (result == null) {
            return 0;
        }
        if (!result.getPassHash().equals(user.getPassHash())) {
            return 0;
        }
        return result.getId();

    }
    private int addUser(User user) {
        if (searchByLogin(user.getLogin()) != null) return 0;
        String strUser = user.toString();
        File file = new File(usersPath);
        BufferedWriter br = null;
        try {
            br = new BufferedWriter(new FileWriter(file,true));
            br.append(strUser);
            br.newLine();

        } catch (IOException e) {
            GuiServerStatus.getInstance().addToLog("[Users: ERROR]   " + "Ошибка записи");
        } finally{
            try {
                br.close();
            } catch (IOException e) {
                GuiServerStatus.getInstance().addToLog("[Users: ERROR]   " + "Ошибка закрытия потока");
            }
        }
        currentID++;
        return currentID;
    }
    private User searchByName(String name, String surname) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(usersPath));
            User user = new User();
            String line;
            int c = 1;

            while ((line = reader.readLine()) != null) {
                user.initFromString(line);
                if (user.getName().equals(name) && user.getLastName().equals(surname)) {
                    user.setId(c);
                    return user;
                }
                c++;
            }
            return null;
        }
        catch (IOException e) {
            GuiServerStatus.getInstance().addToLog("[Users: ERROR]   " + "Ошибка чтения");
            return null;
        }
    }
    private User searchByID(int id) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(usersPath));
            User user = null;
            String line;
            int c = 1;

            while ((line = reader.readLine()) != null) {
                if (c == id) {
                    user = new User(line);
                    user.setId(c);
                    return user;
                }
                c++;
            }
            return user;
        }
        catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    private User searchByLogin(String login){
        try {
            BufferedReader reader = new BufferedReader(new FileReader(usersPath));
            User user = new User();
            String line;
            int c = 1;

            while ((line = reader.readLine()) != null) {
                user.initFromString(line);
                if (user.getLogin().equals(login)) {
                    user.setId(c);
                    return user;
                }
                c++;
            }
            return null;
        }
        catch (IOException e) {
            GuiServerStatus.getInstance().addToLog("[Users: ERROR]   " + "Ошибка записи");
            return null;
        }
    }
    private List<User> search(String name) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(usersPath));
            List<User> users = new ArrayList<>();

            User user;
            String line;
            int c = 1;

            while ((line = reader.readLine()) != null) {
                user = new User(line);
                if ((user.getName() + " " + user.getLastName()).contains(name)) {
                    user.setId(c);
                    users.add(user);
                }
                c++;

            }

            return users;
        }
        catch (IOException e) {
            GuiServerStatus.getInstance().addToLog("[Users: ERROR]   " + "Ошибка записи");
            return null;
        }
    }


    public static Users getInstance() {
        return users;
    }

    public synchronized Object msgToFile(int type, Object a) {
        switch (type) {
            case ADD:
                return addUser((User)a);
            case LOGIN:
                return login((User)a);
            case SEARCH_BY_ID:
                return searchByID((Integer)a);

            case SEARCH_BY_NAME:
                return search((String)a);
            default:
                return null;
        }
    }



    public int getCurrentID() {
        return currentID;
    }

}

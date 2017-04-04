package com.company;


import javafx.util.Pair;
import org.xml.sax.InputSource;
import sun.plugin2.message.Message;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


class ServeOneJabber extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private int idOfCurrentUser;
    HashMap<Integer, Socket> hm;

    public ServeOneJabber(Socket s, HashMap<Integer, Socket> hm) throws IOException {
        GuiServerStatus.getInstance().addToLog("[New connecting]  " + s);
        socket = s;
        this.hm = hm;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        start();
    }


    public void run() {
        try {

            System.out.println("new socket");
            idOfCurrentUser = 0;
            int accessOfCurrentUser = 0;

            boolean isEnd = false;
            while (!isEnd) {
                String str = in.readLine();
                GuiServerStatus.getInstance().addToLog("[MsgToSever]  " + socket + "   id: " + idOfCurrentUser + "\n        " + str);

                SAXHandler handler;
                try {
                    SAXParserFactory factory = SAXParserFactory.newInstance();
                    SAXParser parser = factory.newSAXParser();
                    handler = new SAXHandler();
                    parser.parse(new InputSource(new StringReader(str)), handler);
                } catch (Exception e) {
                    GuiServerStatus.getInstance().addToLog("[SAXError]  Ошибка анализа запроса");
                    continue;
                }

                User user;
                int result;
                switch (handler.getType()) {
                    case "registration":
                        user = new User();
                        user.setLogin(handler.getValue("login"));
                        user.setPassHash(handler.getValue("pass"));
                        user.setName(handler.getValue("name"));
                        user.setLastName(handler.getValue("surname"));
                        result = (Integer)(Users.getInstance().msgToFile(Users.ADD, user));
                        GuiServerStatus.getInstance().addToLog("[Registration]  " + socket + "   " + handler.getValue("login") +
                                " " + handler.getValue("name") + " " + handler.getValue("surname") + "\n      result: " + result);
                        if (result > 0) {
                            hm.put(result, socket);
                            idOfCurrentUser = result;
                            accessOfCurrentUser = 1;
                        }
                        String string = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                                "<message type=\"registration\">" +
                                "<result>" + result + "</result>" +
                                "</message>";
                        out.println(string);
                        GuiServerStatus.getInstance().addToLog("[MsgFromServer]  " + socket + "\n     " + string);
                        break;
                    case "login":

                        user = new User();
                        user.setLogin(handler.getValue("login"));
                        user.setPassHash(handler.getValue("pass"));
                        result = (Integer)(Users.getInstance().msgToFile(Users.LOGIN, user));
                        GuiServerStatus.getInstance().addToLog("[Login]  " + socket + "  " + handler.getValue("login") + "\n     result: " + result);
                        if (result > 0) {
                            hm.put(result, socket);
                            idOfCurrentUser = result;
                            accessOfCurrentUser = 1;
                            int[] idFriends = Friends.getInstance().getFrendsList(idOfCurrentUser);
                            if (idFriends == null) break;
                            for (int id: idFriends) {
                                Socket s = hm.get(id);
                                if (s != null) {
                                    PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);
                                    String sStatus = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><message type=\"setOnline\">" +
                                            "<id>" + idOfCurrentUser + "</id>" +
                                            "<status>1</status>" +
                                            "</message>";
                                    pw.println(sStatus);
                                    GuiServerStatus.getInstance().addToLog("[MsgFromServer]  " + s + "\n     " + sStatus);

                                }
                            }
                        }
                        str = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                                "<message type=\"login\">" +
                                "<result>" + result + "</result>" +
                                "</message>";
                        out.println(str);
                        GuiServerStatus.getInstance().addToLog("[MsgFromServer]  " + socket + "\n     " + str);
                        break;
                    case "end":
                        isEnd = true;
                        break;
                    case "add_friend": {

                        int idTo   = Integer.parseInt(handler.getValue("to"));
                        if (accessOfCurrentUser == 1  && Users.getInstance().getCurrentID() >= idTo) {
                            Friends.getInstance().add(idOfCurrentUser, idTo);
                            result = 1;
                        } else {
                            result = -1;
                        }

                        GuiServerStatus.getInstance().addToLog("[AddFriend]  " + socket +
                                "  from: " + idOfCurrentUser + "  to: " + idTo + "\n     result: " + result);

                        str = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                                "<message type=\"add_frend\">" +
                                "<result>" + result + "</result>" +
                                "</message>";
                        out.println(str);
                        GuiServerStatus.getInstance().addToLog("[MsgFromServer]  " + socket + "\n      " + str);
                        break;
                    }
                    case "get_list_of_friends": {
                        if (accessOfCurrentUser == 1) {
                            int arr[] = Friends.getInstance().getFrendsList(idOfCurrentUser);
                            GuiServerStatus.getInstance().addToLog("[getListOfFriends]  " + socket + "   id: " +idOfCurrentUser);
                            String outStr = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><message type=\"friends\">";
                            for (int i = 0; i < arr.length; i++) {
                                User u = (User)Users.getInstance().msgToFile(Users.SEARCH_BY_ID, arr[i]);
                                outStr += "<user><id>" + u.getId() + "</id><name>" + u.getName() +
                                        "</name><lastName>" + u.getLastName() + "</lastName></user>";
                            }
                            outStr += "</message>";

                            out.println(outStr);
                            GuiServerStatus.getInstance().addToLog("[MsgFromServer]  " + socket +
                                    "   id:" + idOfCurrentUser + "\n     " + outStr);

                        }
                        break;
                    }
                    case "get_list_of_users": {
                        GuiServerStatus.getInstance().addToLog("[getListOfUser]  " + socket +
                                "   id: " + idOfCurrentUser + "  serch: " +handler.getValue("name"));
                        List<User> users = (List<User>)(Users.getInstance().msgToFile(Users.SEARCH_BY_NAME, handler.getValue("name")));
                        String outStr = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><message type=\"users\">";
                        for (User u: users) {
                            if (!Friends.getInstance().isFriends(idOfCurrentUser, u.getId()) && idOfCurrentUser != u.getId()) {
                                outStr += "<user><id>" + u.getId() + "</id><name>" + u.getName() +
                                        "</name><lastName>" + u.getLastName() + "</lastName></user>";
                            }
                        }
                        outStr += "</message>";

                        out.println(outStr);
                        GuiServerStatus.getInstance().addToLog("[MsgFromServer]  " + socket +
                                "   id:" + idOfCurrentUser + "\n     " + outStr);
                        break;
                    }
                    case "info": {
                        GuiServerStatus.getInstance().addToLog("[Info]  " +
                                socket + "   id: " + idOfCurrentUser + "  search: " + handler.getValue("id"));

                        if (Integer.parseInt(handler.getValue("id")) == 0) {
                            user = (User)(Users.getInstance().msgToFile(Users.SEARCH_BY_ID, idOfCurrentUser));
                            if (accessOfCurrentUser == 1) {
                                if (Integer.parseInt(handler.getValue("id")) == 0) {
                                    str = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                                            "<message type=\"info\">" +
                                            "<login>" + user.getLogin() + "</login>" +
                                            "<name>" + user.getName() + "</name>" +
                                            "<id>" + idOfCurrentUser + "</id>" +
                                            "<surname>" + user.getLastName() + "</surname>" +
                                            "</message>";
                                    out.println(str);
                                    GuiServerStatus.getInstance().addToLog("[MsgFromServer]  " + socket +
                                            "   id:" + idOfCurrentUser + "\n     " + str);
                                }
                            }
                        } else {
                            user = (User)(Users.getInstance().msgToFile(Users.SEARCH_BY_ID, Integer.parseInt(handler.getValue("id"))));
                            if (user != null) {
                                str = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                                        "<message type=\"info\">" +
                                        "<name>" + user.getName() + "</name>" +
                                        "<id>" + handler.getValue("id") + "</id>" +
                                        "<surname>" + user.getLastName() + "</surname>" +
                                        "</message>";
                                out.println(str);
                                GuiServerStatus.getInstance().addToLog("[MsgFromServer]  " + socket +
                                        "   id:" + idOfCurrentUser + "\n     " + str);
                            }
                        }


                        break;
                    }
                    case "answerForNewFriend": {
                        if (accessOfCurrentUser != 1) break;
                        int id = Integer.parseInt(handler.getValue("id"));
                        ApplicationsToFriend apt = new ApplicationsToFriend(idOfCurrentUser);
                        GuiServerStatus.getInstance().addToLog("[answerForNewFriend]  " + socket +
                                "   id:" + idOfCurrentUser);
                        if (apt.isContains(id)) {
                            apt.deleteFromHistory(id);
                            if (handler.getValue("answer").equals("1")) {
                                Friends.getInstance().add(idOfCurrentUser, id);
                                Socket s = hm.get(id);
                                if (s != null) {
                                    PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);
                                    User u = (User)Users.getInstance().msgToFile(Users.SEARCH_BY_ID, idOfCurrentUser);
                                    str = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                                            "<message type=\"confimFriend\">" +
                                            "<id>" + idOfCurrentUser + "</id>" +
                                            "<name>" + u.getName() + "</name>" +
                                            "<surname>" + u.getLastName() + "</surname>" +
                                            "</message>";
                                    pw.println(str);
                                    GuiServerStatus.getInstance().addToLog("[MsgFromServer]  " + s +
                                            "   id:" + idOfCurrentUser + "\n     " + str);
                                }
                            }
                        }


                        break;
                    }
                    case "msg": {
                        if (accessOfCurrentUser != 1)break;
                        int id = Integer.parseInt(handler.getValue("to"));
                        if (!Friends.getInstance().isFriends(idOfCurrentUser, id)) break;
                        Socket s = hm.get(id);
                        GuiServerStatus.getInstance().addToLog("[Msg]  " + socket +
                                "   id:" + idOfCurrentUser + " toID: " + handler.getValue("to"));
                        if (s != null) {
                            PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);
                            str = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><message type=\"msg\">" +
                                    "<from>" + idOfCurrentUser + "</from>" +
                                    "<text>" + handler.getValue("text") + "</text>" +
                                    "</message>";
                            pw.println(str);
                            GuiServerStatus.getInstance().addToLog("[MsgFromServer]  " + s +
                                    "   id:" + idOfCurrentUser + "\n    " + str);
                        } else {
                            MessageManager msgMng = new MessageManager(idOfCurrentUser);
                            if (!msgMng.saveMessage(id,  handler.getValue("text"))) {
                                GuiServerStatus.getInstance().addToLog("[MsgManagerERROR]  Cообщение не сохранено! from: " +
                                        idOfCurrentUser +  "  to: " + handler.getValue("id") + "\n     Текст: " +  handler.getValue("text"));
                            }
                        }
                        break;
                    }
                    case "get_new_messages": {
                        if (accessOfCurrentUser != 1)break;
                        GuiServerStatus.getInstance().addToLog("[getNewMessages]  " + socket +
                                "   id:" + idOfCurrentUser);
                        int FriendsId[] = Friends.getInstance().getFrendsList(idOfCurrentUser);
                        String outStr = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><message type=\"messages\">";
                        for (int id: FriendsId) {
                            MessageManager msgMng = new MessageManager(id);
                            List<String> msgList = msgMng.getMessages(idOfCurrentUser);
                            msgMng.clearMessages(idOfCurrentUser);
                            if (msgList == null) continue;
                            for (String m: msgList) {
                                outStr += "<id>" + id + "</id><text>" + m + "</text>";
                            }
                        }
                        outStr += "</message>";
                        out.println(outStr);
                        GuiServerStatus.getInstance().addToLog("[MsgFromServer]  " + socket +
                                "   id:" + idOfCurrentUser + "\n    " + outStr);
                        break;
                    }
                    case "delete": {
                        if (accessOfCurrentUser != 1) break;
                        if (Friends.getInstance().isFriends(Integer.parseInt(handler.getValue("id")), idOfCurrentUser)) {
                            GuiServerStatus.getInstance().addToLog("[delete]  " + socket +
                                    "   id:" + idOfCurrentUser + "  deleteID  " + handler.getValue("id"));
                            Friends.getInstance().delete(Integer.parseInt(handler.getValue("id")), idOfCurrentUser);
                            Socket s = hm.get(Integer.parseInt(handler.getValue("id")));
                            if (s != null) {
                                PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);
                                User u = (User)Users.getInstance().msgToFile(Users.SEARCH_BY_ID, idOfCurrentUser);
                                str = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                                        "<message type=\"removeFromFriend\">" +
                                            "<id>" + idOfCurrentUser + "</id>" +
                                        "<name>" + u.getName() + "</name>" +
                                        "<surname>" + u.getLastName() + "</surname>" +
                                        "</message>";
                                pw.println(str);
                                GuiServerStatus.getInstance().addToLog("[MsgFromServer]  " + s +
                                        "   id:" + idOfCurrentUser + "\n    " + str);
                            }
                        }

                        break;
                    }
                    case "is_online": {
                        if (hm.get(Integer.parseInt(handler.getValue("id"))) != null) {
                            result = 1;
                        } else{
                            result = 0;
                        }
                        GuiServerStatus.getInstance().addToLog("[isOnline]  " + socket +
                                "   id: " + idOfCurrentUser + "  isOnlineID: " + handler.getValue("id") + "\n    result: " + result);
                        str = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                                "<message type=\"is_online\">" +
                                    "<result>" + result + "</result>" +
                                "</message>";
                        out.println(str);
                        GuiServerStatus.getInstance().addToLog("[MsgFromServer]  " + socket +
                                "   id:" + idOfCurrentUser + "\n    " + str);
                        break;
                    }
                    case "newFriend": {
                        if (accessOfCurrentUser != 1 ||
                                Friends.getInstance().isFriends( idOfCurrentUser, Integer.parseInt(handler.getValue("id")) )) break;
                        Socket s = hm.get(Integer.parseInt(handler.getValue("id")));
                        GuiServerStatus.getInstance().addToLog("[newFriend]  " + socket +
                                "   id:" + idOfCurrentUser + "  FriendID: "  + handler.getValue("id"));
                        if (s != null) {
                            PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);
                            User u = (User )Users.getInstance().msgToFile(Users.SEARCH_BY_ID, idOfCurrentUser);
                            str = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                                    "<message type=\"newFriend\">" +
                                        "<id>" + idOfCurrentUser + "</id>" +
                                        "<text>" + handler.getValue("text") + "</text>" +
                                        "<name>" + u.getName() + "</name>" +
                                        "<surname>" + u.getLastName() + "</surname>" +
                                    "</message>";
                            pw.println(str);
                            GuiServerStatus.getInstance().addToLog("[MsgFromServer]  " + socket +
                                    "   id:" + idOfCurrentUser + "\n    " + str);
                            ApplicationsToFriend apt = new ApplicationsToFriend(Integer.parseInt(handler.getValue("id")));
                            apt.addToHistory(idOfCurrentUser);
                        } else {
                            ApplicationsToFriend atp = new ApplicationsToFriend(Integer.parseInt(handler.getValue("id")));
                            atp.add(handler.getValue("text"), idOfCurrentUser);
                        }
                        break;
                    }
                    case "getNewApp": {
                        ApplicationsToFriend apt = new ApplicationsToFriend(idOfCurrentUser);
                        GuiServerStatus.getInstance().addToLog("[getNewApp]  " + socket +
                                "   id:" + idOfCurrentUser);
                        List<String> lText = apt.getMsgsList();
                        List<Integer> lId = apt.getIdList();
                        String outstr = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><message type=\"newFriends\">";
                        if (lText != null) {
                            for (int i = 0; i < lText.size();i++ ){
                                outstr += "<id>" + lId.get(i) + "</id><text>" + lText.get(i) + "</text>";
                            }
                            apt.clear();
                        }
                        outstr += "</message>";
                        out.println(outstr);
                        GuiServerStatus.getInstance().addToLog("[MsgFromServer]  " + socket +
                                "   id:" + idOfCurrentUser + "\n    " + outstr);
                        break;
                    }

                    default:
                        GuiServerStatus.getInstance().addToLog("[MsgToServerERROR]  " + socket +
                                "   id:" + idOfCurrentUser + "\n     Неверный тип запроса");
                        str = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                                "<message type=\"error\"></message>";
                        out.println(str);
                        GuiServerStatus.getInstance().addToLog("[MsgFromServer]  " + socket +
                                "   id:" + idOfCurrentUser + "\n    " + str);
                }

            }

        } catch (IOException e) {
            System.out.println("ERRROOOOORRR" + idOfCurrentUser);
        } finally {
            if (idOfCurrentUser != 0) {
                int[] idFriends = Friends.getInstance().getFrendsList(idOfCurrentUser);
                if (idFriends == null) return;
                for (int id : idFriends) {
                    Socket s = hm.get(id);
                    if (s != null) {
                        PrintWriter pw = null;
                        try {
                            pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);
                            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><message type=\"setOnline\">" +
                                    "<id>" + idOfCurrentUser + "</id>" +
                                    "<status>0</status>" +
                                    "</message>");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }


                    }
                }
                hm.remove(idOfCurrentUser);
                GuiServerStatus.getInstance().addToLog("[End Conecting]  " + socket + "  id: " +idOfCurrentUser);
            }
            try {
                System.err.println("Socket closed");
                socket.close();
            }
            catch (IOException e) {
                System.err.println("Socket not closed");
            }
        }
    }

}

public class Main {
    private static ServerSocket s;
    private static final int PORT = 8080;
    private static HashMap<Integer, Socket> hm = new HashMap<>();


    public static void main(String[] args) throws IOException {
        s = new ServerSocket(PORT);
        GuiServerStatus.getInstance().setVisible(true);
        GuiServerStatus.getInstance().setHm(hm);
        GuiServerStatus.getInstance().addToLog("[Status] Started");
        try {
            while (true) {
                Socket socket = s.accept();
                new ServeOneJabber(socket, hm);
            }
        } finally {
            s.close();
            GuiServerStatus.getInstance().addToLog("[Status]  Stoped");
        }

    }
}
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


                SAXHandler handler;
                try {
                    SAXParserFactory factory = SAXParserFactory.newInstance();
                    SAXParser parser = factory.newSAXParser();
                    handler = new SAXHandler();
                    parser.parse(new InputSource(new StringReader(str)), handler);
                } catch (Exception e) {
                    out.println(Integer.toString(-10));
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
                        if (result > 0) {
                            hm.put(result, socket);
                            idOfCurrentUser = result;
                            accessOfCurrentUser = 1;
                        }
                        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                                "<message type=\"registration\">" +
                                "<result>" + result + "</result>" +
                                "</message>");
                        break;
                    case "login":

                        user = new User();
                        user.setLogin(handler.getValue("login"));
                        user.setPassHash(handler.getValue("pass"));
                        result = (Integer)(Users.getInstance().msgToFile(Users.LOGIN, user));
                        if (result > 0) {
                            hm.put(result, socket);
                            idOfCurrentUser = result;
                            accessOfCurrentUser = 1;
                        }
                        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                                "<message type=\"login\">" +
                                "<result>" + result + "</result>" +
                                "</message>");

                        int[] idFriends = Friends.getInstance().getFrendsList(idOfCurrentUser);
                        if (idFriends == null) break;
                        for (int id: idFriends) {
                            Socket s = hm.get(id);
                            if (s != null) {
                                PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);
                                pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><message type=\"setOnline\">" +
                                        "<id>" + idOfCurrentUser + "</id>" +
                                        "<status>1</status>" +
                                        "</message>");

                            }
                        }
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
                        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                                "<message type=\"add_frend\">" +
                                "<result>" + result + "</result>" +
                                "</message>");
                        break;
                    }
                    case "get_list_of_friends": {
                        if (accessOfCurrentUser == 1) {
                            int arr[] = Friends.getInstance().getFrendsList(idOfCurrentUser);
                            String outStr = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><message type=\"friends\">";
                            for (int i = 0; i < arr.length; i++) {
                                User u = (User)Users.getInstance().msgToFile(Users.SEARCH_BY_ID, arr[i]);
                                outStr += "<user><id>" + u.getId() + "</id><name>" + u.getName() +
                                        "</name><lastName>" + u.getLastName() + "</lastName></user>";
                            }
                            outStr += "</message>";

                            out.println(outStr);

                        }
                        break;
                    }
                    case "get_list_of_users": {
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
                        break;
                    }
                    case "info": {
                        user = (User)(Users.getInstance().msgToFile(Users.SEARCH_BY_ID, idOfCurrentUser));
                        if (accessOfCurrentUser == 1) {
                            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><message type=\"info\"><login>" +
                                    user.getLogin() + "</login><name>" +
                                    user.getName() + "</name><surname>" + user.getLastName() + "</surname><id>"+
                                    idOfCurrentUser + "</id></message>");
                        }
                        break;
                    }
                    case "msg": {
                        if (accessOfCurrentUser != 1)break;
                        int id = Integer.parseInt(handler.getValue("to"));
                        if (!Friends.getInstance().isFriends(idOfCurrentUser, id)) break;
                        Socket s = hm.get(id);
                        if (s != null) {
                            PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);
                            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><message type=\"msg\">" +
                                    "<from>" + idOfCurrentUser + "</from>" +
                                    "<text>" + handler.getValue("text") + "</text>" +
                                    "</message>");
                        } else {
                            MessageManager msgMng = new MessageManager(idOfCurrentUser);
                            if (!msgMng.saveMessage(id,  handler.getValue("text"))) {
                                System.out.println("Не сохранено: " + handler.getValue("text"));
                            }
                        }
                        break;
                    }
                    case "get_new_messages": {
                        if (accessOfCurrentUser != 1)break;
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
                        break;
                    }
                    case "delete": {
                        Friends.getInstance().delete(Integer.parseInt(handler.getValue("id")), idOfCurrentUser);
                        break;
                    }
                    case "is_online": {
                        if (hm.get(Integer.parseInt(handler.getValue("id"))) != null) {
                            result = 1;
                        } else{
                            result = 0;
                        }
                        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                                "<message type=\"is_online\">" +
                                "<result>" + result + "</result>" +
                                "</message>");
                        break;
                    }
                    case "newFriend": {
                        if (accessOfCurrentUser != 1 ||
                                Friends.getInstance().isFriends( idOfCurrentUser, Integer.parseInt(handler.getValue("id")) )) break;
                        Socket s = hm.get(Integer.parseInt(handler.getValue("id")));
                        if (s != null) {
                            PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(s.getOutputStream())), true);
                            pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><message type=\"newFriend\">" +
                                    "<id>" + idOfCurrentUser + "</id>" +
                                    "<text>" + handler.getValue("text") + "</text>" +
                                    "</message>");
                        } else {
                            ApplicationsToFriend atp = new ApplicationsToFriend(Integer.parseInt(handler.getValue("id")));
                            atp.add(handler.getValue("text"), idOfCurrentUser);
                        }
                        break;
                    }

                    default:
                        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
                                "<message type=\"error\"></message>");

                }

            }

        } catch (IOException e) {
            System.out.println("ERRROOOOORRR " + idOfCurrentUser);
        } finally {
            int[] idFriends = Friends.getInstance().getFrendsList(idOfCurrentUser);
            if (idFriends == null) return;
            for (int id: idFriends) {
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

    private static final int PORT = 8080;
    private static HashMap<Integer, Socket> hm = new HashMap<>();

    public static void main(String[] args) throws IOException {


        ServerSocket s = new ServerSocket(PORT);
        System.out.println("Server Started");

        try {
            while (true) {
                // Блокируется до возникновения нового соединения:
                Socket socket = s.accept();
                try {
                    new ServeOneJabber(socket, hm);
                }
                catch (IOException e) {
                    // Если завершится неудачей, закрывается сокет,
                    // в противном случае, нить закроет его:
                    socket.close();
                }
            }
        }
        finally {
            s.close();
        }





    }
}
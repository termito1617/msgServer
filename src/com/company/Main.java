package com.company;


import org.xml.sax.InputSource;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;


class ServerOneJabber extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ServerOneJabber(Socket s) throws IOException {
        socket = s;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        start();
    }


    public void run() {
        try {
            System.out.println("new socket");
            int idOfCurrentUser = 0;
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
                            idOfCurrentUser = result;
                            accessOfCurrentUser = 1;
                        }
                        out.println(result);
                        break;
                    case "login":

                        user = new User();
                        user.setLogin(handler.getValue("login"));
                        user.setPassHash(handler.getValue("pass"));
                        result = (Integer)(Users.getInstance().msgToFile(Users.LOGIN, user));
                        if (result > 0) {
                            idOfCurrentUser = result;
                            accessOfCurrentUser = 1;
                        }
                        out.println(result);
                        break;
                    case "end":
                        isEnd = true;
                        break;
                    case "add_friend": {
                        int idTo   = Integer.parseInt(handler.getValue("to"));
                        if (accessOfCurrentUser == 1  && Users.getInstance().getCurrentID() >= idTo) {
                            Friends.getInstance().add(idOfCurrentUser, idTo);
                            out.println(1);
                        } else {
                            out.println(-1);
                        }
                        break;
                    }
                    case "get_list_of_friends": {
                        if (accessOfCurrentUser == 1) {
                            List friends = new ArrayList();
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
                            outStr += "<user><id>" + u.getId() + "</id><name>" + u.getName() +
                                    "</name><lastName>" + u.getLastName() + "</lastName></user>";
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
                                    user.getName() + "</name><surname>" + user.getLastName() + "</surname></message>");
                        }
                        break;
                    }
                    default:
                        out.println(Integer.toString(-10));

                }



            }

        } catch (IOException e) {
            //sdfsdfsdf
        } finally {
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

    public static void main(String[] args) throws IOException {
        ServerSocket s = new ServerSocket(PORT);
        System.out.println("Server Started");

        try {
            while (true) {
                // Блокируется до возникновения нового соединения:
                Socket socket = s.accept();
                try {
                    new ServerOneJabber(socket);
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
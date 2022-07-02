import java.io.*;
import java.net.Socket;

public class OneClient implements Runnable {

    private static Socket clientDialog;

    public OneClient(Socket client) {
        OneClient.clientDialog = client;
    }

    @Override
    public void run() {

        try {
            // инициируем каналы общения в сокете, для сервера
            // канал записи в сокет следует инициализировать сначала канал чтения для избежания блокировки выполнения программы на ожидании заголовка в сокете
            DataOutputStream out = new DataOutputStream(clientDialog.getOutputStream());
            System.out.println("DataOutputStream  created");

            // канал чтения из сокета
            DataInputStream in = new DataInputStream(clientDialog.getInputStream());
            System.out.println("DataInputStream created");

            // начинаем диалог с подключенным клиентом в цикле, пока сокет не
            // закрыт клиентом
            while (!clientDialog.isClosed()) {
                System.out.println("Server reading from channel");

                // серверная нить ждёт в канале чтения (inputstream) получения
                // данных клиента после получения данных считывает их
                String entry = in.readUTF();

                // и выводит в консоль
                System.out.println("READ from clientDialog message - " + entry);

                if (entry.equalsIgnoreCase("image")) { // кто-то скинул нюдесы
                    System.out.println("1");
                    File filePath = new File("Image");
                    filePath.mkdir();

                    FileOutputStream  outFile = new FileOutputStream(filePath + "\\" + in.readUTF());
                    byte[] bytes = new byte[5*1024];

                    System.out.println("1");
                    int count, total=0;
                    long lenght = in.readLong();
                    System.out.println(lenght);
                    while ((count = in.read(bytes)) > -1) {
                        total+=count;
                        System.out.println(total);
                        outFile.write(bytes, 0, count);
                        if (total==lenght) break;
                    }
                    outFile.close();
                }
                // инициализация проверки условия продолжения работы с клиентом
                // по этому сокету по кодовому слову - quit в любом регистре
                if (entry.equalsIgnoreCase("quit")) {
                    System.out.println("Client initialize connections suicide ...");
                    out.writeUTF("Server reply - " + entry + " - OK");
                    break;
                }

                System.out.println("Server try writing to channel");
                out.writeUTF("Server reply - " + entry + " - OK");
                System.out.println("Server Wrote message to clientDialog.");

                // освобождаем буфер сетевых сообщений
                out.flush();
            }

            // если условие выхода - верно выключаем соединения
            System.out.println("Client disconnected");
            System.out.println("Closing connections & channels.");

            // закрываем сначала каналы сокета !
            in.close();
            out.close();

            // потом закрываем сокет общения с клиентом в нити моносервера
            clientDialog.close();

            System.out.println("Closing connections & channels - DONE.");
            System.out.println("__________________________________________________");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
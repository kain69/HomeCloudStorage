import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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
                if (entry.equalsIgnoreCase("quit")) {
                    System.out.println("Client initialize connections suicide ...");
                    out.writeUTF("Server reply - " + entry + " - OK");
                    break;
                }
                if (entry.equalsIgnoreCase("Allimage")){
                    File dir = new File("Image"); //path указывает на директорию
                    out.writeInt(dir.listFiles().length);
                    //System.out.println("" + dir.listFiles().length);
                    for ( File file : dir.listFiles() ){
                        if ( file.isFile() )
                            out.writeUTF("" + file);
                            System.out.println("" + file);
                    }
                }
                if (entry.equalsIgnoreCase("SelectedImage")){
                    int countImage = in.readInt();
                    ArrayList<String> selectedPhotos = new ArrayList<String>();
                    for ( int i = 0; i < countImage; i++ ){
                        selectedPhotos.add(in.readUTF());
                    }
                    for ( int i = 0; i < countImage; i++ ){
                        //Отправка
                        File file = new File(selectedPhotos.get(i));
                        FileInputStream inF = new FileInputStream(file);
                        byte[] bytes = new byte[5*1024];
                        int count;
                        long lenght = file.length();
                        String[] temp = selectedPhotos.get(i).split("/");
                        String fileName = temp[temp.length - 1];
                        out.writeUTF(fileName);
                        out.writeLong(lenght);
                        while ((count = inF.read(bytes)) > -1) {
                            out.write(bytes, 0, count);
                        }
                        out.flush();
                    }
                }

                System.out.println("Server try writing to channel");
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
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        // 命令行参数，id和processNum
        int id = Integer.parseInt(args[0]);
        int processNum = Integer.parseInt(args[1]);

        Process process = new Process(id, processNum);
        Thread sendThread = new Thread(new SendThread(id, process, processNum));
        Thread receiveThread = new Thread(new ReceiveThread(id, process));

        try {
            receiveThread.start();
            Thread.sleep(5000);
            sendThread.start();

            sendThread.join();
            receiveThread.join();

            Status localStatus = process.getLocalStatus();
            System.out.printf("Proc %d status: %s\n", id, localStatus.toString());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

// 发送消息的线程
class SendThread implements Runnable {
    private Integer id;
    private Process process;
    private Integer processNum;

    public SendThread(int id, Process process, int processNum) {
        this.id = id;
        this.process = process;
        this.processNum = processNum;
    }

    @Override
    public void run() {
        try {
            while (true) {
                System.out.printf("Proc %d counter = %d\n", this.id, process.getCounter());
                // 如果是进程0，且状态达到101，发起全局快照
                if (id == 0 && process.getCounter() >= 101 && !process.snapshotRunning()) {
                    process.startSnapshot();
                    while (true) {
                        Thread.sleep(100);
                        if (process.snapshotFinished())
                            break;
                    }
                }

                // 只要没有进行快照，就持续给除自身外的每一个进程发消息
                if (process.snapshotFinished()) break;
                for (int i = 0; i < this.processNum; i++)
                    if (i != this.id)
                        process.send(i, "message" + id);
//                Thread.sleep(10);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class ReceiveThread implements Runnable {
    private Integer id;
    private Process process;

    public ReceiveThread(Integer id, Process process) {
        this.id = id;
        this.process = process;
    }

    @Override
    public void run() {
        try {
            // 建立Socket服务器，监听端口
            ServerSocket server = new ServerSocket(Utils.PORT_INIT + this.id);
            while (true) {
                // 进程做完快照，则结束接收消息
                if (process.snapshotFinished()) break;

                Socket socket = server.accept();
                Reader reader = new InputStreamReader(socket.getInputStream());
                char[] chars = new char[1024];
                int len;
                StringBuilder builder = new StringBuilder();
                while ((len=reader.read(chars)) != -1) {
                    builder.append(new String(chars, 0, len));
                }
                // 由进程解析接收到的消息
                process.receive(builder.toString());
                reader.close();
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

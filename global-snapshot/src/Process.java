import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

// 进程类，包含状态及各进程之间传输的消息
public class Process {
    private Integer id;
    private Integer processNum;

    private Integer counter;  // 接收到消息的次数，即自身状态
    private List<Queue<String>> messages;  // 从各个进程接收到的消息

    private Boolean snapshot;   // 标记是否处于构建快照状态
    private boolean[] receiveMarker;  // 标记已从哪些进程收到marker消息
    private Status localStatus;  // 保存构建快照时，本线程的状态信息

    public Process(int id, int processNum) {
        this.id = id;
        this.processNum = processNum;
        this.counter = 0;
        this.messages = new ArrayList<>();
        for (int i = 0; i < processNum; i++)
            this.messages.add(new LinkedList<>());

        this.snapshot = false;
    }

    // 给dest进程发送msg消息
    public void send(int dest, String msg) throws IOException {
        System.out.printf("Proc %d send a message to Proc %d: %s\n", this.id, dest, msg);
        Utils.sendMsg(id, Utils.PORT_INIT + dest, msg);
    }

    // 解析监听到的消息
    public synchronized void receive(String content) throws IOException {
        String[] parts = content.split(Utils.SEPARATOR);
        int dest = Integer.parseInt(parts[0]);  // 目标进程消息
        String msg = parts[1];  // 对方发来的消息
        System.out.printf("Proc %d receive a message from Proc %d: %s\n", this.id, dest, msg);

        if (msg.contains("marker") && !this.snapshot) { // 识别为标记消息，并且未处于构建快照状态，则进行构建快照初始化工作
            this.snapshot = true;
            this.receiveMarker = new boolean[this.processNum];
            this.localStatus = new Status(this.counter, this.processNum);
            for (int i = 0; i < this.processNum; i++)
                if (i != this.id)
                    this.send(i, msg);
        } else {  // 普通消息，将消息加入目标进程的消息队列
            this.counter++;
            this.messages.get(dest).add(msg);
        }

        if (this.snapshot) {  // 如果处于构建快照状态
            if (msg.contains("marker")) // 如果是标记消息，则比较目标进程的marker已收到
                this.receiveMarker[dest] = true;
            else if (!this.receiveMarker[dest])  // 如果是普通消息，且还未收到目标进程的maker，则将该消息加入消息通道的状态
                this.localStatus.setChannelStatus(dest, msg);
        }
    }

    // 发起快照构建，进行本地快照初始化
    public synchronized void startSnapshot() throws IOException {
        System.out.printf("Proc %d start snapshot\n", this.id);
        this.snapshot = true;
        this.receiveMarker = new boolean[this.processNum];
        this.localStatus = new Status(this.counter, this.processNum);

        for (int i = 0; i < this.processNum; i++)
            if (i != this.id)
                this.send(i, "marker" + this.id);
    }

    public synchronized boolean snapshotRunning() {
        return this.snapshot;
    }

    public synchronized boolean snapshotFinished() {
        if (!this.snapshot) return false;
        for (int i = 0; i < processNum; i++)
            if (i != this.id && !this.receiveMarker[i])
                return false;
        return true;
    }

    public Status getLocalStatus() {
        if (!this.snapshot) return null;

        this.snapshot = false;
        return localStatus;
    }

    public Integer getCounter() {
        return counter;
    }
}

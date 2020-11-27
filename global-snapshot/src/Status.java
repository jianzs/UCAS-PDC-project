import java.util.ArrayList;
import java.util.List;

// 本地快照状态类
public class Status {
    private Integer counter;  // 本地状态
    private List<List<String>> messages; // 本进程的input通道的状态

    public Status(Integer counter, int processNum) {
        this.counter = counter;
        this.messages = new ArrayList<>();
        for (int i = 0; i < processNum; i++)
            this.messages.add(new ArrayList<>());
    }

    public void setChannelStatus(int dest, String msg) {
        this.messages.get(dest).add(msg);
    }

    @Override
    public String toString() {
        return "Status{" +
                "counter=" + counter +
                ", messages=" + messages +
                '}';
    }
}

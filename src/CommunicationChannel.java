import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;

/**
 * Class that implements the channel used by wizards and miners to communicate.
 */
public class CommunicationChannel {
    BlockingQueue<Message> minerQueue = new LinkedBlockingQueue<Message>();
    BlockingQueue<Message> wizardQueue = new LinkedBlockingQueue<Message>();
    Semaphore semaphore = new Semaphore(1);
    LinkedList<String> firstMessage = new LinkedList<String>();

    /**
     * Creates a {@code CommunicationChannel} object.
     */
    public CommunicationChannel() {

    }

    /**
     * Puts a message on the miner channel (i.e., where miners write to and wizards
     * read from).
     *
     * @param message message to be put on the channel
     */
    public void putMessageMinerChannel(Message message) {
        try {
            minerQueue.put(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets a message from the miner channel (i.e., where miners write to and
     * wizards read from).
     *
     * @return message from the miner channel
     */
    public Message getMessageMinerChannel() {
        try {
            return minerQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Puts a message on the wizard channel (i.e., where wizards write to and miners
     * read from).
     *
     * @param message message to be put on the channel
     */
    void putMessageWizardChannel(Message message) {
            try {
                //acquire only if current thread's ID doesn't already exist in list
                String threadName = Thread.currentThread().getName();
                if (!firstMessage.contains(threadName)) {
                    try {
                        semaphore.acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    firstMessage.add(threadName);
                }

                //release and delete current thread's ID
                if (message.getData().equals("END")) {
                    firstMessage.remove(threadName);
                    semaphore.release();
                    return;
                }
                wizardQueue.put(message);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    }

    /**
     * Gets a message from the wizard channel (i.e., where wizards write to and
     * miners read from).
     *
     * @return message from the wizard channel
     */
    public Message getMessageWizardChannel() {
        try {
            return wizardQueue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}

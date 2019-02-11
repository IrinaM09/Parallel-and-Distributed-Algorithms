import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.concurrent.Semaphore;

/**
 * Class for a miner.
 */
public class Miner extends Thread {
    private Integer hashCount;
    static Set<Integer> solved;
    private CommunicationChannel channel;
    static Semaphore semaphore = new Semaphore(1);

    /**
     * @param hashCount number of times that a miner repeats the hash operation when
     *                  solving a puzzle.
     * @param solved    set containing the IDs of the solved rooms
     * @param channel   communication channel between the miners and the wizards
     */
    public Miner(Integer hashCount, Set<Integer> solved, CommunicationChannel channel) {
        this.hashCount = hashCount;
        this.solved = solved;
        this.channel = channel;
    }

    @Override
    public void run() {
        do {
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Message parentRoom = channel.getMessageWizardChannel();

            //Exit from cave
            if (parentRoom.getData().equals("EXIT")) {
                break;
            }

            Message currentRoom = channel.getMessageWizardChannel();
            semaphore.release();

            //Solve current room only if it wasn't already solved
            if (!solved.contains(currentRoom.getCurrentRoom())) {
                synchronized (solved) {
                    solved.add(currentRoom.getCurrentRoom());
                }
                String solution = encryptMultipleTimes(currentRoom.getData(), hashCount);

                if (parentRoom.getData().equals("NO_PARENT")) {
                    channel.putMessageMinerChannel(new Message(-1, currentRoom.getCurrentRoom(), solution));
                } else {
                    channel.putMessageMinerChannel(new Message(parentRoom.getCurrentRoom(), currentRoom.getCurrentRoom(), solution));
                }
            }
        }
        while (true);
    }

    private static String encryptMultipleTimes(String input, Integer count) {
        String hashed = input;
        for (int i = 0; i < count; ++i) {
            hashed = encryptThisString(hashed);
        }
        return hashed;
    }

    private static String encryptThisString(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));

            // convert to string
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String hex = Integer.toHexString(0xff & messageDigest[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}

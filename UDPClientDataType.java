package UDP;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
    
public class UDPClientDataType {
    public static void main(String[] args) {
        String studentCode = "B23DCCN531";
        String qCode = "1ouegsX5";

        DatagramSocket socket = null;

        try {
            socket = new DatagramSocket();

            InetAddress serverAddress = InetAddress.getByName("36.50.135.242");

            int serverPort = 2207;

            String message = ";" + studentCode + ";" + qCode;

            byte[] sendData = message.getBytes(StandardCharsets.UTF_8);

            DatagramPacket sendPacket = new DatagramPacket(
                    sendData,
                    sendData.length,
                    serverAddress,
                    serverPort);

            socket.send(sendPacket);

            System.out.println("Da gui: " + message);

            byte[] receiveData = new byte[4096];

            DatagramPacket receivePacket = new DatagramPacket(
                    receiveData,
                    receiveData.length);

            socket.receive(receivePacket);

            String response = new String(
                    receivePacket.getData(),
                    receivePacket.getOffset(),
                    receivePacket.getLength(),
                    StandardCharsets.UTF_8).trim();

            System.out.println("Server tra ve: " + response);

            String[] parts = response.split(";", -1);

            String requestId = parts[0];
            int n = Integer.parseInt(parts[1]);

            Set<Integer> existingNumbers = new HashSet<>();

            if (parts.length >= 3 && !parts[2].isEmpty()) {
                String[] numbers = parts[2].split(",");

                for (String number : numbers) {
                    existingNumbers.add(
                            Integer.parseInt(number.trim()));
                }
            }

            List<Integer> missingNumbers = new ArrayList<>();

            for (int i = 1; i <= n; i++) {
                if (!existingNumbers.contains(i)) {
                    missingNumbers.add(i);
                }
            }

            StringBuilder result = new StringBuilder(requestId);
            result.append(";");

            for (int i = 0; i < missingNumbers.size(); i++) {
                if (i > 0) {
                    result.append(",");
                }

                result.append(missingNumbers.get(i));
            }

            String answer = result.toString();

            System.out.println("Cac so con thieu: " + missingNumbers);
            System.out.println("Thong diep gui lai: " + answer);

            byte[] answerData = answer.getBytes(StandardCharsets.UTF_8);

            DatagramPacket answerPacket = new DatagramPacket(
                    answerData,
                    answerData.length,
                    receivePacket.getAddress(),
                    receivePacket.getPort());

            socket.send(answerPacket);

            System.out.println("Da gui ket qua len server.");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }

            System.out.println("Da dong socket.");
        }
    }
}

package UDP;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class UDPClientString {
    public static void main(String[] args) {
        String studentCode = "B23DCCN531";
        String qCode = "9PdqBZGQ";

        DatagramSocket socket = null;

        try {
            socket = new DatagramSocket();

            InetAddress serverAddress = InetAddress.getByName("36.50.135.242");
            int serverPort = 2208;

            String message = ";" + studentCode + ";" + qCode;
            byte[] sendData = message.getBytes(StandardCharsets.UTF_8);

            DatagramPacket sendPacket = new DatagramPacket(
                    sendData, sendData.length,
                    serverAddress, serverPort);

            socket.send(sendPacket);
            System.out.println("Da gui: " + message);

            byte[] receiveData = new byte[4096];
            DatagramPacket receivePacket = new DatagramPacket(
                    receiveData, receiveData.length);

            socket.receive(receivePacket);

            String response = new String(
                    receivePacket.getData(),
                    receivePacket.getOffset(),
                    receivePacket.getLength(),
                    StandardCharsets.UTF_8).trim();

            System.out.println("Server tra ve: " + response);

            int separator = response.indexOf(';');

            String requestId = response.substring(0, separator);
            String data = response.substring(separator + 1).trim();

            Map<Character, Integer> frequency = new HashMap<>();

            for (char c : data.toCharArray()) {
                frequency.put(c, frequency.getOrDefault(c, 0) + 1);
            }

            int maxCount = 0;

            for (int count : frequency.values()) {
                if (count > maxCount) {
                    maxCount = count;
                }
            }

            char mostFrequent = '\0';

            for (char c : data.toCharArray()) {
                if (frequency.get(c) == maxCount) {
                    mostFrequent = c;
                    break;
                }
            }

            StringBuilder positions = new StringBuilder();

            for (int i = 0; i < data.length(); i++) {
                if (data.charAt(i) == mostFrequent) {
                    positions.append(i+1).append(",");
                }
            }

            String answer = requestId + ";" +
                    mostFrequent + ":" + positions;

            System.out.println("Ky tu xuat hien nhieu nhat: "
                    + mostFrequent);
            System.out.println("Thong diep gui len server: "
                    + answer);

            byte[] answerData = answer.getBytes(StandardCharsets.UTF_8);

            DatagramPacket answerPacket = new DatagramPacket(
                    answerData,
                    answerData.length,
                    receivePacket.getAddress(),
                    receivePacket.getPort());

            socket.send(answerPacket);

            System.out.println("Da gui ket qua.");

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

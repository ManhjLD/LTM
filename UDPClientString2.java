package UDP;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class UDPClientString2 {
    public static void main(String[] args) {
        String studentCode = "B23DCCN531";
        String qCode = "UMCf9cAS";

        DatagramSocket socket = null;

        try {
            socket = new DatagramSocket();

            InetAddress serverAddress = InetAddress.getByName("36.50.135.242");
            int serverPort = 2208;

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

            String[] parts = response.split(";", 2);

            String requestId = parts[0];
            String data = parts[1];

            StringBuilder normalized = new StringBuilder();
            boolean capitalizeNext = true;

            for (int i = 0; i < data.length(); i++) {
                char c = data.charAt(i);

                if (Character.isWhitespace(c)) {
                    normalized.append(c);
                    capitalizeNext = true;
                } else {
                    if (capitalizeNext) {
                        normalized.append(Character.toUpperCase(c));
                        capitalizeNext = false;
                    } else {
                        normalized.append(Character.toLowerCase(c));
                    }
                }
            }

            String normalizedData = normalized.toString();

            String answer = requestId + ";" + normalizedData;

            System.out.println("Chuoi sau chuan hoa: " + normalizedData);
            System.out.println("Thong diep gui len: " + answer);

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

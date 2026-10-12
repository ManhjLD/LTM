package UDP;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Locale;


public class UDPClientObject {

    public static String normalizeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return name;
        }

        String[] words = name.trim().toLowerCase(Locale.ROOT)
                .split("\\s+");

        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (result.length() > 0) {
                result.append(" ");
            }

            result.append(Character.toUpperCase(word.charAt(0)));
            result.append(word.substring(1));
        }

        return result.toString();
    }

    public static String createEmail(String name) {
        String normalized = name.trim().toLowerCase(Locale.ROOT);
        String[] words = normalized.split("\\s+");

        if (words.length == 0 || normalized.isEmpty()) {
            return "";
        }

        StringBuilder email = new StringBuilder(words[words.length - 1]);

        for (int i = 0; i < words.length - 1; i++) {
            email.append(words[i].charAt(0));
        }

        return email.toString() + "@ptit.edu.vn";
    }

    public static void main(String[] args) {
        String studentCode = "B23DCCN531";
        String qCode = "ccUk3mce";

        DatagramSocket socket = null;

        try {
            socket = new DatagramSocket();

            InetAddress serverAddress = InetAddress.getByName("36.50.135.242");
            int serverPort = 2209;

            String message = ";" + studentCode + ";" + qCode;

            byte[] sendData = message.getBytes(StandardCharsets.UTF_8);

            DatagramPacket sendPacket = new DatagramPacket(
                    sendData,
                    sendData.length,
                    serverAddress,
                    serverPort);
            socket.setSoTimeout(5000);
            socket.send(sendPacket);
            System.out.println("Da gui: " + message);

            byte[] receiveData = new byte[65507];

            DatagramPacket receivePacket = new DatagramPacket(
                    receiveData,
                    receiveData.length);

            socket.receive(receivePacket);

            int packetLength = receivePacket.getLength();

            if (packetLength < 9) {
                throw new IllegalArgumentException(
                        "Goi tin khong chua du requestId va doi tuong");
            }

            byte[] requestIdBytes = new byte[8];

            System.arraycopy(
                    receivePacket.getData(),
                    receivePacket.getOffset(),
                    requestIdBytes,
                    0,
                    8);

            String requestId = new String(
                    requestIdBytes,
                    StandardCharsets.UTF_8);

            System.out.println("Request ID: " + requestId);

            ByteArrayInputStream byteInput = new ByteArrayInputStream(
                    receivePacket.getData(),
                    receivePacket.getOffset() + 8,
                    packetLength - 8);

            ObjectInputStream objectInput = new ObjectInputStream(byteInput);

            Student student = (Student) objectInput.readObject();

            objectInput.close();

            String normalizedName = normalizeName(student.getName());

            student.setName(normalizedName);
            student.setEmail(createEmail(normalizedName));

            System.out.println("Ten sau chuan hoa: "
                    + student.getName());
            System.out.println("Email: " + student.getEmail());

            ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();

            ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);

            objectOutput.writeObject(student);
            objectOutput.flush();

            byte[] objectBytes = byteOutput.toByteArray();
            objectOutput.close();

            byte[] answerData = new byte[8 + objectBytes.length];

            System.arraycopy(
                    requestIdBytes, 0,
                    answerData, 0,
                    8);

            System.arraycopy(
                    objectBytes, 0,
                    answerData, 8,
                    objectBytes.length);

            DatagramPacket answerPacket = new DatagramPacket(
                    answerData,
                    answerData.length,
                    receivePacket.getAddress(),
                    receivePacket.getPort());

            socket.send(answerPacket);

            System.out.println("Da gui Student da xu ly len server.");

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

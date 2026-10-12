package UDP;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UDPClientObject2 {

    public static String normalizeName(String name) {
        String[] words = name.trim().toLowerCase(Locale.ROOT)
                .split("\\s+");

        if (words.length == 0) {
            return name;
        }

        StringBuilder result = new StringBuilder();

        for (int i = 0; i < words.length - 1; i++) {
            if (i > 0) {
                result.append(" ");
            }

            result.append(Character.toUpperCase(words[i].charAt(0)));
            result.append(words[i].substring(1));
        }

        String lastName = words[words.length - 1].toUpperCase(Locale.ROOT);

        return lastName + ", " + result;
    }

    public static String formatDate(String dateString)
            throws ParseException {

        SimpleDateFormat inputFormat = new SimpleDateFormat("MM-dd-yyyy", Locale.US);

        inputFormat.setLenient(false);

        SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);

        Date date = inputFormat.parse(dateString);

        return outputFormat.format(date);
    }

    public static String createUsername(String name) {
        String[] words = name.trim().toLowerCase(Locale.ROOT)
                .split("\\s+");

        StringBuilder username = new StringBuilder();

        for (int i = 0; i < words.length - 1; i++) {
            username.append(words[i].charAt(0));
        }

        username.append(words[words.length - 1]);

        return username.toString();
    }

    public static void main(String[] args) {
        String studentCode = "B23DCCN531";
        String qCode = "LWZQuR0K";

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

            socket.send(sendPacket);

            System.out.println("Da gui: " + message);

            byte[] receiveData = new byte[65507];

            DatagramPacket receivePacket = new DatagramPacket(
                    receiveData,
                    receiveData.length);

            socket.receive(receivePacket);

            int offset = receivePacket.getOffset();
            int length = receivePacket.getLength();

            if (length < 9) {
                throw new IllegalArgumentException(
                        "Goi tin khong hop le");
            }

            byte[] requestIdBytes = new byte[8];

            System.arraycopy(
                    receivePacket.getData(),
                    offset,
                    requestIdBytes,
                    0,
                    8);

            String requestId = new String(
                    requestIdBytes,
                    StandardCharsets.UTF_8);

            System.out.println("Request ID: " + requestId);

            ByteArrayInputStream byteInput = new ByteArrayInputStream(
                    receivePacket.getData(),
                    offset + 8,
                    length - 8);

            ObjectInputStream objectInput = new ObjectInputStream(byteInput);

            Customer customer = (Customer) objectInput.readObject();

            objectInput.close();

            String originalName = customer.getName();

            customer.setUserName(createUsername(originalName));
            customer.setName(normalizeName(originalName));
            customer.setDayOfBirth(formatDate(customer.getDayOfBirth()));

            System.out.println("Customer sau khi xu ly:");
            System.out.println(customer);

            ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();

            ObjectOutputStream objectOutput = new ObjectOutputStream(byteOutput);

            objectOutput.writeObject(customer);
            objectOutput.flush();

            byte[] objectBytes = byteOutput.toByteArray();

            objectOutput.close();

            byte[] answerData = new byte[8 + objectBytes.length];

            System.arraycopy(
                    requestIdBytes,
                    0,
                    answerData,
                    0,
                    8);

            System.arraycopy(
                    objectBytes,
                    0,
                    answerData,
                    8,
                    objectBytes.length);

            DatagramPacket answerPacket = new DatagramPacket(
                    answerData,
                    answerData.length,
                    receivePacket.getAddress(),
                    receivePacket.getPort());

            socket.send(answerPacket);

            System.out.println("Da gui Customer da cap nhat.");

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

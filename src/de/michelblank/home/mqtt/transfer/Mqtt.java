public class Mqtt {
    public Mqtt() {

    }

    public static String generateClientId() {
        // Hostname in clientID eintragen
        try { // Beuge doppelte Belegung vor
            clientId = "FF-Java-" + InetAddress.getLocalHost().getHostName() + (int) (Math.random() * 100);
        } catch (UnknownHostException e1) {
            clientId = "FF-Java-" + (int) (Math.random() * 9999);
        }
    }
}
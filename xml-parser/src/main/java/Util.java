import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class Util {
    public static String getTextValue(Element element, String tagName) throws RuntimeException {
        String textVal = null;
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList.getLength() != 1) {
            throw new RuntimeException("Tag not exist or more than one");
        }
        textVal = nodeList.item(0).getFirstChild().getNodeValue();
        if (textVal == null)
            throw new RuntimeException("Tag value empty or invalid");
        return textVal;
    }

    public static List<String> getTextValues(Element element, String tagName) throws RuntimeException {
        List<String> textVals = new ArrayList<>();
        NodeList nodeList = element.getElementsByTagName(tagName);
        for (int i = 0; i < nodeList.getLength(); i++) {
            String text = nodeList.item(i).getFirstChild().getNodeValue();
            if (text == null)
                throw new RuntimeException("Tag empty");
            textVals.add(text);
        }
        return textVals;
    }

    public static int getIntValue(Element ele, String tagName) throws NumberFormatException {
        return Integer.parseInt(getTextValue(ele, tagName));
    }

    public static String generateId(String seed, int length) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] hash = digest.digest(seed.getBytes());
        BigInteger n = new BigInteger(1, hash);
        String id = n.toString(36); // base 36
        return id.substring(0, length);
    }
}

package com.stardew;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileWriter;

public class Application {
    public static void main(String[] args) {
        new Application(args[0], args[1]);
    }

    public Application(String fromFile, String toFile) {
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

            Document srcDoc = builder.parse(getClass().getClassLoader().getResourceAsStream(fromFile));
            Document destDoc = builder.parse(getClass().getClassLoader().getResourceAsStream(toFile));
            srcDoc.getDocumentElement().normalize();
            destDoc.getDocumentElement().normalize();

            removePlayers(destDoc);
            copyPlayers(srcDoc, destDoc);

            writeToFile(destDoc);
            System.out.println("DONE");
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    /**
     * Removes existing player from destination document to
     * prepare to copy the source character.
     *
     * @param document XML document to remove player Node from.
     */
    public void removePlayers(Document document) {
        NodeList players = document.getElementsByTagName("player");
        for (int i = 0; i < players.getLength(); i++) {
            Node player = players.item(i);
            player.getParentNode().removeChild(player);
        }
    }

    /**
     * Copies player Nodes from srcDoc to destDoc.
     *
     * @param srcDoc  Document to copy player Node from.
     * @param destDoc Document to copy player Node to.
     */
    public void copyPlayers(Document srcDoc, Document destDoc) {
        NodeList players = srcDoc.getElementsByTagName("player");
        for (int i = 0; i < players.getLength(); i++) {
            Node player = players.item(i).cloneNode(true);
            destDoc.getFirstChild().appendChild(destDoc.importNode(player, true));
        }
    }

    /**
     * Saves XML Document to file.
     *
     * @param document Document to save.
     */
    public void writeToFile(Document document) {
        try {
            File outDir = new File("generated");
            outDir.mkdirs();

            DOMSource source = new DOMSource(document);
            FileWriter writer = new FileWriter(new File(outDir, "out"));
            StreamResult result = new StreamResult(writer);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(source, result);
        } catch (Exception err) {
            err.printStackTrace();
        }
    }
}

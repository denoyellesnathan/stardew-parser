package com.stardew;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stardew.parsing.instructions.ParserInstruction;
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
import java.util.Optional;

public class Application {
    public Application(String parserInstructions) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ParserInstruction instruction = mapper.readValue(getClass().getClassLoader().getResourceAsStream(parserInstructions), ParserInstruction.class);

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document srcDoc = builder.parse(getClass().getClassLoader().getResourceAsStream(instruction.getFromFile()));
            Document destDoc = builder.parse(getClass().getClassLoader().getResourceAsStream(instruction.getToFile()));
            srcDoc.getDocumentElement().normalize();
            destDoc.getDocumentElement().normalize();

            if (instruction.isClearPlayers()) {
                removePlayers(destDoc);
            }
            if (instruction.isClearFarmers()) {
                removeFarmhands(destDoc);
            }
            copyPlayer(srcDoc, destDoc, instruction);

////            copyPlayers(srcDoc, destDoc);
////            copyFarmhands(srcDoc, destDoc);
//            playerToFarmhand(srcDoc, destDoc);
//
//            writeToFile(destDoc);
//            System.out.println("DONE");
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Application(args[0]);
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

    private Optional<Node> getUidNode(Node parentNode) {
        NodeList children = parentNode.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                if (child.getNodeName().equals("UniqueMultiplayerID")) {
                    return Optional.of(child);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Copies player Node from srcDoc to destDoc.
     *
     * @param srcDoc  Document to copy player Node from.
     * @param destDoc Document to copy player Node to.
     */
    public void copyPlayer(Document srcDoc, Document destDoc, ParserInstruction instruction) {
        Node srcPlayer = srcDoc.getElementsByTagName("player").item(0);
        Optional<Node> uid = getUidNode(srcPlayer);
        if (uid.isPresent() && uid.get().getTextContent().equals(Long.toString(instruction.getPlayer().getUid()))) {
            if (instruction.getPlayer().getCopyAs().getReplace() == null) {
                removePlayers(destDoc);
                Node player = srcPlayer.cloneNode(true);
                destDoc.getFirstChild().appendChild(destDoc.importNode(player, true));
            } else {
                playerToFarmhand(srcDoc, destDoc);
            }
        }
    }

    public void removeFarmhands(Document document) {
        NodeList farmers = document.getElementsByTagName("Farmer");
        for (int i = 0; i < farmers.getLength(); i++) {
            Node farmer = farmers.item(i);
            farmer.getParentNode().removeChild(farmer);
        }
    }

    public void copyFarmhands(Document srcDoc, Document destDoc) {
        NodeList farmers = srcDoc.getElementsByTagName("Farmer");
        Node farmhands = destDoc.getElementsByTagName("farmhands").item(0);
        for (int i = 0; i < farmers.getLength(); i++) {
            Node farmer = farmers.item(i).cloneNode(true);
            farmhands.appendChild(destDoc.importNode(farmer, true));
        }
    }

    public void playerToFarmhand(Document srcDoc, Document destDoc) {
        Node player = srcDoc.getElementsByTagName("player").item(0);
        Node farmer = destDoc.adoptNode(player);

        Node farmhands = destDoc.getElementsByTagName("farmhands").item(0);
        destDoc.renameNode(farmer, null, "Farmer");
        farmhands.appendChild(farmer);
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

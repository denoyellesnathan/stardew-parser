package com.stardew;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stardew.parsing.instructions.ParserInstruction;
import com.stardew.parsing.instructions.types.CharacterType;
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
import java.util.Scanner;

public class Application {
    public Application() {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("-------------------------------------");
            System.out.println("------------STARDEW PARSER-----------");
            System.out.println("-------------------------------------");
            System.out.println("Enter parser instructions location or press (Enter): ");
            String parserInstructionsLocation = scanner.nextLine();
            if (parserInstructionsLocation.isEmpty()) {
                parserInstructionsLocation = "process.json";
            }

            ObjectMapper mapper = new ObjectMapper();
            ParserInstruction instruction = mapper.readValue(new File(parserInstructionsLocation), ParserInstruction.class);

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document srcDoc = builder.parse(new File(instruction.getFromFile()));
            Document destDoc = builder.parse(new File(instruction.getToFile()));
            srcDoc.getDocumentElement().normalize();
            destDoc.getDocumentElement().normalize();

            if (instruction.isClearPlayers()) {
                removePlayers(destDoc);
            }
            if (instruction.isClearFarmers()) {
                removeFarmhands(destDoc);
            }
            if (instruction.getCharacter().getCharacterType() == CharacterType.PLAYER) {
                copyPlayer(srcDoc, destDoc, instruction);
            } else if (instruction.getCharacter().getCharacterType() == CharacterType.FARMER) {
                copyFarmer(srcDoc, destDoc, instruction);
            }
            writeToFile(destDoc, instruction);

            System.out.println("DONE");
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Application();
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
     * Copies player Node from srcDoc to destDoc.
     *
     * @param srcDoc  Document to copy player Node from.
     * @param destDoc Document to copy player Node to.
     */
    public void copyPlayer(Document srcDoc, Document destDoc, ParserInstruction instruction) {
        Node srcPlayer = srcDoc.getElementsByTagName("player").item(0);
        Optional<Node> uid = getUidNode(srcPlayer);
        if (uid.isPresent() && uid.get().getTextContent().equals(Long.toString(instruction.getCharacter().getUid()))) {
            if (instruction.getCharacter().getCopyAs().getReplace() == null) {
                removePlayers(destDoc);
                Node player = srcPlayer.cloneNode(true);
                destDoc.getFirstChild().appendChild(destDoc.importNode(player, true));
            } else {
                playerToFarmhand(srcDoc, destDoc, instruction);
            }
        }
    }

    /**
     * Copies a farmer Node from srcDoc to destDoc.
     *
     * @param srcDoc  Document to copy farmer Node from.
     * @param destDoc Document to copy farmer Node to.
     */
    public void copyFarmer(Document srcDoc, Document destDoc, ParserInstruction instruction) {
        Node srcFarmhands = srcDoc.getElementsByTagName("farmhands").item(0);
        Optional<Node> srcFarmer = getByUid(srcFarmhands, instruction.getCharacter().getUid().toString());
        if (srcFarmer.isPresent()) {
            Node destFarmhands = destDoc.getElementsByTagName("farmhands").item(0);
            Optional<Node> farmerToReplace = getByUid(destFarmhands, instruction.getCharacter().getCopyAs().getReplace().getUid().toString());
            if (farmerToReplace.isPresent()) {
                Node farmer = destDoc.adoptNode(srcFarmer.get());
                destFarmhands.replaceChild(farmer, farmerToReplace.get());
                replaceUidReferences(srcDoc, destDoc, instruction);
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

    public void playerToFarmhand(Document srcDoc, Document destDoc, ParserInstruction instruction) {
        // Add Player converted to Farmhand to Farmer Node.
        Node player = srcDoc.getElementsByTagName("player").item(0);
        Node farmer = destDoc.adoptNode(player);
        Node farmhands = destDoc.getElementsByTagName("farmhands").item(0);
        destDoc.renameNode(farmer, null, "Farmer");

        // Find Farmhand to replace.
        for (int i = 0; i < farmhands.getChildNodes().getLength(); i++) {
            Node farmhand = farmhands.getChildNodes().item(i);
            getUidNode(farmhand).ifPresent(uid -> {
                if (Long.toString(instruction.getCharacter().getCopyAs().getReplace().getUid()).equals(uid.getTextContent())) {
                    farmhands.replaceChild(farmer, farmhand);
                }
            });
        }

        // Replace UID of any farmhandReference that matches the Replace uid.
        replaceUidReferences(srcDoc, destDoc, instruction);
    }

    public void replaceUidReferences(Document srcDoc, Document destDoc, ParserInstruction instruction) {
        // Replace UID of any farmhandReference that matches the Replace uid.
        NodeList farmhandReference = destDoc.getElementsByTagName("farmhandReference");
        for (int i = 0; i < farmhandReference.getLength(); i++) {
            Node farmhandReferenceNode = farmhandReference.item(i);
            if (farmhandReferenceNode.getTextContent().equals(Long.toString(instruction.getCharacter().getCopyAs().getReplace().getUid()))) {
                farmhandReferenceNode.setTextContent(instruction.getCharacter().getUid().toString());
            }
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

    private Optional<Node> getByUid(Node parentNode, String uid) {
        NodeList children = parentNode.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            Optional<Node> uidNode = getUidNode(child);
            if (uidNode.isPresent()) {
                if (uidNode.get().getTextContent().equals(uid)) {
                    return Optional.of(child);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Saves XML Document to file.
     *
     * @param document Document to save.
     */
    public void writeToFile(Document document, ParserInstruction instruction) {
        try {
            File outDir = new File("generated");
            outDir.mkdirs();

            DOMSource source = new DOMSource(document);
            FileWriter writer = new FileWriter(new File(outDir, new File(instruction.getToFile()).getName()));
            StreamResult result = new StreamResult(writer);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(source, result);
        } catch (Exception err) {
            err.printStackTrace();
        }
    }
}

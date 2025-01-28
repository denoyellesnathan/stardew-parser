package com.stardew;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.gui2.WindowListenerAdapter;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialog;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.MessageDialog;
import com.googlecode.lanterna.gui2.dialogs.MessageDialogButton;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.stardew.gui.FarmerRunnable;
import com.stardew.gui.PasteInputDialog;
import com.stardew.parsing.instructions.Character;
import com.stardew.parsing.instructions.CopyAs;
import com.stardew.parsing.instructions.ParserInstruction;
import com.stardew.parsing.instructions.Replace;
import com.stardew.parsing.instructions.types.CharacterType;

public class Application {

    private static final String UID_NODE = "UniqueMultiplayerID";
    private static final String FARMER_NODE = "Farmer";
    private static final String FARMER_NAME_NODE = "name";

    public Application() {
        try (Screen screen = new DefaultTerminalFactory().setTerminalEmulatorTitle("Stardew Parser " + getPomVersion())
                .createScreen()) {
            screen.startScreen();
            WindowBasedTextGUI gui = new MultiWindowTextGUI(screen);
            mainMenu(gui);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Application();
    }

    public void mainMenu(WindowBasedTextGUI gui) {
        ActionListDialog mainMenu = new ActionListDialogBuilder()
                .setTitle("Stardew Parser: " + getPomVersion())
                .setDescription("Choose an action")
                .setCloseAutomaticallyOnAction(true)
                .addAction("Copy Player -> Player", () -> playerToPlayer(gui))
                .addAction("Copy Player -> Farmer", () -> playerToFarmerGui(gui)).build();
        mainMenu.addWindowListener(new WindowListenerAdapter() {
            @Override
            public void onInput(Window basePane, KeyStroke keyStroke, AtomicBoolean deliverEvent) {
                if (keyStroke.getKeyType() == KeyType.Enter) {
                    basePane.close();
                }
            }
        });
        mainMenu.showDialog(gui);
    }

    public static String getPomVersion() {
        try (FileReader reader = new FileReader("pom.xml")) {
            MavenXpp3Reader mavenReader = new MavenXpp3Reader();
            Model model = mavenReader.read(reader);
            return model.getVersion();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Copies a player Node from srcDoc to destDoc.
     *
     * @param srcDoc  Document to copy player Node from.
     * @param destDoc Document to copy player Node to.
     */
    public void playerToPlayer(WindowBasedTextGUI gui) {
        try {
            String srcPath = new PasteInputDialog("Source save file path.").show(gui)
                    .replaceAll("\"", "").replaceAll("\\\\", "/");
            String destPath = new PasteInputDialog("Destination save file path.").show(gui)
                    .replaceAll("\"", "").replaceAll("\\\\", "/");

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document srcDoc = builder.parse(new File(srcPath));
            Document destDoc = builder.parse(new File(destPath));
            srcDoc.getDocumentElement().normalize();
            destDoc.getDocumentElement().normalize();

            Node player = srcDoc.getElementsByTagName("player").item(0);
            Optional<Node> uidNode = getUidNode(player);
            String uid = "";
            if (uidNode.isPresent()) {
                uid = uidNode.get().getTextContent();
                MessageDialog.showMessageDialog(gui, "UID", "Found Player UID of: " + uid, MessageDialogButton.OK);
            } else {
                uid = new PasteInputDialog("Destination save file path.")
                        .show(gui).replaceAll("\"", "")
                        .replaceAll("\\\\", "/");
            }

            ParserInstruction instruction = ParserInstruction.builder()
                    .fromFile(srcPath)
                    .toFile(destPath)
                    .clearFarmers(false)
                    .clearPlayers(true)
                    .character(Character.builder()
                            .characterType(CharacterType.PLAYER)
                            .uid(Long.parseLong(uid))
                            .copyAs(CopyAs.builder()
                                    .characterType(CharacterType.PLAYER)
                                    .build())
                            .build())
                    .build();

            copyPlayer(srcDoc, destDoc, instruction);
            writeToFile(destDoc, instruction);
            MessageDialog.showMessageDialog(gui, "Complete", "Character migration complete!", MessageDialogButton.OK);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playerToFarmerGui(WindowBasedTextGUI gui) {
        try {
            String srcPath = new PasteInputDialog("Source save file path.").show(gui)
                    .replaceAll("\"", "").replaceAll("\\\\", "/");
            String destPath = new PasteInputDialog("Destination save file path.").show(gui)
                    .replaceAll("\"", "").replaceAll("\\\\", "/");

            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document srcDoc = builder.parse(new File(srcPath));
            Document destDoc = builder.parse(new File(destPath));
            srcDoc.getDocumentElement().normalize();
            destDoc.getDocumentElement().normalize();

            Node player = srcDoc.getElementsByTagName("player").item(0);
            Optional<Node> uidNode = getUidNode(player);
            String uid = "";
            if (uidNode.isPresent()) {
                uid = uidNode.get().getTextContent();
                MessageDialog.showMessageDialog(gui, "UID", "Found Player UID of: " + uid, MessageDialogButton.OK);
            } else {
                uid = new PasteInputDialog("Destination save file path.")
                        .show(gui).replaceAll("\"", "")
                        .replaceAll("\\\\", "/");
            }

            NodeList farmers = destDoc.getElementsByTagName("Farmer");
            if (farmers.getLength() == 0) {
                MessageDialog.showMessageDialog(gui, "Error", "No farmers found in destination save file.",
                        MessageDialogButton.OK);
                return;
            }

            ArrayList<Node> farmerNodes = new ArrayList<>();

            for (int i = 0; i < farmers.getLength(); i++) {
                Node farmer = farmers.item(i);
                Optional<Node> farmerNameNode = getChildNode(farmer, FARMER_NAME_NODE);

                if (farmerNameNode.isPresent() && farmerNameNode.get().getTextContent().isEmpty()) {
                    farmerNodes.add(farmer);
                }
            }

            if (!farmerNodes.isEmpty()) {
                ActionListDialogBuilder farmerSelectionBuilder = new ActionListDialogBuilder()
                        .setTitle("Stardew Parser")
                        .setDescription("Choose a Farmer to replace.")
                        .setCloseAutomaticallyOnAction(true);

                for (Node farmer : farmerNodes) {
                    Optional<Node> farmerUidNode = getUidNode(farmer);
                    String farmerUid = "";
                    if (farmerUidNode.isPresent()) {
                        farmerUid = farmerUidNode.get().getTextContent();
                    }
                    farmerSelectionBuilder.addAction("Farmer UID: " + farmerUid,
                            new FarmerRunnable(Long.parseLong(uid), Long.parseLong(farmerUid)) {
                                @Override
                                public void run() {
                                    ParserInstruction instruction = ParserInstruction.builder()
                                            .fromFile(srcPath)
                                            .toFile(destPath)
                                            .clearFarmers(false)
                                            .clearPlayers(true)
                                            .character(Character.builder()
                                                    .characterType(CharacterType.PLAYER)
                                                    .uid(getPlayerUid())
                                                    .copyAs(CopyAs.builder()
                                                            .characterType(CharacterType.FARMER)
                                                            .replace(Replace.builder()
                                                                    .characterType(CharacterType.FARMER)
                                                                    .uid(getFarmerUid())
                                                                    .build())
                                                            .build())
                                                    .build())
                                            .build();
                                    playerToFarmhand(srcDoc, destDoc, instruction);
                                    writeToFile(destDoc, instruction);
                                    MessageDialog.showMessageDialog(gui, "Complete", "Character migration complete!",
                                            MessageDialogButton.OK);
                                }
                            });
                }
                farmerSelectionBuilder.build().showDialog(gui);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Starts the processing of the application.
     *
     * @param gui GUI to display input dialogs.
     */
    public void startProcessing(WindowBasedTextGUI gui) {
        String test = new PasteInputDialog("Source farm save file path.").show(gui);
        System.out.println("DONE" + test);
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
            Optional<Node> farmerToReplace = getByUid(destFarmhands,
                    instruction.getCharacter().getCopyAs().getReplace().getUid().toString());
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
                if (Long.toString(instruction.getCharacter().getCopyAs().getReplace().getUid())
                        .equals(uid.getTextContent())) {
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
            if (farmhandReferenceNode.getTextContent()
                    .equals(Long.toString(instruction.getCharacter().getCopyAs().getReplace().getUid()))) {
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

    private Optional<Node> getChildNode(Node parentNode, String nodeName) {
        NodeList children = parentNode.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                if (child.getNodeName().equals(nodeName)) {
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

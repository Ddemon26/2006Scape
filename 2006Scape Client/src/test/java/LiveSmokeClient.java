import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Locale;
import javax.imageio.ImageIO;

// Live diagnostic launcher using the production game, renderer, and networking.
public final class LiveSmokeClient extends Game {
    private File commands;
    private File output;
    private long nextStatus;
    private String lastChat;
    private boolean dismissedLoginPanels;

    public static void main(String[] args) throws Exception {
        ClientSettings.ANDROID = false;
        ClientSettings.SERVER_IP = "127.0.0.1";
        ClientSettings.SHOW_NAVBAR = false;
        LiveSmokeClient game = new LiveSmokeClient();
        game.commands = new File(args[0]);
        game.output = new File(args[1]);
        game.output.mkdirs();
        game.myUsername = args[2];
        game.myPassword = "SmokeLocal8";
        Game.nodeID = 10;
        Game.portOff = 0;
        Game.setHighMem();
        Game.isMembers = true;
        Signlink.storeid = 32;
        Signlink.startpriv(InetAddress.getByName("127.0.0.1"));
        game.createClientFrame(503, 765);
    }

    @Override
    void drawLoadingText(int percent, String message) {
        System.out.println("LOAD " + percent + " " + message);
        super.drawLoadingText(percent, message);
    }

    @Override
    void startUp() {
        super.startUp();
        System.out.println("STARTUP loggedIn=" + loggedIn + " loadingError=" + loadingError
                + " genericLoadingError=" + genericLoadingError + " login=" + loginMessage1 + "/" + loginMessage2);
    }

    @Override
    public void processGameLoop() {
        try {
            if (commands.isFile()) {
                String command = new String(Files.readAllBytes(commands.toPath()), StandardCharsets.UTF_8);
                Files.delete(commands.toPath());
                System.out.println("COMMAND " + command);
                if (command.equals("quit")) {
                    System.exit(0);
                } else if (command.startsWith("locale ")) {
                    Locale.setDefault(Locale.forLanguageTag(command.substring(7)));
                    System.out.println("LOCALE " + Locale.getDefault().toLanguageTag());
                } else if (command.startsWith("friend ")) {
                    addFriend(TextClass.longForName(command.substring(7)));
                } else if (command.startsWith("public ")) {
                    messagePromptRaised = false;
                    inputString = "";
                    type(command.substring(7));
                } else if (command.startsWith("private ")) {
                    int separator = command.indexOf('|', 8);
                    aLong953 = TextClass.longForName(command.substring(8, separator));
                    friendsListAction = 3;
                    messagePromptRaised = true;
                    promptInput = "";
                    type(command.substring(separator + 1));
                } else if (command.equals("capture")) {
                    capture();
                } else if (command.equals("continue")) {
                    if (backDialogID != -1) {
                        RSInterface root = RSInterface.interfaceCache[backDialogID];
                        for (int childId : root.children) {
                            RSInterface child = RSInterface.interfaceCache[childId];
                            if (child.atActionType == 6) {
                                menuActionID[0] = 679;
                                menuActionCmd3[0] = childId;
                                doAction(0);
                                System.out.println("CONTINUE dialog=" + backDialogID + " child=" + childId);
                                break;
                            }
                        }
                    }
                }
            }
            super.processGameLoop();
            if (loggedIn && loadingStage == 2 && !dismissedLoginPanels) {
                closeOpenInterfaces();
                dismissedLoginPanels = true;
                System.out.println("Dismissed login panels through closeOpenInterfaces");
            }
            String chat = Arrays.toString(chatTypes) + Arrays.toString(chatNames) + Arrays.toString(chatMessages);
            if (!chat.equals(lastChat)) {
                lastChat = chat;
                for (int i = 0; i < 6; i++) {
                    System.out.println("CHAT " + i + " type=" + chatTypes[i] + " name=" + chatNames[i] + " text=[" + chatMessages[i] + "]");
                }
            }
            if (System.currentTimeMillis() >= nextStatus) {
                nextStatus = System.currentTimeMillis() + 5000;
                String status = "loggedIn=" + loggedIn + " stage=" + loadingStage + " loadingError=" + loadingError
                        + " world=" + baseX + "," + baseY + " players=" + playerCount + " login=" + loginMessage1 + "/" + loginMessage2;
                Files.write(new File(output, "status.txt").toPath(), status.getBytes(StandardCharsets.UTF_8));
                System.out.println("STATUS " + status);
            }
        } catch (Throwable failure) {
            failure.printStackTrace();
            System.exit(2);
        }
    }

    private void type(String text) {
        for (char character : text.toCharArray()) {
            handleKeyPressed(KeyEvent.VK_UNDEFINED, character, false, false);
        }
        handleKeyPressed(KeyEvent.VK_ENTER, '\n', false, false);
    }

    private void capture() throws Exception {
        if (fullGameScreen != null) {
            writeImage("full.png", fullGameScreen, 765, 503);
        }
        if (aRSImageProducer_1165 != null) {
            writeImage("world.png", aRSImageProducer_1165, 512, 334);
        }
        if (aRSImageProducer_1166 != null) {
            writeImage("chat.png", aRSImageProducer_1166, 479, 96);
        }
        Files.write(new File(output, "chat.txt").toPath(), lastChat.getBytes(StandardCharsets.UTF_8));
    }

    private void writeImage(String name, RSImageProducer producer, int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        image.setRGB(0, 0, width, height, producer.anIntArray315, 0, width);
        ImageIO.write(image, "png", new File(output, name));
    }
}

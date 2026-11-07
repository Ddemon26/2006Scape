// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

import javax.swing.*;
import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.TextAttribute;
import java.net.URI;
import java.util.Map;

@SuppressWarnings("serial")
public class RSApplet extends Applet implements Runnable, MouseListener, MouseWheelListener, MouseMotionListener, KeyListener, FocusListener, WindowListener {

	public static boolean ctrlDown = false;
	public static boolean shiftDown = false;
	private int mouseWheelX = 0;
	private int mouseWheelY = 0;
	public static boolean removeShiftDropOnMenuOpen;

	final void createClientFrame(int i, int j) {
		myWidth = j;
		myHeight = i;
		this.setPreferredSize(new Dimension(this.myWidth, this.myHeight));

		gameFrame = new RSFrame(this);
		graphics = getGameComponent().getGraphics();
		fullGameScreen = new RSImageProducer(myWidth, myHeight, getGameComponent());

		if (ClientSettings.SHOW_NAVBAR) {
			try {
				java.awt.Font arial = new java.awt.Font("Arial", java.awt.Font.PLAIN, 11);
				Map attributes = arial.getAttributes();
				attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);

				JLayeredPane layers = new JLayeredPane();
				layers.setPreferredSize(new Dimension(765, 25));

				ImageIcon backgroundImg = new ImageIcon(this.getClass().getResource("navbar.gif"));
				ImageIcon menuImg = new ImageIcon(this.getClass().getResource("navbar_mainmenu.gif"));
				ImageIcon companyImg = new ImageIcon(this.getClass().getResource("navbar_jagex.gif"));
				ImageIcon worldmapImg = new ImageIcon(this.getClass().getResource("navbar_worldmap.gif"));
				ImageIcon manualImg = new ImageIcon(this.getClass().getResource("navbar_manual.gif"));
				ImageIcon rulesImg = new ImageIcon(this.getClass().getResource("navbar_rules.gif"));

				// set up containers
				JLabel background = new JLabel(backgroundImg);
				background.setBounds(0, 0, backgroundImg.getIconWidth(), backgroundImg.getIconHeight());

				JLabel company = new JLabel(companyImg);
				company.setBounds(5, 0, companyImg.getIconWidth(), companyImg.getIconHeight());

				JLabel mainMenu = new JLabel(menuImg);
				mainMenu.setBounds(126, 0, menuImg.getIconWidth(), menuImg.getIconHeight());
				mainMenu.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						try {
							Desktop.getDesktop().browse(URI.create(ClientSettings.NAV_MAINMENU_LINK));
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				});
				JLabel mainMenuText = new JLabel();
				mainMenuText.setForeground(Color.WHITE);
				mainMenuText.setFont(arial.deriveFont(attributes));
				mainMenuText.setBounds(126 + menuImg.getIconWidth() + 4, 0, 75, 25);
				mainMenuText.setText("Main Menu");
				mainMenuText.addMouseListener(mainMenu.getMouseListeners()[0]);
				mainMenuText.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseEntered(MouseEvent e) {
						mainMenuText.setForeground(Color.LIGHT_GRAY);
						mainMenuText.setCursor(new Cursor(Cursor.HAND_CURSOR));
					}

					@Override
					public void mouseExited(MouseEvent e) {
						mainMenuText.setForeground(Color.WHITE);
						mainMenuText.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					}
				});

				JLabel worldSelect = new JLabel(menuImg);
				worldSelect.setBounds(250, 0, menuImg.getIconWidth(), menuImg.getIconHeight());
				worldSelect.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						// TODO: world select popup? redirect to launcher?
					}
				});
				JLabel worldSelectText = new JLabel();
				worldSelectText.setForeground(Color.WHITE);
				worldSelectText.setFont(arial.deriveFont(attributes));
				worldSelectText.setBounds(250 + menuImg.getIconWidth() + 4, 0, 75, 25);
				worldSelectText.setText("World Select");
				worldSelectText.addMouseListener(worldSelect.getMouseListeners()[0]);
				worldSelectText.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseEntered(MouseEvent e) {
						worldSelectText.setForeground(Color.LIGHT_GRAY);
						worldSelectText.setCursor(new Cursor(Cursor.HAND_CURSOR));
					}

					@Override
					public void mouseExited(MouseEvent e) {
						worldSelectText.setForeground(Color.WHITE);
						worldSelectText.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					}
				});

				JLabel worldmap = new JLabel(worldmapImg);
				worldmap.setBounds(387, 0, worldmapImg.getIconWidth(), worldmapImg.getIconHeight());
				worldmap.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						try {
							Desktop.getDesktop().browse(URI.create(ClientSettings.NAV_WORLDMAP_LINK));
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				});
				JLabel worldmapText = new JLabel();
				worldmapText.setForeground(Color.WHITE);
				worldmapText.setFont(arial.deriveFont(attributes));
				worldmapText.setBounds(387 + worldmapImg.getIconWidth() + 4, 0, 75, 25);
				worldmapText.setText("World Map");
				worldmapText.addMouseListener(worldmap.getMouseListeners()[0]);
				worldmapText.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseEntered(MouseEvent e) {
						worldmapText.setForeground(Color.LIGHT_GRAY);
						worldmapText.setCursor(new Cursor(Cursor.HAND_CURSOR));
					}

					@Override
					public void mouseExited(MouseEvent e) {
						worldmapText.setForeground(Color.WHITE);
						worldmapText.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					}
				});

				JLabel manual = new JLabel(manualImg);
				manual.setBounds(520, 0, manualImg.getIconWidth(), manualImg.getIconHeight());
				manual.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						try {
							Desktop.getDesktop().browse(URI.create(ClientSettings.NAV_MANUAL_LINK));
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				});
				JLabel manualText = new JLabel();
				manualText.setForeground(Color.WHITE);
				manualText.setFont(arial.deriveFont(attributes));
				manualText.setBounds(520 + manualImg.getIconWidth() + 4, 0, 50, 25);
				manualText.setText("Manual");
				manualText.addMouseListener(manual.getMouseListeners()[0]);
				manualText.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseEntered(MouseEvent e) {
						manualText.setForeground(Color.LIGHT_GRAY);
						manualText.setCursor(new Cursor(Cursor.HAND_CURSOR));
					}

					@Override
					public void mouseExited(MouseEvent e) {
						manualText.setForeground(Color.WHITE);
						manualText.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					}
				});

				JLabel rules = new JLabel(rulesImg);
				rules.setBounds(636, 0, rulesImg.getIconWidth(), rulesImg.getIconHeight());
				rules.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						try {
							Desktop.getDesktop().browse(URI.create(ClientSettings.NAV_RULES_LINK));
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				});
				JLabel rulesText = new JLabel();
				rulesText.setForeground(Color.WHITE);
				rulesText.setFont(arial.deriveFont(attributes));
				rulesText.setBounds(636 + rulesImg.getIconWidth() + 4, 0, 100, 25);
				rulesText.setText("Rules & Security");
				rulesText.addMouseListener(rules.getMouseListeners()[0]);
				rulesText.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseEntered(MouseEvent e) {
						rulesText.setForeground(Color.LIGHT_GRAY);
						rulesText.setCursor(new Cursor(Cursor.HAND_CURSOR));
					}

					@Override
					public void mouseExited(MouseEvent e) {
						rulesText.setForeground(Color.WHITE);
						rulesText.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
					}
				});

				// layer images
				layers.add(background, 0);
				layers.add(company, 0);
				layers.add(mainMenu, 0);
				layers.add(mainMenuText, 0);
				layers.add(worldSelect, 0);
				layers.add(worldSelectText, 0);
				layers.add(worldmap, 0);
				layers.add(worldmapText, 0);
				layers.add(manual, 0);
				layers.add(manualText, 0);
				layers.add(rules, 0);
				layers.add(rulesText, 0);
				gameFrame.add(layers, BorderLayout.NORTH);
				gameFrame.pack();
				gameFrame.setLocationRelativeTo(null); // re-center based on the new size
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		startRunnable(this, 1);
		this.requestFocus();
	}

	final void initClientFrame(int i, int j) {
		myWidth = j;
		myHeight = i;
		graphics = getGameComponent().getGraphics();
		fullGameScreen = new RSImageProducer(myWidth, myHeight, getGameComponent());
		startRunnable(this, 1);
	}

	@Override
	public void run() {
		getGameComponent().addMouseListener(this);
		getGameComponent().addMouseMotionListener(this);
		getGameComponent().addMouseWheelListener(this);
		getGameComponent().addKeyListener(this);
		getGameComponent().addFocusListener(this);
		if (gameFrame != null) {
			gameFrame.addWindowListener(this);
		}
		drawLoadingText(0, "Loading...");
		startUp();
		int frameSlot = 0;
		int speedRatio = 256;
		int sleepMillis = 1;
		int tickAccumulator = 0;
		int interruptCount = 0;
		for (int slot = 0; slot < 10; slot++) {
			frameTimes[slot] = System.currentTimeMillis();
		}

		System.currentTimeMillis();
		while (runState >= 0) {
			if (runState > 0) {
				runState--;
				if (runState == 0) {
					exit();
					return;
				}
			}
			int i2 = speedRatio;
			int j2 = sleepMillis;
			speedRatio = 300;
			sleepMillis = 1;
			long now = System.currentTimeMillis();
			if (frameTimes[frameSlot] == 0L) {
				speedRatio = i2;
				sleepMillis = j2;
			} else if (now > frameTimes[frameSlot]) {
				speedRatio = (int) (2560 * delayTime / (now - frameTimes[frameSlot]));
			}
			if (speedRatio < 25) {
				speedRatio = 25;
			}
			if (speedRatio > 256) {
				speedRatio = 256;
				sleepMillis = (int) (delayTime - (now - frameTimes[frameSlot]) / 10L);
			}
			if (sleepMillis > delayTime) {
				sleepMillis = delayTime;
			}
			frameTimes[frameSlot] = now;
			frameSlot = (frameSlot + 1) % 10;
			if (sleepMillis > 1) {
				for (int slot = 0; slot < 10; slot++) {
					if (frameTimes[slot] != 0L) {
						frameTimes[slot] += sleepMillis;
					}
				}

			}
			if (sleepMillis < minDelay) {
				sleepMillis = minDelay;
			}
			try {
				Thread.sleep(sleepMillis);
			} catch (InterruptedException _ex) {
				interruptCount++;
			}
			for (; tickAccumulator < 256; tickAccumulator += speedRatio) {
				clickMode3 = clickMode1;
				saveClickX = clickX;
				saveClickY = clickY;
				saveClickTime = clickTime;
				clickMode1 = 0;
				processGameLoop();
				readIndex = writeIndex;
			}

			tickAccumulator &= 0xff;
			if (delayTime > 0) {
				fps = 1000 * speedRatio / (delayTime * 256);
			}
			processDrawing();
			if (shouldDebug) {
				System.out.println("ntime:" + now);
				for (int age = 0; age < 10; age++) {
					int slotIndex = (frameSlot - age - 1 + 20) % 10;
					System.out.println("otim" + slotIndex + ":" + frameTimes[slotIndex]);
				}

				System.out.println("fps:" + fps + " ratio:" + speedRatio + " count:" + tickAccumulator);
				System.out.println("del:" + sleepMillis + " deltime:" + delayTime + " mindel:" + minDelay);
				System.out.println("intex:" + interruptCount + " opos:" + frameSlot);
				shouldDebug = false;
				interruptCount = 0;
			}
		}
		if (runState == -1) {
			exit();
		}
	}

	private void exit() {
		runState = -2;
		cleanUpForQuit();
		if (gameFrame != null) {
			try {
				Thread.sleep(1000L);
			} catch (Exception _ex) {
			}
			try {
				System.exit(0);
			} catch (Throwable _ex) {
			}
		}
	}

	final void setFrameRate(int i) {
		delayTime = 1000 / i;
	}

	@Override
	public final void start() {
		if (runState >= 0) {
			runState = 0;
		}
	}

	@Override
	public final void stop() {
		if (runState >= 0) {
			runState = 4000 / delayTime;
		}
	}

	@Override
	public final void destroy() {
		runState = -1;
		try {
			Thread.sleep(1000L);
		} catch (Exception _ex) {
		}
		if (runState == -1) {
			exit();
		}
	}

	@Override
	public final void update(Graphics g) {
		if (graphics == null) {
			graphics = g;
		}
		shouldClearScreen = true;
		raiseWelcomeScreen();
	}

	@Override
	public final void paint(Graphics g) {
		if (graphics == null) {
			graphics = g;
		}
		shouldClearScreen = true;
		raiseWelcomeScreen();
	}

	public boolean mouseWheelDown;

	@Override
	public final void mousePressed(MouseEvent mouseevent) {
		int x = mouseevent.getX();
		int y = mouseevent.getY();
		idleTime = 0;
		clickX = x;
		clickY = y;
		clickTime = System.currentTimeMillis();
		if (mouseevent.getButton() == MouseEvent.BUTTON2) {
			mouseWheelDown = true;
			mouseWheelX = mouseevent.getX();
			mouseWheelY = mouseevent.getY();
			return;
		}

		if (mouseevent.getButton() == MouseEvent.BUTTON3) {
			clickMode1 = 2;
			clickMode2 = 2;
		} else {
			clickMode1 = 1;
			clickMode2 = 1;
		}

	}

	@Override
	public final void mouseReleased(MouseEvent mouseevent) {
		idleTime = 0;
		clickMode2 = 0;
		mouseWheelDown = false;
	}

	@Override
	public final void mouseClicked(MouseEvent mouseevent) {
	}

	@Override
	public final void mouseEntered(MouseEvent mouseevent) {
	}

	@Override
	public final void mouseExited(MouseEvent mouseevent) {
		idleTime = 0;
		mouseX = -1;
		mouseY = -1;
	}

	public final void mouseDragged(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		if (mouseWheelDown) {
			y = mouseWheelX - e.getX();
			int dragUp = mouseWheelY - e.getY();
			mouseWheelDragged(y, -dragUp);
			mouseWheelX = e.getX();
			mouseWheelY = e.getY();
			return;
		}
		idleTime = 0;
		mouseX = x;
		mouseY = y;
	}
	void mouseWheelDragged(int dragLeft, int dragDown) {

	}

	@Override
	public void mouseMoved(MouseEvent mouseevent) {
		int x = mouseevent.getX();
		int y = mouseevent.getY();
		idleTime = 0;
		mouseX = x;
		mouseY = y;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		handleMouseWheel(e.getWheelRotation(), e.isControlDown());
	}

	public void handleMouseWheel(int notches, boolean isControlDown) {}

	public void handleKeyPressed(int i, int j, boolean isCtrl, boolean isShift) {
		idleTime = 0;
		if (isShift) shiftDown = true;
		if (isCtrl) ctrlDown = true;
		if (j < 30) {
			j = 0;
		}
		if (i == 37) {
			j = 1;
		}
		if (i == 39) {
			j = 2;
		}
		if (i == 38) {
			j = 3;
		}
		if (i == 40) {
			j = 4;
		}
		if (i == 17) {
			j = 5;
		}
		if (i == 8) {
			j = 8;
		}
		if (i == 127) {
			j = 8;
		}
		if (i == 9) {
			j = 9;
		}
		if (i == 10) {
			j = 10;
		}
		if (i >= 112 && i <= 123) {
			j = 1008 + i - 112;
		}
		if (i == 36) {
			j = 1000;
		}
		if (i == 35) {
			j = 1001;
		}
		if (i == 33) {
			j = 1002;
		}
		if (i == 34) {
			j = 1003;
		}
		if (j > 0 && j < 128) {
			keyArray[j] = 1;
		}
		if (j > 4) {
			charQueue[writeIndex] = j;
			writeIndex = writeIndex + 1 & 0x7f;
		}
	}

	@Override
	public void keyPressed(KeyEvent keyevent) {
		handleKeyPressed(keyevent.getKeyCode(), keyevent.getKeyChar(), keyevent.isControlDown(), keyevent.isShiftDown());
	}

	@Override
	public final void keyReleased(KeyEvent keyevent) {
		idleTime = 0;
		int i = keyevent.getKeyCode();
		char c = keyevent.getKeyChar();
		switch (keyevent.getKeyCode())
		{
			case KeyEvent.VK_SHIFT:
				shiftDown = false;
				break;
			case KeyEvent.VK_CONTROL:
				ctrlDown = false;
				break;
		}
		if (c < '\036') {
			c = '\0';
		}
		if (i == 37) {
			c = '\001';
		}
		if (i == 39) {
			c = '\002';
		}
		if (i == 38) {
			c = '\003';
		}
		if (i == 40) {
			c = '\004';
		}
		if (i == 17) {
			c = '\005';
		}
		if (i == 8) {
			c = '\b';
		}
		if (i == 127) {
			c = '\b';
		}
		if (i == 9) {
			c = '\t';
		}
		if (i == 10) {
			c = '\n';
		}
		if (c > 0 && c < '\200') {
			keyArray[c] = 0;
		}
	}

	@Override
	public final void keyTyped(KeyEvent keyevent) {
	}

	final int readChar(int dummy) {
		while (dummy >= 0) {
			for (int j = 1; j > 0; j++) {
				;
			}
		}
		int k = -1;
		if (writeIndex != readIndex) {
			k = charQueue[readIndex];
			readIndex = readIndex + 1 & 0x7f;
		}
		return k;
	}

	@Override
	public final void focusGained(FocusEvent focusevent) {
		awtFocus = true;
		shouldClearScreen = true;
		raiseWelcomeScreen();
	}

	@Override
	public final void focusLost(FocusEvent focusevent) {
		awtFocus = false;
		for (int i = 0; i < 128; i++) {
			keyArray[i] = 0;
		}

	}

	@Override
	public final void windowActivated(WindowEvent windowevent) {
	}

	@Override
	public final void windowClosed(WindowEvent windowevent) {
	}

	@Override
	public final void windowClosing(WindowEvent windowevent) {
		destroy();
	}

	@Override
	public final void windowDeactivated(WindowEvent windowevent) {
	}

	@Override
	public final void windowDeiconified(WindowEvent windowevent) {
	}

	@Override
	public final void windowIconified(WindowEvent windowevent) {
	}

	@Override
	public final void windowOpened(WindowEvent windowevent) {
	}

	void startUp() {
	}

	void processGameLoop() {
	}

	void cleanUpForQuit() {
	}

	void processDrawing() {
	}

	void raiseWelcomeScreen() {
	}

	Component getGameComponent() {
		return this;
	}

	public void startRunnable(Runnable runnable, int priority) {
		Thread thread = new Thread(runnable);
		thread.start();
		thread.setPriority(priority);
	}

	void drawLoadingText(int i, String s) {
		while (graphics == null) {
			graphics = getGameComponent().getGraphics();
			try {
				getGameComponent().repaint();
			} catch (Exception _ex) {
			}
			try {
				Thread.sleep(1000L);
			} catch (Exception _ex) {
			}
		}
		Font font = new Font("Helvetica", 1, 13);
		FontMetrics fontmetrics = getGameComponent().getFontMetrics(font);
		Font font1 = new Font("Helvetica", 0, 13);
		getGameComponent().getFontMetrics(font1);
		if (shouldClearScreen) {
			graphics.setColor(Color.black);
			graphics.fillRect(0, 0, myWidth, myHeight);
			shouldClearScreen = false;
		}
		Color color = new Color(140, 17, 17);
		int j = myHeight / 2 - 18;
		graphics.setColor(color);
		graphics.drawRect(myWidth / 2 - 152, j, 304, 34);
		graphics.fillRect(myWidth / 2 - 150, j + 2, i * 3, 30);
		graphics.setColor(Color.black);
		graphics.fillRect(myWidth / 2 - 150 + i * 3, j + 2, 300 - i * 3, 30);
		graphics.setFont(font);
		graphics.setColor(Color.white);
		graphics.drawString(s, (myWidth - fontmetrics.stringWidth(s)) / 2, j + 22);
	}

	RSApplet() {
		delayTime = 20;
		minDelay = 1;
		frameTimes = new long[10];
		shouldDebug = false;
		shouldClearScreen = true;
		awtFocus = true;
		keyArray = new int[128];
		charQueue = new int[128];
	}

	private int runState;
	private int delayTime;
	int minDelay;
	private final long[] frameTimes;
	int fps;
	boolean shouldDebug;
	int myWidth;
	int myHeight;
	Graphics graphics;
	RSImageProducer fullGameScreen;
	RSFrame gameFrame;
	private boolean shouldClearScreen;
	boolean awtFocus;
	int idleTime;
	int clickMode2;
	public int mouseX;
	public int mouseY;
	private int clickMode1;
	private int clickX;
	private int clickY;
	private long clickTime;
	int clickMode3;
	int saveClickX;
	int saveClickY;
	long saveClickTime;
	final int[] keyArray;
	private final int[] charQueue;
	private int readIndex;
	private int writeIndex;
	public static int anInt34;
}

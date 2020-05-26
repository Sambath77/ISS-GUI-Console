package GUI;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import Generator.ISSAbstract;
import Generator.WeatherStats;
import Generator.WeatherStats.SensorDataN;
import ISS.Driver;


public class Console extends JFrame {

	/** Serial Version UID. */
	private static final long serialVersionUID = -2537044402683960271L;

	/** The width of this the western panel. */
	private static final int MY_PANEL_WIDTH = 350;

	/** The height of the western panel. */
	private static final int MY_PANEL_HEIGHT = 500;

	/** The max number of weather stations this panel can configure. */
	private static final int CONFIG_MAX = 8;

	/** The leftmost panel which holds the graph and wind display. */
	private static JPanel myWestPanel;

	/** The configuration panel for the console application. */
	private static JPanel myConfigPanel;

	/** The main console panel for the console application. */
	private static JPanel myConsolePanel;

	/** The center panel of the gui containing the current data of the sensors .*/
	private static JPanel myDataPanel;

	/** The buttons for this GUI. */
	private static JButton[] myButtons;

	/** The eastern panel which contains buttons. */
	private static JPanel myEastPanel;

	/** The organizer for the main display panels of the application. */
	private static CardLayout myPanelOrg;

	/** The container holding all of the display panels of the application. */
	private static Container myContainer;

	/** Multi-line area that displays plain text. */
	private static JTextArea myTempOut, myBaro, myRainRate, myHumidOut, myTempIn, myHumidIn, myChill, myRainMo, myUV;

	/** The number of weather stations chosen to be created upon configuration. */
	private static int myConfigSize = 0;

	/** Traces the current values of each JComboBox that the user has made on the configuration screen. */
	private static int[] myConfigSelections = new int[5];

	private static ArrayList<ISSAbstract> myWeatherStations = new ArrayList<>();

	private static ArrayList<WeatherStats> myWeatherStats = new ArrayList<>();

	private static ArrayList<String> myStationNames = new ArrayList<>();

	private static String myWindSpeed;

	private static double myWindDirection;

	private static Graph myGraph;

	/** Shows the wind direction and speed. */
	private static WindDisplay myWindDisplay;

	private static int stationDisplayed = 0;

	private static Timer myTimer = new Timer();

	private static TimerTask updateTask;

	private static boolean is2nd;

	private Clock clock;

	private static JPanel clockPanel;

	// data field initialization
	static {
		myWindSpeed = "0";
		myWindDirection = 0;
		is2nd = false;
	}

	// label and myValues initialization.
	static {
		myTempOut = new JTextArea();
		myBaro = new JTextArea();
		myHumidOut = new JTextArea();
		myRainRate = new JTextArea();
		myTempIn = new JTextArea();
		myHumidIn = new JTextArea();
		myChill = new JTextArea();
		myRainMo = new JTextArea();
		myUV = new JTextArea();
	}

	private JPanel dataPanel() {
		myDataPanel.add(myTempOut);
		myDataPanel.add(myTempIn);
		myDataPanel.add(myHumidOut);
		myDataPanel.add(myHumidIn);
		myDataPanel.add(myBaro);
		myDataPanel.add(myRainRate);
		myDataPanel.add(myChill);
		myDataPanel.add(myRainMo);
		myDataPanel.add(myUV);
		return myDataPanel;
	}

	// Panel initialization.
	static {
		myWestPanel = new JPanel();
		myEastPanel = new JPanel();
		clockPanel = new JPanel(new BorderLayout());
		myDataPanel = new JPanel(new GridLayout(3,3));
		myConsolePanel =  new JPanel(new BorderLayout());
		myConfigPanel = new JPanel(new BorderLayout());
		myGraph = new Graph();
		myWindDisplay = new WindDisplay(myWindSpeed, myWindDirection);
	}

	// Button initialization.
	static {
		myButtons = new JButton[12];
		myButtons[0] = new JButton("TEMP / HEAT");
		myButtons[1] = new JButton("2ND / LAMPS");
		myButtons[1].addActionListener(event -> {
			if (is2nd) {
				if (stationDisplayed < myConfigSize - 1) {
					stationDisplayed++;
				} else {
					stationDisplayed = 0;
				}
				is2nd = false;
			} else {
				is2nd = true;
			}
		});
		myButtons[2] = new JButton("HUM / DEW");
		myButtons[3] = new JButton("FORECAST / TIME");
		myButtons[4] = new JButton("WIND / CHILL");
		myButtons[5] = new JButton("GRAPH / UNITS");
		myButtons[6] = new JButton("RAINday / SOLAR");
		myButtons[7] = new JButton("HI/LOW / CLEAR");
		myButtons[8] = new JButton("RAINyr / UV");
		myButtons[9] = new JButton("ALARM / SET");
		myButtons[10] = new JButton("BAR / ET");
		myButtons[11] = new JButton("DONE");
		myButtons[11].addActionListener(event -> {
			System.exit(0);
		});
	}

	public Console() {
		clock = new Clock();
		myPanelOrg = new CardLayout();
		myContainer = getContentPane();
		myContainer.setLayout(myPanelOrg);
		myContainer.add(configurationPanel());
		myContainer.add(consolePanel());

		setPreferredSize(new Dimension(400, 400));
		pack();
		setTitle("Console");
		setLocationRelativeTo(null);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
	}



	private JPanel consolePanel() {
		myWestPanel.setPreferredSize(new Dimension(MY_PANEL_WIDTH, MY_PANEL_HEIGHT));
		myWestPanel.setLayout(new BorderLayout());
		myWestPanel.add(myWindDisplay, BorderLayout.NORTH);
		myWestPanel.add(myGraph, BorderLayout.CENTER);
		clockPanel.add(clock, BorderLayout.NORTH);
		clockPanel.add(dataPanel(), BorderLayout.CENTER);
		myEastPanel.setPreferredSize(new Dimension(300, 500));
		myEastPanel.setLayout(new GridLayout(6, 2));

		for (int i = 0; i < 12; i++) {
			myEastPanel.add(myButtons[i]);
		}

		myConsolePanel.add(myWestPanel, BorderLayout.WEST);
		myConsolePanel.add(clockPanel, BorderLayout.CENTER);
		myConsolePanel.add(myEastPanel, BorderLayout.EAST);

		return myConsolePanel;
	}

	private JPanel configurationPanel() {
		JComboBox<Integer> qtyWS1, qtyWS2, qtyWS3, qtyWS4, qtyWS5;
		JLabel welcome = new JLabel("Configuration Setup");
		welcome.setHorizontalAlignment(SwingConstants.CENTER);
		welcome.setFont(new Font(welcome.getFont().getName(), Font.BOLD, 30));

		// Quantity selections
		qtyWS1 = new JComboBox<>();
		qtyWS1.addActionListener(event -> {
			int value = qtyWS1.getItemAt((int) qtyWS1.getSelectedItem());
			myConfigSelections[0] = value;
			System.out.println(value);
		});
		qtyWS2 = new JComboBox<>();
		qtyWS2.addActionListener(event -> {
			int value = qtyWS2.getItemAt((int) qtyWS2.getSelectedItem());
			myConfigSelections[1] = value;
		});
		qtyWS3 = new JComboBox<>();
		qtyWS3.addActionListener(event -> {
			int value = qtyWS3.getItemAt((int) qtyWS3.getSelectedItem());
			myConfigSelections[2] = value;
		});
		qtyWS4 = new JComboBox<>();
		qtyWS4.addActionListener(event -> {
			int value = qtyWS4.getItemAt((int) qtyWS4.getSelectedItem());
			myConfigSelections[3] = value;
		});
		qtyWS5 = new JComboBox<>();
		qtyWS5.addActionListener(event -> {
			int value = qtyWS5.getItemAt((int) qtyWS5.getSelectedItem());
			myConfigSelections[4] = value;
		});

		for(int x = 0; x <= 8; x++) {
			Integer val = new Integer(x);
			qtyWS1.addItem(val);
			qtyWS2.addItem(val);
			qtyWS3.addItem(val);
			qtyWS4.addItem(val);
			qtyWS5.addItem(val);
		}

		// Weather station labels
		JLabel ws1, ws2, ws3, ws4, ws5;
		ws1 = new JLabel("Weather Station Type 1");
		ws1.setHorizontalAlignment(SwingConstants.CENTER);
		ws2 = new JLabel("Weather Station Type 2");
		ws2.setHorizontalAlignment(SwingConstants.CENTER);
		ws3 = new JLabel("Weather Station Type 3");
		ws3.setHorizontalAlignment(SwingConstants.CENTER);
		ws4 = new JLabel("Weather Station Type 4");
		ws4.setHorizontalAlignment(SwingConstants.CENTER);
		ws5 = new JLabel("Weather Station Type 5");
		ws5.setHorizontalAlignment(SwingConstants.CENTER);

		// Selection panel creation
		JPanel selectionPanel = new JPanel(new GridLayout(5, 2));
		selectionPanel.add(ws1);
		selectionPanel.add(qtyWS1);
		selectionPanel.add(ws2);
		selectionPanel.add(qtyWS2);
		selectionPanel.add(ws3);
		selectionPanel.add(qtyWS3);
		selectionPanel.add(ws4);
		selectionPanel.add(qtyWS4);
		selectionPanel.add(ws5);
		selectionPanel.add(qtyWS5);

		// Just for the create button
		JPanel southPanel = new JPanel();
		JButton createButton = new JButton("Create");

		southPanel.add(createButton);

		myConfigPanel.add(selectionPanel);
		myConfigPanel.add(southPanel, BorderLayout.SOUTH);
		myConfigPanel.add(welcome, BorderLayout.NORTH);

		createButton.addActionListener(event -> {
			if (isValidCreation()) {
				configureWeatherStations();
				myPanelOrg.next(getContentPane());
				setPreferredSize(new Dimension(900,500));
				myTimer.schedule(updateTask, 3000, 2000);
				pack();
				setTitle(myStationNames.get(stationDisplayed));
				revalidate();
			} else {
				JOptionPane.showMessageDialog(new JFrame(),"You must select at least 1 weather station. You can have at most 8 weather stations total.",
						"Invalid Setup", JOptionPane.ERROR_MESSAGE);
			}

		});

		return myConfigPanel;
	}

	private void configureWeatherStations() {
		int stationID = 0;
		for (int type = 0; type < myConfigSelections.length; type++) {

			if (myConfigSelections[type] > 0) {
				switch (type) {
					case 0:	for (int x = 0; x < myConfigSelections[type]; x++) {
								Driver station = new Driver();
								station.setStationID(stationID);
								stationID++;
								myWeatherStats.add(station.getMyWeatherStats());
								station.run();
								myWeatherStations.add(station);
								myStationNames.add("Weather Station Type 1 ID: " + stationID);
							}
							break;
					case 1: break;
					case 2: break;
					case 3: break;
					case 4: break;
				}
			}

		}
	}

	/**
	 * Updates the display on the console based on the weather station that is currently
	 * being displayed.
	 */
	public void updateDisplay() {
		WeatherStats currentStats = myWeatherStats.get(stationDisplayed);
		ISSAbstract currentStation = myWeatherStations.get(stationDisplayed);
		List<SensorDataN> data = currentStats.getByTime((int) currentStation.getLastEntryTime());
		myTempOut.setText(data.get(0).getSensor() + "\n" + data.get(0).getData() +  "\u00B0" + "F");
		myTempIn.setText(data.get(1).getSensor() + "\n" + data.get(1).getData() + " F");
		myHumidOut.setText(data.get(2).getSensor() + "\n" + data.get(2).getData() +"%");
		myHumidIn.setText(data.get(3).getSensor()+ "\n" +  data.get(3).getData() + " %");
		myBaro.setText(data.get(4).getSensor() + "\n" + data.get(4).getData() + "\u00B0" + "in");
		myRainRate.setText(data.get(5).getSensor() + "\n" + data.get(5).getData() +"in/hr");
		myChill.setText(data.get(6).getSensor() + "\n" + data.get(6).getData() +"\u00B0" + "F");
		myWindSpeed = String.valueOf(data.get(7).getData());
		myWindDirection = data.get(8).getData();
		myWindDisplay.setSpeedDirection(myWindSpeed, myWindDirection);
		myRainMo.setText(data.get(9).getSensor() +"\n" +(int) data.get(9).getData());
		myUV.setText(data.get(10).getSensor() +"\n" + (int) data.get(10).getData());
		pack();
		setTitle(myStationNames.get(stationDisplayed));
		revalidate();

	}

	private boolean isValidCreation() {
		boolean answer = false;
		for (int x = 0; x < myConfigSelections.length; x++) {
			myConfigSize += myConfigSelections[x];
		}
		if (myConfigSize > 0 && myConfigSize <= CONFIG_MAX) {
			answer = true;
		}
		return answer;
	}

	public static void main(final String[] theArgs) {
		Console test = new Console();
		updateTask = new TimerTask() {

			@Override
			public void run() {
				test.updateDisplay();
			}
		};
	}
}

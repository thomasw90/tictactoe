import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.StringTokenizer;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;


public class Main extends JFrame {
	
	class receiveFromServer implements Runnable{
		@Override
		public void run() {
			try {
				System.out.println("Warte auf Server..");
				String string = in.readUTF();		
				stringToArray(string);
				
				if(spielerfarbe==1){
					if(Integer.parseInt(restRot.getText()) > 0){
						restGelb.setText(Integer.toString(Integer.parseInt(restGelb.getText())-1));
					} else {
					setzen=false;
					ziehen=true;
					}
				}
				
				if(spielerfarbe==2){
					if(Integer.parseInt(restGelb.getText()) > 0){
						restRot.setText(Integer.toString(Integer.parseInt(restRot.getText())-1));
					} else {
					setzen=false;
					ziehen=true;
					}
				}
				
				if(ziehen){
					restGelb.setText("0");
					restRot.setText("0");
				}
				
				anzahlAnzeigen();
				ichBinDran = true;
				laufschrift.setText("du bist dran");	
				checkSieger();
			} catch (IOException e) {
				System.out.println("Fehler beim Empfangen von Server");
				e.printStackTrace();
			}			
		}
	}
	
	class sendToServer implements Runnable {
		@Override
		public void run() {
			try {
				laufschrift.setText("Mitspieler ist dran");
				checkSieger();
				out.writeUTF(arrayToString());
				System.out.println("Erfolgreich gesendet: " + arrayToString());
			} catch (IOException e) {
				System.out.println("Fehler beim Senden zu Server");
				e.printStackTrace();
			}			
		}
	}
	

	private JLayeredPane contentPane;	
	private JLabel background;
	private JButton[] buttonArray;
	private int[] buttonStatus;
	private JLabel figurenAufFeld;
	private JLabel zuSetzen;
	private JLabel bildRot;
	private JLabel bildGelb;
	private JLabel feldRot;
	private JLabel feldGelb;
	private JLabel restRot;
	private JLabel restGelb;
	private JLabel laufschrift;
	private int spielerfarbe=2; //rot=1, gelb=2
	private int buttonAuswahl;
		
	private ImageIcon imgBtnGelb = new ImageIcon(getClass().getResource(
			"gelb.png"));
	private ImageIcon imgBtnRot = new ImageIcon(getClass().getResource(
			"rot.png"));
	private ImageIcon imgBtnKlar = new ImageIcon(getClass().getResource(
			"klar.png"));
	private ImageIcon imgBtnWeiss = new ImageIcon(getClass().getResource(
			"weiss.png"));
	private ImageIcon imgBackground = new ImageIcon(getClass().getResource(
			"brett.png"));
	
	private boolean ichBinDran=true;
	private boolean setzen=true;
	private boolean ziehen=false;
	private boolean loeschen=false;
	
	private Socket socket;	
	private DataInputStream in;
	private DataOutputStream out;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {

					Main frame = new Main();
					final Toolkit toolkit = Toolkit.getDefaultToolkit();
					final Dimension screenSize = toolkit.getScreenSize();
					final int x = (screenSize.width - frame.getWidth()) / 2;
					final int y = (screenSize.height - frame.getHeight()) / 2;
					frame.setLocation(x, y);					
					frame.setVisible(true);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Main() {
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 656, 560);
		contentPane = new JLayeredPane();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		background = new JLabel(imgBackground);
		background.setBackground(Color.BLACK);
		background.setBounds(0, 0, 650, 500);
		contentPane.add(background);
		

		
		buttonStatus = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,	0, 0, 0, 0, 0, 0, 0, 0, 0 }; // rot = 1, gelb = 2
		
		buttonsErstellen();
		layoutErstellen();
		
		connectToServer();
		setupStreams();
		setupGame();

	}
	
	public void layoutErstellen(){
		
		bildRot = new JLabel(imgBtnRot);
		bildRot.setBounds(510, 100, 50, 50);
		contentPane.add(bildRot, 0);		
		bildGelb = new JLabel(imgBtnGelb);
		bildGelb.setBounds(590, 100, 50, 50);
		contentPane.add(bildGelb, 0);
		
		figurenAufFeld = new JLabel("Figuren auf Feld");
		figurenAufFeld.setHorizontalAlignment(SwingConstants.CENTER);
		figurenAufFeld.setForeground(Color.WHITE);
		figurenAufFeld.setFont(new Font("Arial", Font.PLAIN, 16));
		figurenAufFeld.setBounds(490, 165, 169, 20);
		contentPane.add(figurenAufFeld, 0);
		
		zuSetzen = new JLabel("zu setzen");
		zuSetzen.setHorizontalAlignment(SwingConstants.CENTER);
		zuSetzen.setForeground(Color.WHITE);
		zuSetzen.setFont(new Font("Arial", Font.PLAIN, 16));
		zuSetzen.setBounds(490, 230, 169, 20);
		contentPane.add(zuSetzen, 0);
		
		feldRot = new JLabel("0");
		feldRot.setHorizontalAlignment(SwingConstants.CENTER);
		feldRot.setForeground(Color.WHITE);
		feldRot.setFont(new Font("Arial", Font.PLAIN, 25));
		feldRot.setBounds(452, 190, 169, 20);
		contentPane.add(feldRot, 0);
		
		feldGelb = new JLabel("0");
		feldGelb.setHorizontalAlignment(SwingConstants.CENTER);
		feldGelb.setForeground(Color.WHITE);
		feldGelb.setFont(new Font("Arial", Font.PLAIN, 25));
		feldGelb.setBounds(535, 190, 169, 20);
		contentPane.add(feldGelb, 0);
		
		restRot = new JLabel("9");
		restRot.setHorizontalAlignment(SwingConstants.CENTER);
		restRot.setForeground(Color.WHITE);
		restRot.setFont(new Font("Arial", Font.PLAIN, 25));
		restRot.setBounds(452, 255, 169, 20);
		contentPane.add(restRot, 0);
		
		restGelb = new JLabel("9");
		restGelb.setHorizontalAlignment(SwingConstants.CENTER);
		restGelb.setForeground(Color.WHITE);
		restGelb.setFont(new Font("Arial", Font.PLAIN, 25));
		restGelb.setBounds(535, 255, 169, 20);
		contentPane.add(restGelb, 0);
		
		laufschrift = new JLabel("warte auf anderen Spieler");
		laufschrift.setHorizontalAlignment(SwingConstants.LEFT);
		laufschrift.setForeground(Color.BLACK);
		laufschrift.setFont(new Font("Arial", Font.PLAIN, 18));
		laufschrift.setBounds(8, 505, 500, 20);
		contentPane.add(laufschrift, 0);
		
	}
	
	//Erstellt die Spielfiguren und legt Größe und Position fest
	public void buttonsErstellen(){
		
		buttonArray = new JButton[24];
		
		for (int i = 0; i < buttonArray.length; i++) {
			final int j=i;
			
			JButton button = new JButton(imgBtnKlar);
			button.setOpaque(false);
			button.setContentAreaFilled(false);
			button.setBorderPainted(false);		
			
			button.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent arg0) {

				}

				@Override
				// Listener für mouseExited bei Modus setzen
				public void mouseExited(MouseEvent e) {

				}
			});
			
			
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					if(ichBinDran){
						//Setze Spielfigur an Position j
						if(setzen && buttonStatus[j]==0){
							if(spielerfarbe==1){
								button.setIcon(imgBtnRot);
								buttonStatus[j]=1;
								restRot.setText(Integer.toString(Integer.parseInt(restRot.getText())-1));
							} else {
								button.setIcon(imgBtnGelb);
								buttonStatus[j]=2;
								restGelb.setText(Integer.toString(Integer.parseInt(restGelb.getText())-1));
							}
							anzahlAnzeigen();
							checkMuehle(j);
						}
						//Zeige mögliche Zugpositionen an
						else if(ziehen && buttonStatus[j]==spielerfarbe){
							zugAnzeigen(j);
							buttonAuswahl=j;
						}
						//Verschiebe Spielfigur an neue Position
						if(ziehen && button.getIcon().equals(imgBtnWeiss)){
							if(spielerfarbe==1){
								button.setIcon(imgBtnRot);
								buttonStatus[j]=1;	
							} else {
								button.setIcon(imgBtnGelb);
								buttonStatus[j]=2;
							}
							buttonStatus[buttonAuswahl]=0; //Entferne alte Buttonposition
							buttonAusblenden(buttonAuswahl);
							spielfeldRefresh();
							ziehen=false;
							checkMuehle(j);							
						}
						//Lösche Spielfigur des Gegners
						if(loeschen){
							if((spielerfarbe==1 && button.getIcon().equals(imgBtnGelb)) || (spielerfarbe==2 && button.getIcon().equals(imgBtnRot))){
								button.setIcon(imgBtnKlar);
								buttonStatus[j]=0;
								loeschen=false;
								setzen=true;
								ichBinDran=false;
								laufschrift.setText("Mitspieler ist dran");
								anzahlAnzeigen();
								Thread t = new Thread(new sendToServer());
								t.start();
								Thread s = new Thread(new receiveFromServer());
								s.start();
							}
						}
					}
				}	
			});				
			
			buttonArray[i] = button;			
			contentPane.add(buttonArray[i], 0);
		}		

		buttonArray[0].setBounds(19, 16, 50, 50);
		buttonArray[1].setBounds(229, 16, 50, 50);
		buttonArray[2].setBounds(442, 16, 50, 50);
		buttonArray[3].setBounds(80, 77, 50, 50);
		buttonArray[4].setBounds(229, 77, 50, 50);
		buttonArray[5].setBounds(381, 77, 50, 50);
		buttonArray[6].setBounds(141, 138, 50, 50);
		buttonArray[7].setBounds(229, 138, 50, 50);
		buttonArray[8].setBounds(320, 138, 50, 50);
		buttonArray[9].setBounds(19, 228, 50, 50);
		buttonArray[10].setBounds(80, 228, 50, 50);
		buttonArray[11].setBounds(141, 228, 50, 50);
		buttonArray[12].setBounds(320, 228, 50, 50);
		buttonArray[13].setBounds(381, 228, 50, 50);
		buttonArray[14].setBounds(442, 228, 50, 50);
		buttonArray[15].setBounds(141, 317, 50, 50);
		buttonArray[16].setBounds(229, 317, 50, 50);
		buttonArray[17].setBounds(320, 317, 50, 50);
		buttonArray[18].setBounds(80, 378, 50, 50);
		buttonArray[19].setBounds(229, 378, 50, 50);
		buttonArray[20].setBounds(381, 378, 50, 50);
		buttonArray[21].setBounds(19, 439, 50, 50);
		buttonArray[22].setBounds(229, 439, 50, 50);
		buttonArray[23].setBounds(442, 439, 50, 50);
		
	}
	
	private void spielfeldRefresh(){
		for (int i = 0; i < buttonStatus.length; i++) {
			if(buttonStatus[i]==0){
				buttonArray[i].setIcon(imgBtnKlar);
			} else if(buttonStatus[i]==1){
				buttonArray[i].setIcon(imgBtnRot);
			} else if(buttonStatus[i]==2){
				buttonArray[i].setIcon(imgBtnGelb);
			}
		}
	}
	
	private void checkSieger(){
		if(spielerfarbe==1 && Integer.parseInt(feldGelb.getText())==2 && Integer.parseInt(restGelb.getText())==0){
			laufschrift.setText("Gewonnen!");
			ichBinDran=false;
		}
		if(spielerfarbe==1 && Integer.parseInt(feldRot.getText())==2 && Integer.parseInt(restRot.getText())==0){
			laufschrift.setText("Verloren!");
			ichBinDran=false;
		}
		if(spielerfarbe==2 && Integer.parseInt(feldGelb.getText())==2 && Integer.parseInt(restGelb.getText())==0){
			laufschrift.setText("Verloren!");
			ichBinDran=false;
		}
		if(spielerfarbe==2 && Integer.parseInt(feldRot.getText())==2 && Integer.parseInt(restRot.getText())==0){
			laufschrift.setText("Gewonnen!");
			ichBinDran=false;
		}
	}
	
	private void anzahlAnzeigen(){
		
		int i=0;
		int j=0;
		
		for (int k = 0; k < buttonStatus.length; k++) {
			if(buttonStatus[k]==1){
				i++;
			}
			if(buttonStatus[k]==2){
				j++;
			}
		}
	
		
		
		feldRot.setText(Integer.toString(i));
		feldGelb.setText(Integer.toString(j));
		
	}
	
	private void buttonAusblenden(int i){
		buttonArray[i].setIcon(imgBtnKlar);
	}
			
	private void zugAnzeigen(int i){
		
		spielfeldRefresh();
		
		//Nur 3 Spielfiguren übrig
		if((spielerfarbe==1 && Integer.parseInt(feldRot.getText())==3 && Integer.parseInt(restRot.getText()) == 0) || (spielerfarbe==2 && Integer.parseInt(feldGelb.getText())==3 && Integer.parseInt(restGelb.getText()) == 0)){
			for (int j = 0; j < buttonStatus.length; j++) {
				if(buttonStatus[j]==0){
					buttonArray[j].setIcon(imgBtnWeiss);
				}
			}
		}
				
		switch(i){
		case 0:
			if(buttonArray[9].getIcon().equals(imgBtnKlar)){
				buttonArray[9].setIcon(imgBtnWeiss);
			}
			if(buttonArray[1].getIcon().equals(imgBtnKlar)){
				buttonArray[1].setIcon(imgBtnWeiss);
			}
			break;
		case 1:
			if(buttonArray[0].getIcon().equals(imgBtnKlar)){
				buttonArray[0].setIcon(imgBtnWeiss);
			}
			if(buttonArray[2].getIcon().equals(imgBtnKlar)){
				buttonArray[2].setIcon(imgBtnWeiss);
			}
			if(buttonArray[4].getIcon().equals(imgBtnKlar)){
				buttonArray[4].setIcon(imgBtnWeiss);
			}
			break;
		case 2:
			if(buttonArray[1].getIcon().equals(imgBtnKlar)){
				buttonArray[1].setIcon(imgBtnWeiss);
			}
			if(buttonArray[14].getIcon().equals(imgBtnKlar)){
				buttonArray[14].setIcon(imgBtnWeiss);
			}
			break;
		case 3:
			if(buttonArray[4].getIcon().equals(imgBtnKlar)){
				buttonArray[4].setIcon(imgBtnWeiss);
			}
			if(buttonArray[10].getIcon().equals(imgBtnKlar)){
				buttonArray[10].setIcon(imgBtnWeiss);
			}
			break;
		case 4:
			if(buttonArray[1].getIcon().equals(imgBtnKlar)){
				buttonArray[1].setIcon(imgBtnWeiss);
			}
			if(buttonArray[3].getIcon().equals(imgBtnKlar)){
				buttonArray[3].setIcon(imgBtnWeiss);
			}
			if(buttonArray[5].getIcon().equals(imgBtnKlar)){
				buttonArray[5].setIcon(imgBtnWeiss);
			}
			if(buttonArray[7].getIcon().equals(imgBtnKlar)){
				buttonArray[7].setIcon(imgBtnWeiss);
			}
			break;
		case 5:
			if(buttonArray[4].getIcon().equals(imgBtnKlar)){
				buttonArray[4].setIcon(imgBtnWeiss);
			}
			if(buttonArray[13].getIcon().equals(imgBtnKlar)){
				buttonArray[13].setIcon(imgBtnWeiss);
			}
			break;
		case 6:
			if(buttonArray[7].getIcon().equals(imgBtnKlar)){
				buttonArray[7].setIcon(imgBtnWeiss);
			}
			if(buttonArray[11].getIcon().equals(imgBtnKlar)){
				buttonArray[11].setIcon(imgBtnWeiss);
			}
			break;
		case 7:
			if(buttonArray[8].getIcon().equals(imgBtnKlar)){
				buttonArray[8].setIcon(imgBtnWeiss);
			}
			if(buttonArray[6].getIcon().equals(imgBtnKlar)){
				buttonArray[6].setIcon(imgBtnWeiss);
			}
			if(buttonArray[4].getIcon().equals(imgBtnKlar)){
				buttonArray[4].setIcon(imgBtnWeiss);
			}
			break;
		case 8:
			if(buttonArray[7].getIcon().equals(imgBtnKlar)){
				buttonArray[7].setIcon(imgBtnWeiss);
			}
			if(buttonArray[12].getIcon().equals(imgBtnKlar)){
				buttonArray[12].setIcon(imgBtnWeiss);
			}
			break;
		case 9:
			if(buttonArray[0].getIcon().equals(imgBtnKlar)){
				buttonArray[0].setIcon(imgBtnWeiss);
			}
			if(buttonArray[10].getIcon().equals(imgBtnKlar)){
				buttonArray[10].setIcon(imgBtnWeiss);
			}
			if(buttonArray[21].getIcon().equals(imgBtnKlar)){
				buttonArray[21].setIcon(imgBtnWeiss);
			}
			break;
		case 10:
			if(buttonArray[3].getIcon().equals(imgBtnKlar)){
				buttonArray[3].setIcon(imgBtnWeiss);
			}
			if(buttonArray[9].getIcon().equals(imgBtnKlar)){
				buttonArray[9].setIcon(imgBtnWeiss);
			}
			if(buttonArray[11].getIcon().equals(imgBtnKlar)){
				buttonArray[11].setIcon(imgBtnWeiss);
			}
			if(buttonArray[18].getIcon().equals(imgBtnKlar)){
				buttonArray[18].setIcon(imgBtnWeiss);
			}
			break;
		case 11:
			if(buttonArray[6].getIcon().equals(imgBtnKlar)){
				buttonArray[6].setIcon(imgBtnWeiss);
			}
			if(buttonArray[10].getIcon().equals(imgBtnKlar)){
				buttonArray[10].setIcon(imgBtnWeiss);
			}
			if(buttonArray[15].getIcon().equals(imgBtnKlar)){
				buttonArray[15].setIcon(imgBtnWeiss);
			}
			break;
		case 12:
			if(buttonArray[8].getIcon().equals(imgBtnKlar)){
				buttonArray[8].setIcon(imgBtnWeiss);
			}
			if(buttonArray[13].getIcon().equals(imgBtnKlar)){
				buttonArray[13].setIcon(imgBtnWeiss);
			}
			if(buttonArray[17].getIcon().equals(imgBtnKlar)){
				buttonArray[17].setIcon(imgBtnWeiss);
			}
			break;
		case 13:
			if(buttonArray[5].getIcon().equals(imgBtnKlar)){
				buttonArray[5].setIcon(imgBtnWeiss);
			}
			if(buttonArray[12].getIcon().equals(imgBtnKlar)){
				buttonArray[12].setIcon(imgBtnWeiss);
			}
			if(buttonArray[14].getIcon().equals(imgBtnKlar)){
				buttonArray[14].setIcon(imgBtnWeiss);
			}
			if(buttonArray[20].getIcon().equals(imgBtnKlar)){
				buttonArray[20].setIcon(imgBtnWeiss);
			}
			break;
		case 14:
			if(buttonArray[2].getIcon().equals(imgBtnKlar)){
				buttonArray[2].setIcon(imgBtnWeiss);
			}
			if(buttonArray[13].getIcon().equals(imgBtnKlar)){
				buttonArray[13].setIcon(imgBtnWeiss);
			}
			if(buttonArray[23].getIcon().equals(imgBtnKlar)){
				buttonArray[23].setIcon(imgBtnWeiss);
			}
			break;
		case 15:
			if(buttonArray[11].getIcon().equals(imgBtnKlar)){
				buttonArray[11].setIcon(imgBtnWeiss);
			}
			if(buttonArray[16].getIcon().equals(imgBtnKlar)){
				buttonArray[16].setIcon(imgBtnWeiss);
			}
			break;
		case 16:
			if(buttonArray[15].getIcon().equals(imgBtnKlar)){
				buttonArray[15].setIcon(imgBtnWeiss);
			}
			if(buttonArray[17].getIcon().equals(imgBtnKlar)){
				buttonArray[17].setIcon(imgBtnWeiss);
			}
			if(buttonArray[19].getIcon().equals(imgBtnKlar)){
				buttonArray[19].setIcon(imgBtnWeiss);
			}
			break;
		case 17:
			if(buttonArray[12].getIcon().equals(imgBtnKlar)){
				buttonArray[12].setIcon(imgBtnWeiss);
			}
			if(buttonArray[16].getIcon().equals(imgBtnKlar)){
				buttonArray[16].setIcon(imgBtnWeiss);
			}
			break;
		case 18:
			if(buttonArray[10].getIcon().equals(imgBtnKlar)){
				buttonArray[10].setIcon(imgBtnWeiss);
			}
			if(buttonArray[19].getIcon().equals(imgBtnKlar)){
				buttonArray[19].setIcon(imgBtnWeiss);
			}
			break;
		case 19:
			if(buttonArray[16].getIcon().equals(imgBtnKlar)){
				buttonArray[16].setIcon(imgBtnWeiss);
			}
			if(buttonArray[18].getIcon().equals(imgBtnKlar)){
				buttonArray[18].setIcon(imgBtnWeiss);
			}
			if(buttonArray[20].getIcon().equals(imgBtnKlar)){
				buttonArray[20].setIcon(imgBtnWeiss);
			}
			if(buttonArray[22].getIcon().equals(imgBtnKlar)){
				buttonArray[22].setIcon(imgBtnWeiss);
			}
			break;
		case 20:
			if(buttonArray[13].getIcon().equals(imgBtnKlar)){
				buttonArray[13].setIcon(imgBtnWeiss);
			}
			if(buttonArray[19].getIcon().equals(imgBtnKlar)){
				buttonArray[19].setIcon(imgBtnWeiss);
			}
			break;
		case 21:
			if(buttonArray[9].getIcon().equals(imgBtnKlar)){
				buttonArray[9].setIcon(imgBtnWeiss);
			}
			if(buttonArray[22].getIcon().equals(imgBtnKlar)){
				buttonArray[22].setIcon(imgBtnWeiss);
			}
			break;
		case 22:
			if(buttonArray[21].getIcon().equals(imgBtnKlar)){
				buttonArray[21].setIcon(imgBtnWeiss);
			}
			if(buttonArray[23].getIcon().equals(imgBtnKlar)){
				buttonArray[23].setIcon(imgBtnWeiss);
			}
			if(buttonArray[19].getIcon().equals(imgBtnKlar)){
				buttonArray[19].setIcon(imgBtnWeiss);
			}
			break;
		case 23:
			if(buttonArray[14].getIcon().equals(imgBtnKlar)){
				buttonArray[14].setIcon(imgBtnWeiss);
			}
			if(buttonArray[22].getIcon().equals(imgBtnKlar)){
				buttonArray[22].setIcon(imgBtnWeiss);
			}
			break;
		}
		
	}
	
	private void checkMuehle(int i){
		switch (i) {
		case 0:
			if (((buttonStatus[0] != 0) && (((buttonStatus[0] == buttonStatus[9]) && (buttonStatus[9] == buttonStatus[21]))
					|| ((buttonStatus[0] == buttonStatus[1]) && (buttonStatus[1] == buttonStatus[2]))))) {
				loeschen = true;
				setzen = false;
				
			}
			break;
		case 1:
			if (((buttonStatus[1] != 0) && (((buttonStatus[0] == buttonStatus[1]) && (buttonStatus[1] == buttonStatus[2]))
					|| ((buttonStatus[1] == buttonStatus[4]) && (buttonStatus[4] == buttonStatus[7]))))) {
				loeschen = true;
				setzen = false;
			}
			break;
		case 2:
			if (((buttonStatus[2] != 0) && (((buttonStatus[0] == buttonStatus[1]) && (buttonStatus[1] == buttonStatus[2]))
					|| ((buttonStatus[2] == buttonStatus[14]) && (buttonStatus[14] == buttonStatus[23]))))) {
				loeschen = true;
				setzen = false;
			}
			break;
		case 3:
			if (((buttonStatus[3] != 0) && (((buttonStatus[3] == buttonStatus[4]) && (buttonStatus[4] == buttonStatus[5]))
					|| ((buttonStatus[3] == buttonStatus[10]) && (buttonStatus[10] == buttonStatus[18]))))) {
				loeschen = true;
				setzen = false;
			}
			break;
		case 4:
			if (((buttonStatus[4] != 0) && (((buttonStatus[1] == buttonStatus[4]) && (buttonStatus[4] == buttonStatus[7]))
					|| ((buttonStatus[3] == buttonStatus[4]) && (buttonStatus[4] == buttonStatus[5]))))) {
				loeschen = true;
				setzen = false;
			}
			break;
		case 5:
			if (((buttonStatus[5] != 0) && (((buttonStatus[3] == buttonStatus[4]) && (buttonStatus[4] == buttonStatus[5]))
					|| ((buttonStatus[5] == buttonStatus[13]) && (buttonStatus[13] == buttonStatus[20]))))) {
				loeschen = true;
				setzen = false;
			}
			break;
		case 6:
			if (((buttonStatus[6] != 0) && (((buttonStatus[6] == buttonStatus[7]) && (buttonStatus[7] == buttonStatus[8]))
					|| ((buttonStatus[11] == buttonStatus[6]) && (buttonStatus[6] == buttonStatus[15]))))) {
				loeschen = true;
				setzen = false;
			}
			break;
		case 7:
			if (((buttonStatus[7] != 0) && (((buttonStatus[6] == buttonStatus[7]) && (buttonStatus[7] == buttonStatus[8]))
					|| ((buttonStatus[1] == buttonStatus[4]) && (buttonStatus[4] == buttonStatus[7]))))) {
				loeschen = true;
				setzen = false;
			}
			break;
		case 8:
			if (((buttonStatus[8] != 0) && (((buttonStatus[6] == buttonStatus[7]) && (buttonStatus[7] == buttonStatus[8]))
					|| ((buttonStatus[8] == buttonStatus[12]) && (buttonStatus[12] == buttonStatus[17]))))) {
				loeschen = true;
				setzen = false;
			}
			break;
		case 9:
			if (((buttonStatus[9] != 0) && (((buttonStatus[9] == buttonStatus[10]) && (buttonStatus[10] == buttonStatus[11]))
					|| ((buttonStatus[0] == buttonStatus[9]) && (buttonStatus[9] == buttonStatus[21]))))) {
				loeschen = true;
				setzen = false;
			}
			break;
		case 10:
			if (((buttonStatus[10] != 0) && (((buttonStatus[9] == buttonStatus[10]) && (buttonStatus[10] == buttonStatus[11]))
					|| ((buttonStatus[3] == buttonStatus[10]) && (buttonStatus[10] == buttonStatus[18]))))) {
				loeschen = true;
				setzen = false;
			}
			break;
		case 11:
			if (((buttonStatus[11] != 0) && (((buttonStatus[9] == buttonStatus[10]) && (buttonStatus[10] == buttonStatus[11]))
					|| ((buttonStatus[11] == buttonStatus[6]) && (buttonStatus[6] == buttonStatus[15]))))) {
				loeschen = true;
				setzen = false;
			}
			break;
		case 12:
			if (((buttonStatus[12] != 0) && (((buttonStatus[12] == buttonStatus[13]) && (buttonStatus[13] == buttonStatus[14]))
					|| ((buttonStatus[8] == buttonStatus[12]) && (buttonStatus[12] == buttonStatus[17]))))) {
				loeschen = true;
				setzen = false;
			}
			break;
		case 13:
			if (((buttonStatus[13] != 0) && (((buttonStatus[5] == buttonStatus[13]) && (buttonStatus[13] == buttonStatus[20]))
					|| ((buttonStatus[12] == buttonStatus[13]) && (buttonStatus[13] == buttonStatus[14]))))) {
				loeschen = true;
				setzen = false;
			}
			break;
		case 14:
			if (((buttonStatus[14] != 0) && (((buttonStatus[2] == buttonStatus[14]) && (buttonStatus[14] == buttonStatus[23]))
					|| ((buttonStatus[12] == buttonStatus[13]) && (buttonStatus[13] == buttonStatus[14]))))) {
				loeschen = true;
				setzen = false;
			}
			break;
		case 15:
			if (((buttonStatus[15] != 0) && (((buttonStatus[6] == buttonStatus[11]) && (buttonStatus[11] == buttonStatus[15]))
					|| ((buttonStatus[15] == buttonStatus[16]) && (buttonStatus[16] == buttonStatus[17]))))) {
				loeschen = true;
				setzen = false;
			}
			break;
		case 16:
			if (((buttonStatus[16] != 0) && (((buttonStatus[16] == buttonStatus[19]) && (buttonStatus[19] == buttonStatus[22]))
					|| ((buttonStatus[15] == buttonStatus[16]) && (buttonStatus[16] == buttonStatus[17]))))) {
				loeschen = true;
				setzen = false;
			}
			break;
		case 17:
			if (((buttonStatus[17] != 0) && (((buttonStatus[8] == buttonStatus[12]) && (buttonStatus[12] == buttonStatus[17]))
					|| ((buttonStatus[15] == buttonStatus[16]) && (buttonStatus[16] == buttonStatus[17]))))) {
				loeschen = true;
				setzen = false;
			}
			break;
		case 18:
			if (((buttonStatus[18] != 0) && (((buttonStatus[3] == buttonStatus[10]) && (buttonStatus[10] == buttonStatus[18]))
					|| ((buttonStatus[18] == buttonStatus[19]) && (buttonStatus[19] == buttonStatus[20]))))) {
				loeschen = true;
				setzen = false;
			}
			break;
		case 19:
			if (((buttonStatus[19] != 0) && (((buttonStatus[16] == buttonStatus[19]) && (buttonStatus[19] == buttonStatus[22]))
					|| ((buttonStatus[18] == buttonStatus[19]) && (buttonStatus[19] == buttonStatus[20]))))) {
				loeschen = true;
				setzen = false;
			}
			break;
		case 20:
			if (((buttonStatus[20] != 0) && (((buttonStatus[5] == buttonStatus[13]) && (buttonStatus[13] == buttonStatus[20]))
					|| ((buttonStatus[18] == buttonStatus[19]) && (buttonStatus[19] == buttonStatus[20]))))) {
				loeschen = true;
				setzen = false;
			}
			break;
		case 21:
			if (((buttonStatus[21] != 0) && (((buttonStatus[0] == buttonStatus[9]) && (buttonStatus[9] == buttonStatus[21]))
					|| ((buttonStatus[21] == buttonStatus[22]) && (buttonStatus[22] == buttonStatus[23]))))) {
				loeschen = true;
				setzen = false;
			}
			break;
		case 22:
			if (((buttonStatus[22] != 0) && (((buttonStatus[16] == buttonStatus[19]) && (buttonStatus[19] == buttonStatus[22]))
					|| ((buttonStatus[21] == buttonStatus[22]) && (buttonStatus[22] == buttonStatus[23]))))) {
				loeschen = true;
				setzen = false;
			}
			break;
		case 23:
			if (((buttonStatus[23] != 0) && (((buttonStatus[2] == buttonStatus[14]) && (buttonStatus[14] == buttonStatus[23]))
					|| ((buttonStatus[21] == buttonStatus[22]) && (buttonStatus[22] == buttonStatus[23]))))) {
				loeschen = true;
				setzen = false;
			}
		}
		
		if(loeschen){
			laufschrift.setText("du hast eine Mühle. Entferne Spielfigur");
		} else {
			ichBinDran=false;
			Thread t = new Thread(new sendToServer());
			t.start();
			Thread s = new Thread(new receiveFromServer());
			s.start();
		}
	}
	
	private void connectToServer(){
		try {
			socket = new Socket("thomas1231231.ddns.net", 7777);
			System.out.println("Erfolgreich verbunden");
		} catch (Exception e) {
			System.out.println("Fehler beim Verbinden zum Server");
			e.printStackTrace();
		} 
	}
	
	private void setupStreams(){
		try {
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
			System.out.println("Streams erfolgreich angelegt");
		} catch (IOException e) {
			System.out.println("Fehler beim Anlegen der Streams");
			e.printStackTrace();
		}
	}
	
	// Beantragt Spielernummer von Server, wenn Spielernummer=1 Beginner
		private void setupGame(){
			try {
				out.writeUTF("0"); // "Ich will eine Spielerfarbe"
				spielerfarbe = Integer.parseInt(in.readUTF());
				
				if(spielerfarbe == 1){
					ichBinDran = true;
					setzen = true;
					laufschrift.setText("du bist dran");
				} else if (spielerfarbe == 2) {
					ichBinDran = false;
					setzen = true;
					laufschrift.setText("Mitspieler ist dran");
					Thread s = new Thread(new receiveFromServer());
					s.start();
				}
				
				System.out.println("Spielernummer: " + spielerfarbe);
				
			} catch (IOException e) {
				System.out.println("Fehler beim übermitteln des Spielstartes");
				e.printStackTrace();
			}
		}	
		
		private String arrayToString(){
			String s = "";		
			for (int i = 0; i < buttonStatus.length; i++) {
				s = s + " " + buttonStatus[i];
			}		
			return s;
		}

		
		private void stringToArray(String string){
			
			StringTokenizer tokenizer = new StringTokenizer(string);
			
			for (int i = 0; i < buttonStatus.length; i++) {
				buttonStatus[i] = Integer.parseInt(tokenizer.nextToken());
				
				// aktualisiere die Farben der Buttons
				if(buttonStatus[i] == 1){
					buttonArray[i].setIcon(imgBtnRot);
				} else if (buttonStatus[i] == 2){
					buttonArray[i].setIcon(imgBtnGelb);
				} else if (buttonStatus[i] == 0){
					buttonArray[i].setIcon(imgBtnKlar);
				}
				
			}
			
		}
}

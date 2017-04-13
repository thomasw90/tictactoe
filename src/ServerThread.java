import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread extends Thread {
	
	private Socket socket1=null;
	private Socket socket2=null;
	
	private DataInputStream in1;
	private DataOutputStream out1;
	private DataInputStream in2;
	private DataOutputStream out2;
	
	private ServerSocket listener;
	
	private int spielerNr=1;

	
	public void run(){		
		setupStreams();
		
		while(true){
			try {
				out1.writeUTF(in2.readUTF());
				out2.writeUTF(in1.readUTF());					
			} catch (IOException e) {
				System.out.println("Fehler beim Übergeben von Stream1 zu Stream2");
				e.printStackTrace();
			}
		}
	}
	
	private void setupStreams(){
		try {
			listener = new ServerSocket(7777);
		} catch (IOException e) {
			System.out.println("Fehler beim Erstellen des ServerSockets");
			e.printStackTrace();
		}
		
		try{
			while(socket1 == null || socket2 == null){
				if(socket1 == null){
					socket1 = listener.accept();
					in1 = new DataInputStream(socket1.getInputStream());
					out1 = new DataOutputStream(socket1.getOutputStream());
					sendPlayerNumber(in1, out1);
				}
				if(socket2 == null){
					socket2 = listener.accept();
					in2 = new DataInputStream(socket2.getInputStream());
					out2 = new DataOutputStream(socket2.getOutputStream());
					sendPlayerNumber(in2, out2);
				}
			}
		} catch (Exception e){
			System.out.println("Fehler beim Anlegen der Streams");
		}
	}
	
	private void sendPlayerNumber(DataInputStream in, DataOutputStream out){
		String string;
		try {
			string = in.readUTF();
    		if(string.equals("0")){ // Wenn Client verbindet (0), sende SpielerNr zurück
	    		out.writeUTF(Integer.toString(spielerNr));
	    		spielerNr++;	    			    		
	    	}
		} catch (IOException e) {
			System.out.println("Fehler beim Senden der Spielernummer");
			e.printStackTrace();
		}
	}
	
	
	
}

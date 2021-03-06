import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
/**
 * Server
 * @author Max Br�mer, Tilman Zuckmantel, Pierre Haritz
 */
public class Server {

	private static ArrayList<String> names = new ArrayList<String>();
	private static ArrayList<PrintWriter> writers = new ArrayList<PrintWriter>();
	
	public static void main(String[] args) throws Exception {
		System.out.println("Der Server wurde gestartet...");
		System.out.println("IP-Adresse: " + InetAddress.getLocalHost());
		//Port auslesen
		int port = 2222;
		if (args.length == 1){
			port = Integer.parseInt(args[0]);
		}
		ServerSocket listener;
		try{
			listener = new ServerSocket(port);
		}catch(Exception e){
			listener = new ServerSocket(0);
		}
		try {
			while (true) {
				// Clients werden fortlaufend akzeptiert und ein Thread f�r sie gestartet:
				new ClientHandler(listener.accept()).start(); 
			}
		} finally {
			listener.close();
		}
	}
	/**
	 * ClientHandler dient zum managen von Clients und der Kommunikation zwischen Client und Server
	 * @author Max Br�mer, Tilman Zuckmantel, Pierre Haritz
	 */
	private static class ClientHandler extends Thread {
		private String name;
		private Socket socket;
		private BufferedReader in;
		private PrintWriter out;

		public ClientHandler(Socket socket) {
			this.socket = socket;
		}
		
		@Override
		public void run() {
			boolean running = true;
			try {

				in = new BufferedReader(new InputStreamReader(
						socket.getInputStream()));
				out = new PrintWriter(socket.getOutputStream(), true);
				boolean nameEntered = false;
				String input;
				String[] arr;
				
				//Implementierung des Protokolls
				while (running){
					input = in.readLine();
					if (input == null) {
						return;
					}
					switch (input.charAt(0)){
						case'n': 
							if (nameEntered){ 
								out.println("e Name kann nicht ge�ndert werden.");
								break;
							}
							arr = input.split(" ");
							if (arr.length != 2){
								out.println("e Name darf keine Leerzeichen enthalten und darf nicht leer sein./n"
										+ " G�ltige Eingabe: n Name");
							}
							else
							{
								name = arr[1];
								synchronized (names) {
									if (!names.contains(name)) {
										names.add(name);
										writers.add(out);
										out.println("s");
										out.println("e Sie sind als" + name + "angemeldet.");
										nameEntered = true;
									}
								}
							}	    
						break;
			
						case'm': 
							if(!nameEntered){
								out.println("e Geben Sie einen Namen ein, bevor Sie anderen schreiben./n"
									        + " G�ltige Eingabe: n Name");	
							break;
							}
							arr = input.split(" ");
							if (arr.length < 3){
								out.println("e Schicken Sie Ihre Nachrichten in der g�ltigen Syntx ab./n"
										+ " G�ltige Eingabe: m Name Nachricht");	
							}
							/*
							synchronized(names){
								if(!names.contains(arr[1])){
									out.println("e Der gew�nschte Nutzer ist nicht verf�gbar oder die Reihenfolge der Eingaben wurde missachtet./n"
											+ " G�ltige Eingabe: m Name Nachricht");	
								}
								else{
									try{
										PrintWriter writer = writers.get(names.indexOf(name));
										String message = new String();
										for (int i = 2; i < arr.length; i ++){message += " " + arr[i];}
										writer.println("m" + " " + name + input);
									}catch(Exception e){ out.println("e Nachricht konnte nicht gesendet werde."); }			
								}
							}
							*/
									
						break;
						
						case't': 	
						//if(!nameEntered){ out.println("Sind sind noch nicht angemeldet!")};
					    String clients = "t";
						for (int i = 0; i < names.size(); i++){
							if (name != names.get(i)) clients += " " + names.get(i);
						}
						out.println(clients);		
						break;
						
						case 'x':
							running = false;
						break;
					}
					
				}
				
// Aufruf bei Beenden des Servers

			} catch (IOException e) {
				System.out.println(e);
			} finally {
				if (running) out.println("x Verbindung wurde aus unbekannten Gr�nden geschlossen");
				else out.println("x Die Verbindung wurde beendet");
				if (name != null) {
					names.remove(name);
				}
				if (out != null) {
					writers.remove(out);
				}
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}
	}

}
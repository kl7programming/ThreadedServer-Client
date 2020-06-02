import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;


public class Proj2_Server implements Runnable{
	
	Socket clientSoc;
	static ServerSocket servSoc = null;
	
	Proj2_Server(Socket cSoc)
	{
		clientSoc = cSoc;
	}

	public static void main(String[] args) throws IOException
	{
		if(args.length != 1)
		{
			System.err.println("Usage: java Proj1_Server <Port#>");
			System.exit(1);
		}
		
		int portNum = Integer.parseInt(args[0]);
		
		servSoc = new ServerSocket(portNum);
		servSoc.setSoTimeout(120000);										//Set Timeout if no connection established in 2 Minutes
		System.out.println("Listening for a connection...");	
		
		try
		{
			while(true)
			{
				Socket soc = servSoc.accept();
				System.out.println("Connection Established with "+soc.getInetAddress().toString());
				new Thread(new Proj2_Server(soc)).start();
			}
		}
		catch(SocketException e)
		{
			System.out.println("A Thread has closed the sockets\nServer Terminating");
		}
		finally
		{
			System.exit(1);
		}
	}
	@Override
	public void run() {
		try(PrintWriter out = new PrintWriter(clientSoc.getOutputStream(), true);
				BufferedReader input = new BufferedReader(new InputStreamReader(clientSoc.getInputStream()));)
		{
			String line;
			
			while((line = input.readLine()) != null)
			{	
				System.out.println("Received: " + line);
				
				if(line.compareTo("7") == 0)
				{
					out.println("EXIT");
					out.flush();
					System.out.println("Exiting...");
					servSoc.close();
					clientSoc.close();
					System.out.println("Connection Terminated.");
					System.exit(0);
				}
				
				CommandHandleProtocol receiver = new CommandHandleProtocol(); 
				
				out.println(receiver.handle(line) + "\nEND");
				System.out.println("Responded");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

class CommandHandleProtocol
{
	public String handle(String commandStr)
	{
		int commandNum = -1;
		try
		{
		commandNum = Integer.parseInt(commandStr);
		}
		catch(NumberFormatException e)
		{
			commandNum = -1;
		}
		StringBuffer output = new StringBuffer();
		String command = "";
		
		if(commandNum == 1)
		{
			command = "date";
		}
		if(commandNum == 2)
		{
			command = "uptime";
		}
		if(commandNum == 3)
		{
			command = "free";
		}
		if(commandNum == 4)
		{
			command = "netstat";
		}
		if(commandNum == 5)
		{
			command = "who";
		}
		if(commandNum == 6)
		{
			command = "ps";
		}
		if(commandNum < 1 || commandNum > 6 )
		{
			command = "invalid";
		}
		
		if(commandNum > 0 && commandNum < 7)
		{
			try
			{
			Process p = Runtime.getRuntime().exec(command);
			p.waitFor();
			
			BufferedReader processReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			String line = "";
			
			while((line = processReader.readLine()) != null)
			{
				output.append(line+'\n');
			}
			
			p.destroy();
			}
			catch(IOException e)
			{
				output.append(e.getMessage()+"\n");
				output.append("Unable to execute command : " + command);
			}
			catch(InterruptedException e)
			{
				output.append(e.getMessage()+"\n");
				output.append("Unable to execute command : " + command);
			}
			catch(Exception e)
			{
				output.append(e.getMessage()+"\n");
				output.append("Unable to execute command : " + command);
			}
			
			output.append('\n');
			return output.toString();
		}
		
		
		return "invalid option";
	}
}

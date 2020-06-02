/*
 * -----------------
 * Proj1_Client.java
 * -----------------
 * This program is the client part of the project. This program displays a menu and asks the user to choose and option and how many times to repeat. It will 		 	
 * then spawn x amount of threads, each thread will connect to the server and sends out a request based on the user's selection. The result 
 * is displayed, the amount of threads successfully executed is displayed ,the total response time and mean response time is calculated. 
 *
 * ---------------
 * Project Details
 * ---------------
 * Group Number: 10
 * Members: Danah Alkadi, Kaihua Liu, Shaneice Lord, Samantha Maletta, Elizabeth Moreno, Michael O'Donnell, Slaven Popadic
 * Class: CNT4504
 * Date: 03/02/2016
 *
 */	

import java.io.*;
import java.net.*;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Proj1_Client implements Runnable
{

	private static int choice, executedThreadCount, portNum;
	//private static CommandRequestProtocol req;
	private static long totalElapsedTime = 0;
	private static long meanElapsedTime = 0;
	private static String host;
	
	public static void main(String[] args) throws IOException
	{
		if(args.length != 2)
		{
			System.err.println("Usage: java Proj1_Client <hostname/IP> <port>");
			System.exit(1);
		}
		
		host = args[0];
		portNum = Integer.parseInt(args[1]);
		
		//Scanner inputScanner = new Scanner(System.in);
		
		System.out.println("Network Project #1");
		System.out.println("------------------");
		
		while(true)
		{
			int repeat = 0;
			choice = 0;
			choice = displayMenu();
			if(choice == 7)
			{
				Thread clientThread = new Thread(new Proj1_Client());
				clientThread.start();
				break;
			}
			
			repeat = repeatPrompt();
			
			Thread clientThreads[] = new Thread[repeat];
			executedThreadCount = 0;
			totalElapsedTime = 0;
			
			for(int i = 0; i < repeat; i++)
			{
				clientThreads[i] = new Thread(new Proj1_Client());
			}
			
			for(int i = 0; i < repeat; i++)
			{
				clientThreads[i].start();
			}	
			
			while(executedThreadCount != repeat)
			{
				try
				{
					Thread.sleep(500);
				}
				catch(InterruptedException e)
				{
					
				}
			}
			
			meanElapsedTime = totalElapsedTime/repeat;
			System.out.println("Threads Executed: " + executedThreadCount);
			System.out.println("Total Elapsed Time: " + totalElapsedTime/1000000.0 + "mSec");
			System.out.println("Mean Elapsed Time: " + meanElapsedTime/1000000.0 + "mSec");
		}
		
	}
	
	public static int displayMenu()
	{
		Scanner inputScanner = new Scanner(System.in);
		int userChoice = 0;
		String nextLine = "";
		
		System.out.println("\n\nSelect a command to request from server.");
		System.out.println("1 - Date/Time");
		System.out.println("2 - Uptime");
		System.out.println("3 - Memory Use");
		System.out.println("4 - Netstat");
		System.out.println("5 - Current Users");
		System.out.println("6 - Running Processes");
		System.out.println("7 - Quit");
		
		while(userChoice > 7 || userChoice < 1)
		{	
			if(inputScanner.hasNextLine())
			{
				nextLine = inputScanner.nextLine();
			}
			try
			{
				userChoice = Integer.parseInt(nextLine);
			}
			catch(NumberFormatException e)
			{
				System.out.println("Please Enter a Number");
			}
			catch(NoSuchElementException e)
			{
			}
			if(userChoice > 7 || userChoice < 1)
			{
				System.out.println("Invalid Option");
			}
		}
		
		return userChoice;
	}
	
	public static int repeatPrompt()
	{
		Scanner inputScanner = new Scanner(System.in);
		int userChoice = 0;
		String nextLine = "";
		
		System.out.println("How many times to repeat?");
		while(true)
		{
			if(inputScanner.hasNextLine())
			{
				nextLine = inputScanner.nextLine();
			}
			try{
				userChoice = Integer.parseInt(nextLine);
				break;
			}
			catch(NumberFormatException e)
			{
				System.out.println("Please Enter a Number");
			}
			catch(NoSuchElementException e)
			{
				
			}
			System.out.println("How many times to repeat?");
		}

		return userChoice;
	}
	
	public void run()
	{
		System.out.println("Trying to connect to " + host + " via port <" + portNum+">");
		Socket s;
		try
		{
			s = new Socket(host, portNum);
			if(!s.isClosed())
			{
				System.out.println("Connection Successful");
			}
			
			try(PrintWriter out = new PrintWriter(s.getOutputStream(), true); 
					BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));)
			{
				CommandRequestProtocol req = new CommandRequestProtocol();
				req.setPrintWriter(out);
				req.setInBufferedReader(in);
				req.request(Integer.toString(choice));
				System.out.println(req.getResponse());
				totalElapsedTime += req.getResponseTime();
				executedThreadCount++;
			}
			catch(UnknownHostException e)
			{
				System.err.println("Unknown Host: " + host);
				System.exit(1);
			}
			catch(IOException e)
			{
				System.err.println("IO Error with Host:" +host);
				System.err.print(e.getMessage());
				System.exit(1);
			}
			catch(NullPointerException e)
			{
				System.err.println("Connection Lost");
				System.exit(0);
			}
			catch(Exception e)
			{
				System.err.println("Unexpected Error");
				System.err.println("Desc: " + e.getMessage());
				System.exit(1);
			}
			finally
			{
				s.close();
			}
		}
		catch(IOException e)
		{
			System.err.println("I/O Exception");
			System.err.println("Desc: " + e.getMessage());
		}
		

		
	}
	
}

class CommandRequestProtocol
{
	private PrintWriter out;
	private BufferedReader in;
	private String response;
	private long responseTime;
	
	public void setPrintWriter(PrintWriter out){this.out = out;};
	public PrintWriter getPrintWriter(){return out;};
	public void setInBufferedReader(BufferedReader in){this.in = in;};
	public BufferedReader getInBufferedReader(){return in;};
	public String getResponse(){return response;};
	public long getResponseTime(){return responseTime;};
	
	public void request(String line) throws IOException
	{
		try
		{
			out.println(line);
			out.flush();
			long startTime = System.nanoTime();
			response = "";
			String responseLine;
			
			while(!(responseLine = in.readLine()).equals("END"))
			{
				response += responseLine + '\n';
				
				if(responseLine.compareToIgnoreCase("EXIT") == 0)
				{
					response = "exiting..";
				}
			}
			
			long stopTime = System.nanoTime();
			responseTime = stopTime - startTime;
		}
		catch(Exception e)
		{
			throw e;
		}
	}
	
}

package classic;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;


public class PollClient {
	
	private static Context jndiContext = null;
    private static QueueSession session = null;
    private QueueSender messageSender = null;
    private Queue publicQueue = null;
    private QueueConnection connection = null;
    
    int myId;
    String myName;
    int numberOfPartcipants;
    ArrayList<String> participantsList;
    
    static ArrayList<Poll> createdPollList = new ArrayList<Poll>();
    static ArrayList<Poll> receivedPollList = new ArrayList<Poll>();
    
    public PollClient(int myId, String myName, int numberOfPartcipants,  ArrayList<String> participantsList) throws Exception  {
    	this.myId = myId;
    	this.numberOfPartcipants = numberOfPartcipants;
    	this.participantsList = participantsList;
    	this.myName = myName;
    	
    	new ReceivingThread(myId, myName, createdPollList, receivedPollList).start();
    }
    
 /*  Create the polls as specified by the user */    
    public void createNewPoll(String pollName, String participantsName, String timeIntervals, String pollId) throws Exception {
        Poll poll = new Poll();
        String[] ptcptList;
        String[] timeIntervalList;
        String[][] decision;
        poll.setPollId(myId + "-" + pollId);
        poll.setInitiatorName(myName);
        poll.setPollName(pollName);
        poll.setPollStatus("Active");
        StringTokenizer participantsNameTokens = new StringTokenizer(participantsName, ",");
        int numParticipantTokens = participantsNameTokens.countTokens();
        ptcptList = new String[numParticipantTokens];
        int i = 0;
        while(participantsNameTokens.hasMoreTokens()) {
        	String name = participantsNameTokens.nextToken().trim();
        	ptcptList[i] = name;
        	i++;
        }
        poll.setParticipantsList(ptcptList);
        
        int j = 0;
        StringTokenizer timeIntervalsTokens = new StringTokenizer(timeIntervals, ",");
        int numTimeIntervalTokens = timeIntervalsTokens.countTokens();
        timeIntervalList = new String[numTimeIntervalTokens];
        while(timeIntervalsTokens.hasMoreTokens()) {
        	String time = timeIntervalsTokens.nextToken().trim();
        	timeIntervalList[j] = time;
        	j++;
        }
        poll.setIntervals(timeIntervalList);
        
        poll.setNumParticipants(numParticipantTokens);
        poll.setNumTimeIntervals(numTimeIntervalTokens);
        
        decision = new String[numParticipantTokens][numTimeIntervalTokens];
        for(int k = 0; k < numParticipantTokens; k++) {
        	 for(int m = 0; m < numTimeIntervalTokens; m++) {
        		 decision[k][m] = "maybe";
        	 }
        }
        poll.setDecision(decision);
        
        updateMyPollCreatedList(poll);
        createConnection(ptcptList, numParticipantTokens, poll);
    }
    
/*  Update the pollcreatedList when a poll is created by the user */    
    synchronized public void updateMyPollCreatedList(Poll poll) {
    	createdPollList.add(poll);
	}
    
/*  Create connection for each of the participants */
    public void createConnection(String[] ptcptList, int numParticipantTokens, Poll poll) throws Exception {
    	int totalNumParticipant = participantsList.size();
    	for(int i=0; i<totalNumParticipant; i++) {
    		String name = participantsList.get(i);
    		for(int j = 0; j < numParticipantTokens ; j++) {
    			if(name.equalsIgnoreCase(ptcptList[j])) {
	        		jndiContext = new InitialContext();
	                publicQueue = (Queue)jndiContext.lookup("queue"+i);
	                QueueConnectionFactory qcf = (QueueConnectionFactory)jndiContext.lookup("qcf");
	                jndiContext.close();
	                
	                connection = qcf.createQueueConnection();
	                session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
	                messageSender = session.createSender(publicQueue);
	                connection.start();
	                sendPoll(poll);
    			}
        	}
    	}
    	
    }
 
/*  Send the created poll to the participants */
    public void sendPoll(Poll poll) throws JMSException {
    	Message msg = session.createObjectMessage();
    	((ObjectMessage) msg).setObject(poll);
    	messageSender.send(msg);
    	connection.close();
    }
    
/*  Display the polls created by the user */
    synchronized public boolean viewCreatedOpenPolls(String st) {
    	boolean noPoll = false;
    	Iterator<Poll> createdPollListItr = createdPollList.iterator();
    	while(createdPollListItr.hasNext()) {
    		Poll createdPoll = (Poll) createdPollListItr.next();
    		String status = createdPoll.getPollStatus();
    		if((status.equalsIgnoreCase(st)) || (st.equalsIgnoreCase("All"))) {
	    		System.out.println("\nPoll Initiator: " + createdPoll.getInitiatorName() + 
	    				"\nPoll Name: " + createdPoll.getPollName() +
	    				"\nPoll Id: " + createdPoll.getPollId() +
	    				"\nPoll Status: "+ createdPoll.getPollStatus());
	    		int numP = createdPoll.getNumParticipants();
	    		int numT = createdPoll.getNumTimeIntervals();
	    		String[][] decision = new String [numP][numT];
	    		decision = createdPoll.getDecision();
	    		String[] participants = createdPoll.getParticipantsList();
	    		String[] timeIntervals = createdPoll.getIntervals();
	    		System.out.println("\nMeeting Time" + "\t\t" + "Participant Name" +"\t" + "Decision");
	    		for(int j = 0; j < numT; j++) {
	    			for(int i = 0; i < numP; i++) {
	    				System.out.println(timeIntervals[j] + "\t" + participants[i] +"\t\t" + decision[i][j]);
	           		}
	    		}
	    		noPoll = true;
	    		if(status.equalsIgnoreCase("Closed")) {
	    			System.out.println("\nFinalized Meeting Time: " + createdPoll.getFinalizedTime());
	    		}
           }	
    	}
    	return noPoll;
	}
    
/*  Display the polls received by the user */
    synchronized public boolean viewReceivedOpenPolls(String st) {
    	boolean noPoll = false;
    	Iterator<Poll> receivedPollListItr = receivedPollList.iterator();
    	while(receivedPollListItr.hasNext()) {
    		Poll receivedPoll = (Poll) receivedPollListItr.next();
    		String status = receivedPoll.getPollStatus();
    		if((status.equalsIgnoreCase(st)) || (st.equalsIgnoreCase("All"))) {
	    		System.out.println("\nPoll Initiator: " + receivedPoll.getInitiatorName() + 
	    				"\nPoll Name: " + receivedPoll.getPollName() +
	    				"\nPoll Id: " + receivedPoll.getPollId() +
	    				"\nPoll Status: "+ receivedPoll.getPollStatus());
	    		int numP = receivedPoll.getNumParticipants();
	    		int numT = receivedPoll.getNumTimeIntervals();
	    		String[][] decision = new String [numP][numT];
	    		decision = receivedPoll.getDecision();
	    		String[] participants = receivedPoll.getParticipantsList();
	    		String[] timeIntervals = receivedPoll.getIntervals();
	    		System.out.println("\nMeeting Time" + "\t\t" + "Participant Name" +"\t" + "Decision");
	    		for(int j = 0; j < numT; j++) {
	    			for(int i = 0; i < numP; i++) {
	    				System.out.println(timeIntervals[j] + "\t" + participants[i] +"\t\t" + decision[i][j]);
	           		}
	    		}
	    		noPoll = true;
	    		if(status.equalsIgnoreCase("Closed")) {
	    			System.out.println("\nFinalized Meeting Time: " + receivedPoll.getFinalizedTime());
	    		}
           }	
    	}
    	return noPoll;
	}

/*  Respond to the polls received by the user */
    synchronized public void respondToPoll(String pollId) throws Exception {
    	boolean cannotRespond = true;
    	Iterator<Poll> receivedPollListItr = receivedPollList.iterator();
    	while(receivedPollListItr.hasNext()) {
    		Poll receivedPoll = (Poll) receivedPollListItr.next();
    		String status = receivedPoll.getPollStatus();
    		String receivedPollId = receivedPoll.getPollId();
    		if(receivedPollId.equalsIgnoreCase(pollId) && !(status.equalsIgnoreCase("Responded") || (status.equalsIgnoreCase("Closed")))) {
	    		System.out.println("\nPoll Initiator: " + receivedPoll.getInitiatorName() + 
	    				"\nPoll Name: " + receivedPoll.getPollName() +
	    				"\nPoll Id: " + receivedPoll.getPollId());
	    		String[] ptcptList = {receivedPoll.getInitiatorName()};
	    		int numP = receivedPoll.getNumParticipants();
	    		int numT = receivedPoll.getNumTimeIntervals();
	    		String[][] decision = new String [numP][numT];
	    		decision = receivedPoll.getDecision();
	    		String[] participants = receivedPoll.getParticipantsList();
	    		String[] timeIntervals = receivedPoll.getIntervals();
	    		System.out.println("\nEnter your response for the available meeting times:");
	    		BufferedReader stdinp = new BufferedReader(new InputStreamReader(System.in));
	    		for(int i = 0; i < numP; i++) {
	    			if(myName.equalsIgnoreCase(participants[i])) {
		    			for(int j = 0; j < numT; j++) {
		    				System.out.println("\nMeeting Time: " + timeIntervals[j] +" ");
		    				String response = "";
		        			while(response.equalsIgnoreCase("")) {
		        				System.out.println("Enter your Response (Yes, No, Maybe): ");
		        				response = stdinp.readLine();
		        				if((response.equalsIgnoreCase("Yes")) || (response.equalsIgnoreCase("No"))
		        						|| (response.equalsIgnoreCase("Maybe")) ) {
		        					decision[i][j] = response;
		        				} else {
		        					response = "";
		        				}
		        			} 
		           		}
		    			cannotRespond = false;
	    			}
	    		}
	    		receivedPoll.setDecision(decision);
	    		receivedPoll.setPollStatus("Responded");
	    		receivedPoll.setResponder(myName);
	    		
	    		createConnection(ptcptList, 1, receivedPoll);
    		}
    	}
    	if(cannotRespond || receivedPollList.isEmpty()) { 
    		System.out.println("You cannot respond to the poll with poll Id " + pollId); 
		}
    	
    }

/*  Close a active or responded poll created by the user */
    synchronized public void closePoll(String pollId) throws Exception {
    	boolean cannotClose = true;
    	Iterator<Poll> createdPollListItr = createdPollList.iterator();
    	while(createdPollListItr.hasNext()) {
    		Poll createdPoll = (Poll) createdPollListItr.next();
    		String status = createdPoll.getPollStatus();
    		String createdPollId = createdPoll.getPollId();
    		if(createdPollId.equalsIgnoreCase(pollId) && !(status.equalsIgnoreCase("Closed"))) {
	    		System.out.println("\nPoll Initiator: " + createdPoll.getInitiatorName() + 
	    				"\nPoll Name: " + createdPoll.getPollName() +
	    				"\nPoll Id: " + createdPoll.getPollId());
	    		int numP = createdPoll.getNumParticipants();
	    		int numT = createdPoll.getNumTimeIntervals();
	    		String[][] decision = new String [numP][numT];
	    		decision = createdPoll.getDecision();
	    		String[] participants = createdPoll.getParticipantsList();
	    		String[] timeIntervals = createdPoll.getIntervals();
	    		System.out.println("\nMeeting Time" + "\t\t" + "Participant Name" +"\t" + "Decision");
	    		for(int j = 0; j < numT; j++) {
	    			for(int i = 0; i < numP; i++) {
	    				System.out.println(timeIntervals[j] + "\t" + participants[i] +"\t\t" + decision[i][j]);
	           		}
	    		}
	    		
	    		//Menu for choosing the finalized time
	    		System.out.println("\nChoose the finalized time. You can select one of the following:");
	    		for(int j = 0; j < numT; j++) {
	    			System.out.println(j + 1 + "." + timeIntervals[j]);
	    		}
	    		
	    		System.out.println("\nEnter your choice between 1 and " + numT + " (inclusive):");
	    		BufferedReader stdinp = new BufferedReader(new InputStreamReader(System.in));
	    		String finalMeetingTimeChoice = "";
	    		String finalMeetingTime = "";
	    		while(finalMeetingTimeChoice.equalsIgnoreCase("")) {
	    			
    				finalMeetingTimeChoice = stdinp.readLine();
    				int choice = Integer.parseInt(finalMeetingTimeChoice);
    				if ((choice > 0) && (choice <= numT)){
    					finalMeetingTime = timeIntervals[choice-1];
					} 
    			} 
	    		createdPoll.setPollStatus("Closed");
	    		createdPoll.setFinalizedTime(finalMeetingTime);
	    		createConnection(participants, numP, createdPoll);
	    		cannotClose = false;
    		}
    	}
    	if(cannotClose || createdPollList.isEmpty()) { 
    		System.out.println("You cannot close the poll with poll Id " + pollId); //either because you are the creator or you are not invited to participate in the poll.
		}
    }
    
/*  User Menu provided to the poll client */ 
    public void userMenu() {
    	System.out.println("\nMain Poll Menu for user " + myName +
    								   ": \n 1. Create a New Poll." +
				 					     "\n 2. Display all Polls." +
				 					     "\n 3. Display all New Polls." +
				 					     "\n 4. Display all Responded Polls." +
				 					     "\n 5. Display all Closed Polls (with finalized times)." +
				 				         "\n 6. Respond to an Open Poll." +
				 					     "\n 7. Close an Active Poll.");
    	System.out.println("\nPlease enter your choice:");
    }
    
    public static void main(String[] args) throws Exception {
        System.out.println("... Starting the Poll Client...");
        
        ArrayList<String> participantsList = new ArrayList<String>();
        int pollCount = 0;
        /* Reading the config file */
		try{
			FileInputStream fstream = new FileInputStream(args[0]);			  
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;			  
			while ((strLine = br.readLine()) != null)   {
				StringTokenizer st = new StringTokenizer(strLine, ":");
    			String tag = st.nextToken();  
    			if(tag.equalsIgnoreCase("participantName")) {
    				participantsList.add(st.nextToken());
    			}
		  }
			//Close the input stream
		  in.close();
	    } catch (Exception e){//Catch exception if any
			 
	    }
		
		int numberOfPartcipants = participantsList.size();

		int myId = -1;
		String myName = "";
		boolean client_found = false;
		
		//start the clients
		try{
			BufferedReader stdinp = new BufferedReader(new InputStreamReader(System.in));
			while(!client_found) {
				System.out.println("Enter the client number between 0 and " + (numberOfPartcipants - 1) + "(inclusive): ");
				String echoline = "";
				try {
					echoline = stdinp.readLine();
				} catch (IOException e1) {
					
				}
				
				try{
					int numserver = Integer.parseInt(echoline);
					if ((numserver >= 0) && (numserver < numberOfPartcipants)){
						myId = numserver;
						myName = participantsList.get(myId);
		    			System.out.println("Poll Client "+ myId +" started: ");
		    			client_found = true;
					} 
					else  {
		    			System.out.println("Server number should be between 0 and " + (numberOfPartcipants - 1) + "(inclusive).");
		    			System.out.println("\nPlease enter your choice");
		    		} 
				}catch (Exception e) {
		                System.err.println("Input not in proper form. Query not processed. Please enter your input.");
		        }
			}
		}
		catch (Exception e) {
			System.out.println("Error "+e);
        }
		
		
		PollClient myPollClient = new PollClient(myId, myName, numberOfPartcipants, participantsList);
		
    	System.out.println("Poll Client ready to take inputs........");
        try {
        	BufferedReader stdinp = new BufferedReader(new InputStreamReader(System.in));
        	
        	while (true) {   
        		boolean done = false;
        		myPollClient.userMenu();
        		try {        			
	        		String echoline = stdinp.readLine();
	        		StringTokenizer st = null;
	        		if(echoline.equalsIgnoreCase(null) || echoline.equalsIgnoreCase("")) {
	        			
	        		} else {
	        			st = new StringTokenizer(echoline);       		
	        			String tag = st.nextToken();        		
		        		if(tag.equalsIgnoreCase("1")) {
		        			while(!done) {
		        				String pollName = "";
			        			while(pollName.equalsIgnoreCase("")) {
			        				System.out.println("\nEnter Poll Name: ");
			        				pollName = stdinp.readLine();
			        			} 
			        			
			        			System.out.println("\nPick Participant's Name from the list of Participants: ");
			        			for(int i = 0; i < numberOfPartcipants; i++) {
			        				if(i != myId) {
			        					System.out.println(participantsList.get(i));
			        				}
			        			}
			        			String participantsName = "";
			        			while(participantsName.equalsIgnoreCase("")) {
			        				System.out.println("\nEnter Participant Name (Names are comma separated): ");
			        				participantsName = stdinp.readLine();
			        				StringTokenizer participantsNameTokens = new StringTokenizer(participantsName, ",");
			        				boolean nameValid = true;
			        				while(participantsNameTokens.hasMoreTokens() && (nameValid)) {
			        					boolean nameCorrect = false;
			        		        	String name = participantsNameTokens.nextToken().trim();
			        		        	for(int i = 0; i < numberOfPartcipants; i++) {
			        		        		String temp = participantsList.get(i);
					        				if((i != myId) && (name.equalsIgnoreCase(temp)) && !(nameCorrect)) {
					        					nameCorrect = true;
					        				}
					        			}
			        		        	if(!(nameCorrect)) {
			        		        		System.out.println("\nMalformed name " + name);
			        		        		nameValid = false;
			        		        		participantsName = "";
			        		        	}
			        		        }
			        			}
			        			
			        			String timeIntervals = "";
			        			System.out.println("\nYou will be prompted to enter a sequence of proposed meeting times. A meeting time" +
			        					" consists of a date (e.g. 08/10/2012) and time (e.g. 11:30am-1:00pm).");
			        			System.out.println("\nEnter a Meeting Time. ");
			        			boolean flag = false;
			        			while(!flag) {
			        				String date = "";
			        				boolean datematches = false;
			        				while(date.equalsIgnoreCase("")) {
			        					System.out.println("\nDate (in mm/dd/yyyy format):");
			        					date = stdinp.readLine();
			        					datematches = Pattern.matches("^(0[1-9]|1[012])[/](0[1-9]|[12][0-9]|3[01])[/](20)\\d\\d$", date);
			        					if(!datematches) {
			        						System.out.println("Invalid date format.");
			        						date = "";
			        					}
			        				}
			        				
			        				String time = "";
			        				boolean timematches = false;
			        				while(time.equalsIgnoreCase("")) {
			        					System.out.println("Time (Ex. 11:30am-1:00pm):");
			        					time = stdinp.readLine();
			        					timematches = Pattern.matches("^(1[012]|[1-9]):[0-5][0-9](\\s)?(?i)(am|pm)-(1[012]|[1-9]):[0-5][0-9](\\s)?(?i)(am|pm)$", time);
			        					if(!timematches) {
			        						System.out.println("Invalid format for time interval.");
			        						time = "";
			        					}
			        				}
			        				
			        				timeIntervals = timeIntervals + date + " " + time;
			        				boolean choice = false;
			        				String response = "";
			        				while(!choice) {
				        				System.out.println("Do you want to enter another meeting time? Type Yes or No");
				        				response = stdinp.readLine();
				        				if(!(((response.equalsIgnoreCase("Yes")) || (response.equalsIgnoreCase("No")))))
				        					System.out.println("Please answer Yes or No");
				        				else 
				        					choice = true;
			        				}
				        				if(response.equalsIgnoreCase("Yes")) {
				        					timeIntervals = timeIntervals + ",";
				        				} else if(response.equalsIgnoreCase("No")) {
				        					flag = true;
				        				}
			        			}
			        			pollCount = pollCount + 1;
			        			String pollId = "" + pollCount;
			        			
			        			myPollClient.createNewPoll(pollName, participantsName, timeIntervals, pollId); 
			        			
			        			done = true;
		        			} 
		        		} else if(tag.equalsIgnoreCase("2")) {
		        			boolean aCrPoll = myPollClient.viewCreatedOpenPolls("All");
		        			boolean aRePoll = myPollClient.viewReceivedOpenPolls("All");
		        			boolean noPoll = aCrPoll || aRePoll;
		        			if(!noPoll) {
		        				System.out.println("There are no polls to display at this time.");
		        			}
		        		} else if(tag.equalsIgnoreCase("3")) {
		        			boolean aCrPoll = myPollClient.viewCreatedOpenPolls("Active");
		        			boolean aRePoll = myPollClient.viewReceivedOpenPolls("Active");
		        			boolean noPoll = aCrPoll || aRePoll;
		        			if(!noPoll) {
		        				System.out.println("There are no new polls to display at this time.");
		        			}
		        		} else if(tag.equalsIgnoreCase("4")) {
		        			boolean aCrPoll = myPollClient.viewCreatedOpenPolls("Responded");
		        			boolean aRePoll = myPollClient.viewReceivedOpenPolls("Responded");
		        			boolean noPoll = aCrPoll || aRePoll;
		        			if(!noPoll) {
		        				System.out.println("There are no responded polls to display at this time.");
		        			}
		        		} else if(tag.equalsIgnoreCase("5")) {
		        			boolean aCrPoll = myPollClient.viewCreatedOpenPolls("Closed");
		        			boolean aRePoll = myPollClient.viewReceivedOpenPolls("Closed");
		        			boolean noPoll = aCrPoll || aRePoll;
		        			if(!noPoll) {
		        				System.out.println("There are no closed polls to display at this time.");
		        			}
		        		} else if(tag.equalsIgnoreCase("6")) {
		        			while(!done) {
			        			String pollId = "";
			        			while(pollId.equalsIgnoreCase("")) {
			        				System.out.println("Enter Poll Id: ");
			        				pollId = stdinp.readLine();
			        			} 
			        			
			        			myPollClient.respondToPoll(pollId);
			        			done = true;
		        			}
		        		} else if(tag.equalsIgnoreCase("7")) {
		        			while(!done) {
		        			String pollId = "";
		        			while(pollId.equalsIgnoreCase("")) {
		        				System.out.println("Enter Poll Id: ");
		        				pollId = stdinp.readLine();
		        			} 
		        			
		        			myPollClient.closePoll(pollId);
		        			done = true;
	        			}
		        		}
		        		else {
		        			System.out.println("Malformed Query");
		        		}
		        	}
        		} 
        		catch (Exception e) {        			        			        			        			                   
                }
        	}
        } catch (Exception e) {
        	
        }
    }
}

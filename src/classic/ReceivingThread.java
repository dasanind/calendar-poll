package classic;

import java.util.ArrayList;
import java.util.Iterator;

import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;


public class ReceivingThread extends Thread {

	int myId;
    String myName;
    ArrayList<Poll> createdPollList;
    ArrayList<Poll> receivedPollList;
    
    private static Context jndiContext = null;
    private Queue publicQueue = null; 
    private QueueSession session = null;
    private QueueReceiver receiver = null;
    private QueueConnection connection = null;
    
    public ReceivingThread(int myId, String myName, ArrayList<Poll> createdPollList, ArrayList<Poll> receivedPollList) throws Exception {
    	this.myId = myId;
        this.myName = myName;
        this.createdPollList = createdPollList;
        this.receivedPollList = receivedPollList;
    }
	
    synchronized public void run() {    	
        try {
			while(true) {
				jndiContext = new InitialContext();
		        publicQueue = (Queue)jndiContext.lookup("queue"+myId);
		        QueueConnectionFactory qcf = (QueueConnectionFactory)jndiContext.lookup("qcf");
		        jndiContext.close();
		        
		        connection = qcf.createQueueConnection();
		        session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		        receiver = session.createReceiver(publicQueue);
		        connection.start();
				receivePoll();
			} 
		}
		catch (Exception e) {
			
		}
    }
    
/*  Process the polls received depending upon its status and type */    
    public void receivePoll() throws Exception{
        ObjectMessage received = (ObjectMessage) receiver.receive();
        Poll poll = new Poll();
        poll = (Poll) ((ObjectMessage) received).getObject();
        String pollInitiator = poll.getInitiatorName();
        String pollStatus = poll.getPollStatus();
        if(pollInitiator.equalsIgnoreCase(myName)) {
        	updateCreatedOrReceivedPollList(poll, pollInitiator, "myPoll");
        } else {
        	if(pollStatus.equalsIgnoreCase("Closed")) {
        		updateReceivedPollList(poll);
        	} else {
        		updateCreatedOrReceivedPollList(poll, pollInitiator, "othersPoll");
        		System.out.println(pollInitiator + " has created a new poll with name " + poll.getPollName() + ".");
        	}
        }
        connection.close();
    }
    
/*  Update the createdPoll List when the user receives the decision about the poll created  
 *  Update the received Poll List when the user receives the poll created by other users */
    synchronized void updateCreatedOrReceivedPollList(Poll poll, String pollInitiator, String type) {
    	if(type.equalsIgnoreCase("myPoll")){
    		Iterator<Poll> createdPollListItr = createdPollList.iterator();
    		while(createdPollListItr.hasNext()) {
    			Poll createdPoll = (Poll) createdPollListItr.next();
    			String pollId = createdPoll.getPollId();
    			String pollStatus = createdPoll.getPollStatus();
    			int numParticipants = createdPoll.getNumParticipants();
    			int numTimeIntervals = createdPoll.getNumTimeIntervals();
    			int numP = poll.getNumParticipants();
    			int numT = poll.getNumTimeIntervals();
    			String[][]decision = new String[numParticipants][numTimeIntervals];
    			String[][]decisionReceived = new String[numP][numT];
    			decisionReceived = poll.getDecision();
    			decision = createdPoll.getDecision();
    			if(pollId.equalsIgnoreCase(poll.getPollId()) && !(pollStatus.equalsIgnoreCase("Closed"))){
    				for (int i=0; i<numParticipants; i++) {
    					String ptcptName = createdPoll.getParticipantsList()[i];
    					String responderName = poll.getResponder();
    					if(responderName.equalsIgnoreCase(ptcptName)) {
        		        	for (int j=0; j<numTimeIntervals; j++) {
        		        		decision[i][j] = decisionReceived[i][j];
        		        	}
        		        	System.out.println(poll.getParticipantsList()[i] + " has responded to your poll named " + poll.getPollName());
        		        	createdPoll.setDecision(decision);
    					}
    		        }
    				createdPoll.setPollStatus(poll.getPollStatus());
    			}
    		}
    	} else if(type.equalsIgnoreCase("othersPoll")){
			receivedPollList.add(poll);
    	}
	}
    
/*  Update the received poll list with the status and the finalized time when a poll is closed by the poll initiator */    
    synchronized void updateReceivedPollList(Poll poll) {
    	Iterator<Poll> receivedPollListItr = receivedPollList.iterator();
    	while(receivedPollListItr.hasNext()) {
    		Poll receivedPoll = (Poll) receivedPollListItr.next();
    		String receivedPollId = receivedPoll.getPollId();
    		if(receivedPollId.equalsIgnoreCase(poll.getPollId())) {
	    		
	    		int numP = receivedPoll.getNumParticipants();
	    		int numT = receivedPoll.getNumTimeIntervals();
	    		String[][] decision = new String [numP][numT];
	    		decision = poll.getDecision();
	    		receivedPoll.setDecision(decision);
	    		receivedPoll.setPollStatus(poll.getPollStatus());
	    		receivedPoll.setFinalizedTime(poll.getFinalizedTime());
	    		System.out.println("Poll initiator " + poll.getInitiatorName() + " closed the poll " + poll.getPollName() +
	    				" with poll Id " + poll.getPollId() + ". The final meeting time is " + poll.getFinalizedTime());
    		}
    	}
    }
        
}

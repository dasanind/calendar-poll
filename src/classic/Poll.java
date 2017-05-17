package classic;

import java.io.Serializable;

public class Poll implements Serializable{
	
	String pollId;
	String pollName;
	String initiatorName;
	String[] participantsList;
	String[][] decision;
	String[] intervals;
	int numParticipants;
	int numTimeIntervals;
	String pollStatus;
	String responder;
	String finalizedTime;
	
	public String getPollId() {
		return pollId;
	}
	public void setPollId(String pollId) {
		this.pollId = pollId;
	}
	public String getPollName() {
		return pollName;
	}
	public void setPollName(String pollName) {
		this.pollName = pollName;
	}
	public String getInitiatorName() {
		return initiatorName;
	}
	public void setInitiatorName(String initiatorName) {
		this.initiatorName = initiatorName;
	}
	public String[] getParticipantsList() {
		return participantsList;
	}
	public void setParticipantsList(String[] participantsList) {
		this.participantsList = participantsList;
	}
	public String[][] getDecision() {
		return decision;
	}
	public void setDecision(String[][] decision) {
		this.decision = decision;
	}
	public String[] getIntervals() {
		return intervals;
	}
	public void setIntervals(String[] intervals) {
		this.intervals = intervals;
	}
	public int getNumParticipants() {
		return numParticipants;
	}
	public void setNumParticipants(int numParticipants) {
		this.numParticipants = numParticipants;
	}
	public int getNumTimeIntervals() {
		return numTimeIntervals;
	}
	public void setNumTimeIntervals(int numTimeIntervals) {
		this.numTimeIntervals = numTimeIntervals;
	}
	public String getPollStatus() {
		return pollStatus;
	}
	public void setPollStatus(String pollStatus) {
		this.pollStatus = pollStatus;
	}
	public String getResponder() {
		return responder;
	}
	public void setResponder(String responder) {
		this.responder = responder;
	}
	public String getFinalizedTime() {
		return finalizedTime;
	}
	public void setFinalizedTime(String finalizedTime) {
		this.finalizedTime = finalizedTime;
	}
}

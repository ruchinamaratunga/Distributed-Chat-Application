package com.ds.chatserver.serverstate;

import com.ds.chatserver.config.ServerConfigurations;
import com.ds.chatserver.serverhandler.Server;
import com.ds.chatserver.serverhandler.ServerRequestSender;
import com.ds.chatserver.utils.ServerServerMessage;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

import static com.ds.chatserver.constants.CommunicationProtocolKeyWordsConstants.*;

@Slf4j
public class CandidateState extends ServerState {

    public CandidateState(Server server) {
        super(server);
        this.server.setLeaderId(null);
        log.info("Candidate State : {}", this.server.getCurrentTerm());
    }

    @Override
    public void initState() {

    }

    @Override
    public void heartBeatAndLeaderElect() throws IOException {
        this.server.incrementTerm();
        log.info("Initialize a Vote for the term {}", this.server.getCurrentTerm());
        this.server.setLastVotedTerm(this.server.getCurrentTerm());
        int serverCount = ServerConfigurations.getNumberOfServers();
        ArrayBlockingQueue<JSONObject> queue = new ArrayBlockingQueue<JSONObject>(serverCount);
        int voteCount = 1;
        int rejectCount = 0;
        Set<String> serverIds = ServerConfigurations.getServerIds();
        JSONObject jsonMessage = ServerServerMessage.getRequestVoteRequest(
                this.server.getCurrentTerm(),
                this.server.getServerId(),
                this.server.getRaftLog().getLastLogIndex(),
                this.server.getRaftLog().getLastLogTerm());

        for (String id: serverIds) {
            if (id.equals(this.server.getServerId())) {
                continue;
            }
            Thread thread = new Thread(new ServerRequestSender( id, jsonMessage, queue));
            thread.start();
        }

        while(true) {
            try {
                JSONObject response = queue.take();
                if ((!(Boolean) response.get(ERROR)) && (Boolean) response.get(VOTE_GRANTED)) {
                    voteCount++;
                    log.info("Vote True");
                } else {
                    if(!(Boolean) response.get(ERROR)){
                        int responseTerm = Integer.parseInt((String) response.get(TERM));
                        if(responseTerm > this.server.getCurrentTerm()){
                            this.server.setCurrentTerm(responseTerm);
                            this.server.setState(new FollowerState(this.server, null));
                            return;
                        }
                    }

                    log.info("Vote False");
                    rejectCount ++;
                    if (rejectCount >= (serverCount - serverCount/2)) {
                        int electionTimeOut = 150 + (int)(Math.random()*150);
                        log.info("Election Timeoout - {}", electionTimeOut);
                        Thread.sleep(electionTimeOut);
                        break;
                    }
                }
                if (voteCount > serverCount/2) {
                    // stateChange
                    this.server.setState(new LeaderState(this.server));
                    break;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public JSONObject handleRequestVote(JSONObject request) {
        //TODO: recheck the conditions

        int requestTerm = Integer.parseInt((String)request.get(TERM));
        if (this.server.getCurrentTerm() < requestTerm) {
            this.server.setState(new FollowerState(this.server, null));
            return this.server.getState().handleRequestVote(request);
        }
        return ServerServerMessage.getRequestVoteResponse(this.server.getCurrentTerm(), false);
    }

    public JSONObject handleRequestAppendEntries(JSONObject jsonObject) {

        int requestTerm = Integer.parseInt((String)jsonObject.get(TERM));
        String leaderId = (String) jsonObject.get(LEADER_ID);
        log.info("Append Entry {} from {}", requestTerm, leaderId);
        boolean success = false;
        if (requestTerm >= this.server.getCurrentTerm()) {
            log.info("New Leader Appointed {} for the term {}", leaderId, requestTerm);
            this.server.setCurrentTerm(requestTerm);
            this.server.setState(new FollowerState(this.server, leaderId));
            return this.server.getState().handleRequestAppendEntries(jsonObject);
        }
        JSONObject response = ServerServerMessage.getAppendEntriesResponse(
                this.server.getCurrentTerm(),
                success
        );
        return response;
    }

    @Override
    public String printState(){
        return "Candidate State - Term: " + this.server.getCurrentTerm() + " Leader: " + this.server.getLeaderId();
    }
}




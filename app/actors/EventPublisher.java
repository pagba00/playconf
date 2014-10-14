package actors;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import play.Logger;
import play.libs.Akka;
import play.mvc.WebSocket.Out;
import actors.messages.CloseConnectionEvent;
import actors.messages.NewConnectionEvent;
import actors.messages.UserEvent;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;

public class EventPublisher extends UntypedActor {

	public static ActorRef ref = Akka.system().actorOf(Props.create(EventPublisher.class));
	
	private Map<String, Out<JsonNode>> connections = new HashMap<String, Out<JsonNode>>();
	
	@Override
	public void onReceive(Object message) throws Exception {
		if(message instanceof NewConnectionEvent){
			NewConnectionEvent nce = (NewConnectionEvent) message;
			connections.put(nce.uuid(), nce.out());
			Logger.info("NewConnectionEvent: " + nce.uuid());
		} else if(message instanceof CloseConnectionEvent){
			CloseConnectionEvent cce = (CloseConnectionEvent) message;
			connections.remove(cce.uuid());
			Logger.info("CloseConnectionEvent: " + cce.uuid());
		} else if(message instanceof UserEvent){
			broadcastMessage((UserEvent)message);
			Logger.info("UserEvent: ");
		} else{
			unhandled(message);
		}
	}

	private void broadcastMessage(UserEvent message) {
		for(Out<JsonNode> out : connections.values()){
			out.write(message.json());
		}
	} 
}

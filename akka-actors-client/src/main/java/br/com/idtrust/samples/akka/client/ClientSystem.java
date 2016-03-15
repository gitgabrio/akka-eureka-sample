package br.com.idtrust.samples.akka.client;

import java.util.Random;
import java.util.Scanner;

import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import br.com.idtrust.samples.akka.protocol.MessageProto;

public class ClientSystem {


    public static void main(String[] args) throws Exception {
        final ActorSystem system = ActorSystem.create("Client",
                ConfigFactory.load("application"));

        final ActorRef actor = system.actorOf(
                Props.create(ClientActor.class), "clientActor");
        
        Thread.sleep(2000);

        Random random = new Random();

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Type a message or 'quit'");
            String inputMessage = scanner.nextLine();
            if (inputMessage.equals("quit")) {
                break;
            }
            MessageProto.Message message =
                    MessageProto.Message.newBuilder()
                            .setId(random.nextInt()).setDescription(inputMessage).build();
            actor.tell(message, null);
        }

        system.terminate();

    }
}



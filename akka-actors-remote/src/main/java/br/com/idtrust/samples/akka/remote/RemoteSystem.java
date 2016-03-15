package br.com.idtrust.samples.akka.remote;

import com.typesafe.config.ConfigFactory;

import akka.actor.ActorSystem;
import akka.actor.Props;

public class RemoteSystem {
    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("RemoteWorkerSystem", ConfigFactory.defaultApplication());
        system.actorOf(Props.create(RemoteActor.class),"remoteActor");
    }
}

package br.com.idtrust.samples.akka.client;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.MyDataCenterInstanceConfig;
import com.netflix.discovery.DefaultEurekaClientConfig;
import com.netflix.discovery.DiscoveryManager;

import akka.actor.ActorIdentity;
import akka.actor.ActorRef;
import akka.actor.Identify;
import akka.actor.ReceiveTimeout;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import akka.japi.Procedure;
import br.com.idtrust.samples.akka.protocol.MessageProto;

public class ClientActor extends UntypedActor {

    private ActorRef remoteActor = null;
    String path = null;

    public ClientActor() {
        sendIdentifyRequest();
    }

    private void sendIdentifyRequest() {
        DiscoveryManager.getInstance().initComponent(
                new MyDataCenterInstanceConfig(),
                new DefaultEurekaClientConfig());
        InstanceInfo nextServerInfo = DiscoveryManager.getInstance()
                .getEurekaClient()
                .getNextServerFromEureka("akka.idtrust.com.br", false);
        final String serviceUrl = nextServerInfo.getInstanceId();
        path = "akka.tcp://RemoteWorkerSystem@" + serviceUrl + ":" + nextServerInfo.getPort() + "/user/remoteActor";
//        path = "akka.tcp://RemoteWorkerSystem@localhost" + ":" + nextServerInfo.getPort() + "/user/remoteActor";
        System.out.println("Sending message to server " + serviceUrl);

        getContext().actorSelection(path).tell(new Identify(path), getSelf());
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof ActorIdentity) {
            remoteActor = ((ActorIdentity) message).getRef();
            if (remoteActor == null) {
                System.out.println("Remote actor not available: " + path);
                sendIdentifyRequest();
            } else {
                getContext().watch(remoteActor);
                getContext().become(active, true);
            }

        } else if (message instanceof ReceiveTimeout) {
            sendIdentifyRequest();
        } else {
            System.out.println("Not ready yet");

        }
    }

    Procedure<Object> active = new Procedure<Object>() {

        public void apply(Object message) {

            if (message instanceof MessageProto.Message) {
                try {
                    remoteActor.tell(message, getSelf());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (message instanceof MessageProto.ResultMessage) {
                System.out.println(((MessageProto.ResultMessage) message).getStatus());
            } else if (message instanceof Terminated) {
                System.out.println("OMG, they killed Kenny, searching on eureka ");
                getContext().unwatch(remoteActor);
                getContext().unbecome();
                sendIdentifyRequest();
            } else {
                System.out.println(message.getClass().getName());
                unhandled(message);
            }

        }
    };
}
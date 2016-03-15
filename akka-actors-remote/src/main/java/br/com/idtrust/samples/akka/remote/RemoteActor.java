package br.com.idtrust.samples.akka.remote;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.MyDataCenterInstanceConfig;
import com.netflix.discovery.DefaultEurekaClientConfig;
import com.netflix.discovery.DiscoveryManager;
import com.netflix.discovery.EurekaClient;

import akka.actor.UntypedActor;
import br.com.idtrust.samples.akka.protocol.MessageProto;

public class RemoteActor extends UntypedActor {


    private ApplicationInfoManager applicationInfoManager;
    private EurekaClient eurekaClient;

    @Override
    public void preStart() throws Exception {
        registerWithEureka();
    }

    @Override
    public void postStop() throws Exception {
        unRegisterWithEureka();
    }

    @Override
    public void onReceive(final Object o) throws Exception {
        if (o instanceof MessageProto.Message) {

            MessageProto.Message message = (MessageProto.Message) o;

            System.out.println("Hello " + message.getDescription());

            getSender().tell(MessageProto.ResultMessage.newBuilder()
                    .setId(message.getId())
                    .setStatus(MessageProto.ResultMessage.Status.OK).build(), getSelf());
        } else {
            unhandled(o);
        }
    }


    private void registerWithEureka() {
        applicationInfoManager = ApplicationInfoManager.getInstance();

        DiscoveryManager.getInstance().initComponent(
                new MyDataCenterInstanceConfig(),
                new DefaultEurekaClientConfig());

        eurekaClient = DiscoveryManager.getInstance().getEurekaClient();

        System.out.println("Registering service to eureka with STARTING status");
        applicationInfoManager.setInstanceStatus(InstanceInfo.InstanceStatus.STARTING);

        System.out.println("Simulating service initialization by sleeping for 2 seconds...");
        // Now we change our status to UP
        System.out.println("Done sleeping, now changing status to UP");
        applicationInfoManager.setInstanceStatus(InstanceInfo.InstanceStatus.UP);
        waitForRegistrationWithEureka(eurekaClient);
        System.out.println("Service started and ready to process requests..");

    }

    private void unRegisterWithEureka() {
        DiscoveryManager.getInstance().shutdownComponent();
    }


    private void waitForRegistrationWithEureka(EurekaClient eurekaClient) {
        // my vip address to listen on
        String vipAddress = "akka.idtrust.com.br";
        InstanceInfo nextServerInfo = null;
        while (nextServerInfo == null) {
            try {
                nextServerInfo = eurekaClient.getNextServerFromEureka(vipAddress, false);
            } catch (Throwable e) {
                System.out.println("Waiting ... verifying service registration with eureka ...");

                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}

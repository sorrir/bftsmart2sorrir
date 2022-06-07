package bftsmart.mvptools.replica;

import bftsmart.tom.MessageContext;
import bftsmart.tom.ReplicaContext;
import bftsmart.tom.ServiceReplica;
import bftsmart.tom.core.messages.TOMMessage;
import bftsmart.tom.server.Replier;
import bftsmart.tom.server.defaultservices.DefaultRecoverable;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Replica extends DefaultRecoverable implements Replier {

    private ReplicaContext rc;
    ReplicaForwarderThread forwarderThread;
    String configHome;

    public Replica(int id, ReplicaForwarderThread forwarderThread, String configHome) {
        this.forwarderThread = forwarderThread;
        this.configHome = configHome;
        new ServiceReplica(id, this, this, this, configHome);
        forwarderThread.start();
    }

    public static void main(String[] args) {
        System.out.println("Replica, v0.1bench");
        if (args.length < 3) {
            System.out.println("Use: java Replica <processId> <Port> <configPath>");
            System.exit(-1);
        }

        System.out.println("BFT-SMaRt -- Debugging for deployment ..");

        String configPath = args[2];
        ReplicaForwarderThread forwarderThread = new ReplicaForwarderThread(Integer.parseInt(args[1]));
        new Replica(Integer.parseInt(args[0]), forwarderThread, configPath);
    }

    @Override
    public byte[] appExecuteUnordered(byte[] command, MessageContext msgCtx) {
        return new byte[0];
    }

    private byte[] execute(byte[] command, MessageContext msgCtx) {
        // System.out.println("Replica byteLength command " + command.length + " " + command);
        Charset charset = StandardCharsets.UTF_8;
        ByteBuffer buf = ByteBuffer.wrap(command);
        String request = charset.decode(buf).toString();
       // System.out.println("Replica received request:" + request);
        forwarderThread.send(request);
        return command;
    }

    @Override
    public void installSnapshot(byte[] state) {
    }

    @Override
    public byte[] getSnapshot() {
        return new byte[0];
    }

    @Override
    public byte[][] appExecuteBatch(byte[][] commands, MessageContext[] msgCtxs, boolean fromConsensus) {
        byte[][] replies = new byte[commands.length][];
        for (int i = 0; i < commands.length; i++) {
            replies[i] = execute(commands[i], msgCtxs[i]);
        }
        return replies;
    }

    @Override
    public void manageReply(TOMMessage request, MessageContext msgCtx) {
        // do not reply
        //  system.numrepliers = 0 in config or this method will not be called?
    }

}



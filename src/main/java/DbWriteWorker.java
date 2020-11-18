import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class DbWriteWorker implements Runnable {
    private final static String QUEUE_NAME = "DB_POST";

    @Override
    public void run() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = null;
        try {
            connection = factory.newConnection();
        } catch (IOException | TimeoutException e) {
            // TODO: Make more useful
            System.err.println("problem connecting to RabbitMQ");
            e.printStackTrace();
            System.exit(1);
        }

        Channel channel = null;
        try {
            channel = connection.createChannel();
        } catch (IOException e) {
            // TODO: Make more useful
            System.err.println("problem connecting to RabbitMQ");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        } catch (IOException e) {
            System.err.println("problem reading from RabbitMQ");
            e.printStackTrace();
        }

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] LiftRide{ " + message + " }");
        };
        try {
            channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {});
        } catch (IOException e) {
            System.err.println("problem reading from RabbitMQ");
            e.printStackTrace();
        }
    }
}

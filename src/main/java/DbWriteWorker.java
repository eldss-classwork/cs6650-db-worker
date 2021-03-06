import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import data.SkierDbConnection;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.concurrent.TimeoutException;

public class DbWriteWorker implements Runnable {

    private final static String QUEUE_NAME = "DB_POST_DURABLE";
    private final SkierDbConnection dbConn = new SkierDbConnection();

    @Override
    public void run() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");  // Can stay localhost on server, same machine
        Connection connection = null;
        try {
            connection = factory.newConnection();
        } catch (IOException | TimeoutException e) {
            // TODO: Make more useful
            System.err.println("problem connecting to RabbitMQ");
            e.printStackTrace();
            return;
        }

        try {
            Channel channel = connection.createChannel();
            boolean durable = true;
            channel.queueDeclare(QUEUE_NAME, durable, false, false, null);

            // Ensure messages are distributed by workload
            int prefetchCount = 1;
            channel.basicQos(prefetchCount);

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {

                // Form of "resort day skier time lift"
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                //System.out.println(" [x] Received msg: " + message);  // TODO: make this a log w/ DEBUG level
                // Extract into parts
                String[] parts = message.split(",");

                // Write to the database
                try {
                    String resort = parts[0];
                    int day = Integer.parseInt(parts[1]);
                    int skier = Integer.parseInt(parts[2]);
                    int time = Integer.parseInt(parts[3]);
                    int lift = Integer.parseInt(parts[4]);
                    dbConn.postLiftRide(resort, day, skier, time, lift);

                } catch (SQLException e) {
                    System.err.println("problem posting to database");
                    e.printStackTrace();

                } finally {
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                }
            };

            // Consume messages as they come in
            boolean autoAck = false;
            channel.basicConsume(QUEUE_NAME, autoAck, deliverCallback, consumerTag -> { });

            // Catch channel based exceptions
        } catch (IOException e) {
            // TODO: Make more useful
            System.err.println("problem connecting to RabbitMQ");
            e.printStackTrace();
            System.exit(1);
        }
    }
}

package org.ikasan.component.endpoint.bigqueue.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leansoft.bigqueue.BigQueueImpl;
import com.leansoft.bigqueue.IBigQueue;
import com.leansoft.bigqueue.IBigQueue.ItemIterator;
import org.apache.commons.io.FileUtils;
import org.ikasan.component.endpoint.bigqueue.message.BigQueueMessageImpl;
import org.ikasan.spec.bigqueue.message.BigQueueMessage;
import org.ikasan.spec.bigqueue.service.BigQueueManagementService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Implementation of a BigQueue management service.
 *
 * @author Ikasan Development Team
 */
 // TODO REMOVE THIS WHEN READY, replaced with AbstractBigQueueManagementService
public class BigQueueManagementServiceImpl implements BigQueueManagementService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Get the size of a given queue.
     * Ensures that the queue exists before finding out the size of the queue, otherwise retuns 0
     * @param queueDir - the directory where the queue exists
     * @param queueName - the name of the queue to inspect
     */

    public synchronized long size(String queueDir, String queueName) throws IOException {
        if (queueExists(queueDir, queueName)) {
            IBigQueue bigQueue = new BigQueueImpl(queueDir, queueName);
            return bigQueue.size();
        }
        return 0;
    }

    /**
     * Get the first message off the queue.
     * Ensures that the queue exists before doing a peek of the queue. Returns null otherwise or if nothing on the queue.
     * @param queueDir - the directory where the queue exists
     * @param queueName - the name of the queue to inspect
     */

    public synchronized BigQueueMessage peek(String queueDir, String queueName) throws IOException {
        if (queueExists(queueDir, queueName)) {
            IBigQueue bigQueue = new BigQueueImpl(queueDir, queueName);
            byte[] peek = bigQueue.peek();
            if (peek != null) {
                return MAPPER.readValue(peek, BigQueueMessageImpl.class);
            }
        }
        return null;
    }

    /**
     * Deletes a message off the queue.
     * Ensures that the queue exists and the messageId is in the queue before looping i.e. dequeue and enqueue without
     * enqueuing the message to be deleted.
     * @param queueDir - the directory where the queue exists
     * @param queueName - the name of the queue to inspect
     * @param biQueueMessageId - the message id of the big queue message to delete
     */

    public synchronized void deleteMessage(String queueDir, String queueName, String biQueueMessageId) throws IOException {
        if (biQueueMessageId != null
            && queueExists(queueDir, queueName)
            && messageIdExistsInMessages(queueDir, queueName, biQueueMessageId)) {
            IBigQueue bigQueue = new BigQueueImpl(queueDir, queueName);
            long size = bigQueue.size();
            for (int i = 0; i < size; i++) {
                byte[] dequeue = bigQueue.dequeue();
                BigQueueMessage message = MAPPER.readValue(dequeue, BigQueueMessageImpl.class);
                if (!biQueueMessageId.equals(message.getMessageId())) {
                    bigQueue.enqueue(dequeue);
                }
            }
        }
    }

    /**
     * Deletes all message off the queue.
     * Will check if the queue exist and then it will action removeAll() method from BigQueueImpl
     * @param queueDir - the directory where the queue exists
     * @param queueName - the name of the queue to inspect
     * @throws IOException exception throws if there is any IO error during dequeue operation.
     */

    public synchronized void deleteAllMessage(String queueDir, String queueName) throws IOException {
        if (queueExists(queueDir, queueName)) {
            IBigQueue bigQueue = new BigQueueImpl(queueDir, queueName);
            bigQueue.removeAll();
        }
    }

    /**
     * Get a list of the directories in the queue directory.
     * Ensures that the queue exists before returning the list of directory names under the queue directory.
     * Returns empty list otherwise.
     * @param queueDir - the directory where the queue exists
     */
    @Override
    public synchronized List<String> listQueues(String queueDir) throws IOException {
        List<String> queueNames = new ArrayList<>();
        if (queueDir != null && Files.exists(Path.of(queueDir))) {
            File[] directories = new File(queueDir).listFiles(File::isDirectory);
            if (directories != null) {
                Arrays.stream(directories).forEach(file -> queueNames.add(file.getName()));
            }
        }

        return queueNames;
    }

    /**
     * Deletes a queue directory.
     * @param queueDir - the directory name
     * @param queueName - the name of the queue
     */
    @Override
    public void deleteQueue(String queueDir, String queueName) throws IOException {
        if (queueExists(queueDir, queueName)) {
            FileUtils.forceDelete(new File(queueDir + File.separator + queueName));
        }
    }

    /**
     * Get the messages on the queue.
     * Ensures that the queue exists before returning the list of messages. Returns empty list otherwise.
     * @param queueDir - the directory where the queue exists
     * @param queueName - the name of the queue to inspect
     */

    public synchronized List<BigQueueMessage> getMessages(String queueDir, String queueName) throws IOException {
        if (queueExists(queueDir, queueName)) {
            IBigQueue bigQueue = new BigQueueImpl(queueDir, queueName);
            MessagesIterator messagesIterator = new MessagesIterator();
            bigQueue.applyForEach(messagesIterator);
            return messagesIterator.getMessages();
        }

        return Collections.emptyList();
    }

    private class MessagesIterator implements ItemIterator {
        List<BigQueueMessage> messages = new ArrayList<>();

        @Override
        public void forEach(byte[] item) throws IOException {
            messages.add(MAPPER.readValue(item, BigQueueMessageImpl.class));
        }

        public List<BigQueueMessage> getMessages() {
            return messages;
        }
    }

    private boolean queueExists(String queueDir, String queueName) {
        return Files.exists(Paths.get(queueDir + File.separator + queueName));
    }

    private boolean messageIdExistsInMessages(String queueDir, String queueName, String biQueueMessageId) throws IOException {
        return getMessages(queueDir, queueName).stream().anyMatch(m -> biQueueMessageId.equals(m.getMessageId()));
    }

    @Override
    public long size(String queueName) {
        return 0;
    }

    @Override
    public BigQueueMessage peek(String queueName) throws IOException {
        return null;
    }

    @Override
    public void deleteMessage(String queueName, String biQueueMessageId) throws IOException { }


    @Override
    public void deleteAllMessage(String queueName) throws IOException { }

    @Override
    public List<BigQueueMessage> getMessages(String queueName) throws IOException {
        return null;
    }
}
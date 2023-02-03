package com.jacker.plugin.music.player.audio.player;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Actor {
    private final Logger logger = Logger.getLogger(getClass().getName());

    public enum Message {
        // player messages
        PLAY, PAUSE, STOP, FLUSH,
        // buffer messages
        OPEN, SEEK;

        private Object[] params;

        public Object[] getParams() {
            return params;
        }

        public void setParams(Object[] params) {
            this.params = params;
        }
    }

    private final BlockingQueue<Message> queue = new LinkedBlockingDeque<>();

    public synchronized void send(Message message, Object... params) {
        message.setParams(params);
        queue.add(message);
    }

    protected Actor() {
        Thread messageThread = new Thread(() -> {
                while (true) {
                    Message message = null;
                    try {
                        message = queue.take();
                        process(message);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Error processing message " + message, e);
                    }
                }
        }, "Actor Thread");
        messageThread.start();
    }

    protected abstract void process(Message message);
}

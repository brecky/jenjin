package com.jenjinstudios.client.net;

import java.io.IOException;
import java.util.TimerTask;

/**
 * The ClientLoop class is essentially what amounts to the output thread.
 *
 * @author Caleb Brinkman
 */

class ClientLoop extends TimerTask
{
    public static final int MAX_STORED_UPDATE_TIMES = 50;
    public static final int NANOS_TO_SECOND = 1000000000;
    /** The Client for this loop. */
    private final Client client;
    private int updateCount;
    private long lastStart = System.nanoTime();
    private final long[] updateTimesNanos = new long[MAX_STORED_UPDATE_TIMES];

    /**
     * Construct a ClientLoop for the given client.
     *
     * @param client The client for this ClientLoop
     */
    ClientLoop(Client client) {
        this.client = client;
    }

    @Override
    public void run() {
        updateCount++;
        saveUpdateTime();
        client.runRepeatedTasks();
        client.getExecutableMessageQueue().runQueuedExecutableMessages();
        try
        {
            client.getMessageIO().writeAllMessages();
        } catch (IOException e)
        {
            client.shutdown();
        }
    }

    private void saveUpdateTime() {
        long newStart = System.nanoTime();
        long timeElapsed = newStart - lastStart;
        lastStart = newStart;
        updateTimesNanos[updateCount % updateTimesNanos.length] = timeElapsed;
    }

    public double getAverageRunTime() {
        double maxIndex = Math.min(updateCount, updateTimesNanos.length);
        double total = 0;
        for (int i = 0; i < maxIndex; i++)
        {
            total += updateTimesNanos[i];
        }
        return (total / maxIndex) / NANOS_TO_SECOND;
    }

}
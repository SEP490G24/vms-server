package fpt.edu.capstone.vms.util;

import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;

@Slf4j
public class IpRequestCounter {

    private LinkedList<Long> requestTimestamps = new LinkedList<>();

    public int getRequestCount() {
        return requestTimestamps.size();
    }

    public void incrementRequestCount(long timestamp) {
        requestTimestamps.addLast(timestamp);
    }

    public void cleanupOldRequests(long cutoffTime) {
        while (!requestTimestamps.isEmpty() && requestTimestamps.getFirst() < cutoffTime) {
            requestTimestamps.removeFirst();
        }
        log.info(String.valueOf(requestTimestamps.size()));
    }

}

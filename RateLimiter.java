import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.*;

interface IRateLimitingStrategy{
    boolean isAllowed();
}

class TokenRateLimiter implements IRateLimitingStrategy{
    private String clientId;
    private final int rate;
    private long lastRequestTS;
    private int tokens;
    private int MAX_TOKENS;
    private final ReentrantLock lock;

    public TokenRateLimiter(String clientId, int rate, int MAX_TOKENS){
        this.clientId = clientId;
        this.rate = rate;
        this.tokens = MAX_TOKENS;
        this.lastRequestTS = System.currentTimeMillis();
        this.MAX_TOKENS = MAX_TOKENS;
        this.lock = new ReentrantLock();
    }

    @Override 
    public boolean isAllowed(){

        lock.lock();

        try{
            long now = System.currentTimeMillis();
            long timeElapsed = now - lastRequestTS;

            int tokensToAdd = (int) ((timeElapsed * rate)/1000);

            if(tokensToAdd > 0){
                tokens = Math.min(MAX_TOKENS, tokens + tokensToAdd);
                lastRequestTS += (tokensToAdd * 1000L)/rate;
            }

            if(tokens > 0){
                tokens--;
                return true;
            }
        }finally{
            lock.unlock();
        }


        return false;
    }
}

class SlidingWindowRateLimiter implements IRateLimitingStrategy{
    private String clientId;
    private final int requestLimit;
    private long windowTime;
    private final ReentrantLock lock;
    private final Queue<Long> requestQueue;

    public SlidingWindowRateLimiter(String clientId, int requestLimit, long windowTime){
        this.clientId = clientId;
        this.requestLimit = requestLimit;
        this.windowTime = windowTime;
        this.lock = new ReentrantLock();
        this.requestQueue = new LinkedList<>();
    }

    @Override 
    public boolean isAllowed(){
        lock.lock();

        try{
            long now = System.currentTimeMillis();

            while(!requestQueue.isEmpty() && requestQueue.peek() < now - windowTime)
                requestQueue.poll();

            if(requestQueue.size() < requestLimit){
                requestQueue.add(now);
                return true;
            }
        
        }finally{
            lock.unlock();
        }

        return false;
    }
}

public class RateLimiter{
    private ConcurrentHashMap<String, IRateLimitingStrategy> clientMap;

    public RateLimiter(){
        clientMap = new ConcurrentHashMap<>();
    }

    public void addClient(String clientId, IRateLimitingStrategy strategy){
        clientMap.computeIfAbsent(clientId, k->strategy);
    }

    public boolean isAllowed(String clientId){

        if(clientMap.containsKey(clientId)){
            IRateLimitingStrategy clientStrategy = clientMap.get(clientId);

            return clientStrategy.isAllowed();
        }

        return false;
    }

    public static void main(String[] args) {
    }
}
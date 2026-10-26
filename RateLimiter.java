import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

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
    @Override 
    public boolean isAllowed(){
        return true;
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
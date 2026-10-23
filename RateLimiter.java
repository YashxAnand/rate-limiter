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
        this.tokens = 0;
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
            lastRequestTS = now;

            int tokensToAdd = (int)Math.ceil((Math.floor(timeElapsed/60.0) * (rate/60.0)));
            tokens = Math.min(MAX_TOKENS, tokens + tokensToAdd);

            if(tokens > 0){
                tokens--;
                return true;
            }
        }catch(RuntimeException e){
            // Exception handling
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

    public static void main(String[] args) {
    }
}
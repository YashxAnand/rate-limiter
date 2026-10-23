import java.util.concurrent.ConcurrentHashMap;

interface IRateLimitingStrategy{
    boolean isAllowed();
}

class TokenRateLimiter implements IRateLimitingStrategy{
    @Override 
    public boolean isAllowed(){
        return true;
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
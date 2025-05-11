package debug;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class RedisDebugger {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    private final Environment env;

    @PostConstruct
    public void debug() {
        System.out.println("🔍 [RedisDebugger] Active profiles: " + Arrays.toString(env.getActiveProfiles()));
        System.out.println("🔍 [RedisDebugger] Redis Host: " + redisHost);
        System.out.println("🔍 [RedisDebugger] Redis Port: " + redisPort);
    }
}

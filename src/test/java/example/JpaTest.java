package example;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testng.Assert.assertEquals;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.testng.annotations.Test;

@SpringBootTest
@AutoConfigureMockMvc
public class JpaTest extends AbstractTestNGSpringContextTests {

    @Autowired
    PersonService personService;
    @Autowired
    HikariDataSource hikariDataSource;
    @Autowired
    MockMvc mockMvc;

    @Test
    public void testAssert() throws Exception {
        mockMvc.perform(get("/assert")).andExpect(status().isOk());
    }

    @Test
    public void testRealLifeUsage() throws InterruptedException {
        int maxConnections = hikariDataSource.getHikariConfigMXBean().getMaximumPoolSize();
        int threadCount = maxConnections + maxConnections / 2;
        String name = UUID.randomUUID().toString();
        Person person = personService.insert(name);

        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());
        UncaughtExceptionHandler handler = (t, e) -> errors.add(e);
        List<Thread> threads = IntStream.range(0, threadCount)
                .mapToObj(i -> new Thread(() -> {
                    try {
                        mockMvc.perform(post("/post?name=" + name)).andExpect(status().isOk());
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }))
                .collect(Collectors.toList());
        threads.forEach(t -> t.setUncaughtExceptionHandler(handler));
        threads.forEach(Thread::start);
        for (Thread t : threads) {
            t.join();
        }
        assertEquals(errors.size(), 0, errors.toString());
    }
}

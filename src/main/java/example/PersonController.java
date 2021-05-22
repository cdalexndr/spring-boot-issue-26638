package example;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class PersonController {

    @Autowired
    PersonService personService;
    @Autowired
    HikariDataSource hikariDataSource;

    @PostMapping("/assert")
    @ResponseStatus(value = HttpStatus.OK)
    public void getAssert() {
        personService.get("asd"); //transactional = acquire connection
        //should release here, but no...
        assert hikariDataSource.getHikariPoolMXBean().getActiveConnections() == 0;
    }

    @PostMapping("/post")
    @ResponseStatus(value = HttpStatus.OK)
    public void get(@RequestParam("name") String name) throws ExecutionException, InterruptedException {
        personService.get(name); //acquire connection
        //start async process that requires connection internally
        Future<Object> future = personService.updateCounterAsync(name);
        future.get(); //wait for process
    }
}
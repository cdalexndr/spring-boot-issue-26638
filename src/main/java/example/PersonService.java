package example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Service
public class PersonService {

    @Autowired
    private PersonRepository personRepository;
    @Autowired
    EntityManager entityManager;

    @Transactional
    public Person insert(String name) {
        return personRepository.save(new Person(name));
    }

    @Transactional
    public Person update(Person person) {
        return personRepository.save(person);
    }

    @Transactional(readOnly = true)
    public Optional<Person> get(String name) {
        return personRepository.getByName(name);
    }

    public Future<Object> updateCounterAsync(String name) {
        CompletableFuture<Object> future = new CompletableFuture<>();
        Thread thread = new Thread(() -> {
            try {
                Optional<Person> person = get(name);
                if (person.isPresent()) {
                    person.get().setCounter(person.get().getCounter() + 1);
                    update(person.get());
                }
                future.complete(null);
            } catch (Exception ex) {
                future.completeExceptionally(ex);
            }
        });
        thread.start();
        return future;
    }
}

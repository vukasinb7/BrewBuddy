package brewbuddy.services;

import brewbuddy.drools.DroolsHelper;
import brewbuddy.enums.BeerType;
import brewbuddy.events.Alarm;
import brewbuddy.events.Rating;
import brewbuddy.events.UserBeerLogger;
import brewbuddy.exceptions.BadRequestException;
import brewbuddy.models.Beer;
import brewbuddy.models.Brewery;
import brewbuddy.models.User;
import brewbuddy.exceptions.NotFoundException;
import brewbuddy.repositories.UserBeerLoggerRepository;
import brewbuddy.services.interfaces.IUserService;
import brewbuddy.repositories.UserRepository;
import org.drools.core.ClassObjectFilter;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final UserBeerLoggerRepository userBeerLoggerRepository;
    private final KieContainer kieContainer;

    @Autowired
    public UserService(KieContainer kieContainer,UserBeerLoggerRepository userBeerLoggerRepository, UserRepository userRepository) {
        this.userRepository = userRepository;
        this.kieContainer = kieContainer;
        this.userBeerLoggerRepository=userBeerLoggerRepository;
    }

    @Override
    public User get(Integer id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            return user.get();
        } else {
            throw new NotFoundException("User does not exist");
        }
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public User insert(User user) {
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("Email already in use");
        }
    }

    @Override
    public Boolean isUserDrunk(User user) {
        KieSession kieSession = kieContainer.newKieSession("beerKsession");

        // forwards
        kieSession.insert(user);
        for (UserBeerLogger usl : userBeerLoggerRepository.findAll()) {
            kieSession.insert(usl);
        }
        kieSession.getAgenda().getAgendaGroup("beerAlarm").setFocus();
        kieSession.fireAllRules();
        Collection<Alarm> alarms = (Collection<Alarm>) kieSession.getObjects(new ClassObjectFilter(Alarm.class));

        return !alarms.isEmpty();
    }
    @Override
    public List<User> mostPopularUsers(){
        KieSession kieSession = kieContainer.newKieSession("beerKsession");
        for (User user :userRepository.findAll()){
            kieSession.insert(user);
        }
        for (UserBeerLogger logger:userBeerLoggerRepository.findAll()){
            kieSession.insert(logger);
        }
        QueryResults results = kieSession.getQueryResults("Most Active Users");

        Map<User, Integer> beerCountMap = new HashMap<>();
        for (QueryResultsRow row : results) {
            User resultUser = (User) row.get("$user");
            Integer count = ((Long) row.get("$count")).intValue();
            beerCountMap.put(resultUser, count);
        }
        List<Map.Entry<User, Integer>> sortedEntries = new ArrayList<>(beerCountMap.entrySet());
        Collections.sort(sortedEntries, Map.Entry.comparingByValue(Comparator.reverseOrder()));

        List<User> sortedResultList = new ArrayList<>();
        for (Map.Entry<User, Integer> entry : sortedEntries) {
            sortedResultList.add(entry.getKey());
        }
        return sortedResultList;
    }

}

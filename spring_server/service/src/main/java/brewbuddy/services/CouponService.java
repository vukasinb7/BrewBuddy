package brewbuddy.services;

import brewbuddy.events.UserBeerLogger;
import brewbuddy.exceptions.NotFoundException;
import brewbuddy.models.*;
import brewbuddy.enums.CouponType;
import brewbuddy.repositories.CouponCriteriaRepository;
import brewbuddy.repositories.UserBeerLoggerRepository;
import brewbuddy.services.interfaces.IBeerService;
import brewbuddy.services.interfaces.ICouponService;
import brewbuddy.repositories.CouponRepository;
import brewbuddy.services.interfaces.IUserService;
import org.drools.template.DataProvider;
import org.drools.template.DataProviderCompiler;
import org.drools.template.objects.ArrayDataProvider;
import org.kie.api.KieBaseConfiguration;
import org.kie.api.KieServices;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.utils.KieHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CouponService implements ICouponService {
    private final CouponRepository couponRepository;
    private final IUserService userService;
    private final UserBeerLoggerRepository userBeerLoggerRepository;
    private final IBeerService beerService;
    private final KieContainer kieContainer;

    private final CouponCriteriaRepository couponCriteriaRepository;


    @Autowired
    public CouponService(KieContainer kieContainer, CouponCriteriaRepository couponCriteriaRepository, CouponRepository couponRepository, IUserService userService, UserBeerLoggerRepository userBeerLoggerRepository, IBeerService beerService) {
        this.couponRepository = couponRepository;
        this.kieContainer = kieContainer;
        this.userService = userService;
        this.userBeerLoggerRepository = userBeerLoggerRepository;
        this.beerService = beerService;
        this.couponCriteriaRepository = couponCriteriaRepository;
    }

    @Override
    public Coupon get(Integer id) {
        Optional<Coupon> coupon = couponRepository.findById(id);
        if (coupon.isPresent()) {
            return coupon.get();
        } else {
            throw new NotFoundException("Coupon does not exist");
        }
    }

    @Override
    public List<Coupon> getAll() {
        return couponRepository.findAll();
    }

    @Override
    public Coupon insert(Coupon coupon) {
        return couponRepository.save(coupon);
    }

    @Override
    public List<Coupon> getUserCoupons(User user) {
        return couponRepository.findCouponsByUser(user);
    }


    @Override
    public List<CouponCriteria> getCriterias(){
        CouponCriteria festivalCriteria = couponCriteriaRepository.findCouponCriteriaByType(CouponType.FESTIVAL);
        CouponCriteria breweryCriteria = couponCriteriaRepository.findCouponCriteriaByType(CouponType.BREWERY);
        CouponCriteria appCriteria = couponCriteriaRepository.findCouponCriteriaByType(CouponType.APPLICATION);
        List<CouponCriteria> criterias=new ArrayList<>();
        criterias.add(festivalCriteria);
        criterias.add(breweryCriteria);
        criterias.add(appCriteria);
        return criterias;
    }

    @Override
    public List<FestivalCoupon> createFestivalCoupon(Festival festival, CouponCriteria inputCriteria) {
        InputStream template = CouponService.class.getResourceAsStream("/rules/template/festivalCoupons.drt");
        CouponCriteria criteria = couponCriteriaRepository.findCouponCriteriaByType(CouponType.FESTIVAL);
        inputCriteria.setId(criteria.getId());
        criteria = couponCriteriaRepository.save(inputCriteria);

        DataProvider dataProvider = new ArrayDataProvider(new String[][]{
                new String[]{criteria.getMinBeers().toString(), criteria.getPercentage().toString(), criteria.getExpireIn().toString(), criteria.getDaysRange().toString()}
        });

        DataProviderCompiler converter = new DataProviderCompiler();
        String drl = converter.compile(dataProvider, template);

        System.out.println(drl);

        KieSession ksession = createKieSessionFromDRL(drl);

        ArrayList<FestivalCoupon> coupons = new ArrayList<>();
        ksession.setGlobal("coupons", coupons);
        ksession.insert(festival);
        for (User u : userService.getAll()) {
            ksession.insert(u);
        }
        for (UserBeerLogger logger : userBeerLoggerRepository.findAll()) {
            ksession.insert(logger);
        }


        int count = ksession.fireAllRules();
        coupons = (ArrayList<FestivalCoupon>) ksession.getGlobal("coupons");
        couponRepository.saveAll(coupons);

        return coupons;

    }

    @Override
    public List<BreweryCoupon> createBreweryCoupon(Brewery brewery, CouponCriteria inputCriteria) {
        InputStream template = CouponService.class.getResourceAsStream("/rules/template/breweryCoupons.drt");
        CouponCriteria criteria = couponCriteriaRepository.findCouponCriteriaByType(CouponType.BREWERY);
        inputCriteria.setId(criteria.getId());
        criteria = couponCriteriaRepository.save(inputCriteria);
        DataProvider dataProvider = new ArrayDataProvider(new String[][]{
                new String[]{criteria.getMinBeers().toString(), criteria.getPercentage().toString(), criteria.getExpireIn().toString(), criteria.getDaysRange().toString()}
        });

        DataProviderCompiler converter = new DataProviderCompiler();
        String drl = converter.compile(dataProvider, template);

        System.out.println(drl);

        KieSession ksession = createKieSessionFromDRL(drl);

        ArrayList<BreweryCoupon> coupons = new ArrayList<>();
        ksession.setGlobal("coupons", coupons);
        List<UserBeerLogger> loggers = userBeerLoggerRepository.findAll();
        ksession.insert(brewery);
        for (User u : userService.getAll()) {
            ksession.insert(u);
        }
        for (UserBeerLogger logger : userBeerLoggerRepository.findAll()) {
            ksession.insert(logger);
        }


        int count = ksession.fireAllRules();
        coupons = (ArrayList<BreweryCoupon>) ksession.getGlobal("coupons");
        couponRepository.saveAll(coupons);

        return coupons;

    }

    @Override
    public List<ApplicationCoupon> createAppCoupon(CouponCriteria inputCriteria) {
        InputStream template = CouponService.class.getResourceAsStream("/rules/template/appCoupons.drt");
        CouponCriteria criteria = couponCriteriaRepository.findCouponCriteriaByType(CouponType.APPLICATION);
        inputCriteria.setId(criteria.getId());
        criteria = couponCriteriaRepository.save(inputCriteria);
        DataProvider dataProvider = new ArrayDataProvider(new String[][]{
                new String[]{criteria.getMinBeers().toString(), criteria.getPercentage().toString(), criteria.getExpireIn().toString(), criteria.getDaysRange().toString()}
        });

        DataProviderCompiler converter = new DataProviderCompiler();
        String drl = converter.compile(dataProvider, template);

        System.out.println(drl);

        KieSession ksession = createKieSessionFromDRL(drl);

        ArrayList<ApplicationCoupon> coupons = new ArrayList<>();
        ksession.setGlobal("coupons", coupons);

        for (Beer b : beerService.getAll()) {
            ksession.insert(b);
        }
        for (User u : userService.getAll()) {
            ksession.insert(u);
        }
        for (UserBeerLogger logger : userBeerLoggerRepository.findAll()) {
            ksession.insert(logger);
        }


        int count = ksession.fireAllRules();
        coupons = (ArrayList<ApplicationCoupon>) ksession.getGlobal("coupons");
        couponRepository.saveAll(coupons);

        return coupons;

    }

    private KieSession createKieSessionFromDRL(String drl) {
        KieHelper kieHelper = new KieHelper();
        kieHelper.addContent(drl, ResourceType.DRL);

        Results results = kieHelper.verify();

        if (results.hasMessages(Message.Level.WARNING, Message.Level.ERROR)) {
            List<Message> messages = results.getMessages(Message.Level.WARNING, Message.Level.ERROR);
            for (Message message : messages) {
                System.out.println("Error: " + message.getText());
            }

            throw new IllegalStateException("Compilation errors were found. Check the logs.");
        }

        return kieHelper.build(createStreamModeKieBaseConfiguration()).newKieSession();
    }

    private KieBaseConfiguration createStreamModeKieBaseConfiguration() {
        KieServices kieServices = KieServices.Factory.get();
        KieBaseConfiguration config = kieServices.newKieBaseConfiguration();
        config.setOption(EventProcessingOption.STREAM);
        return config;
    }
}

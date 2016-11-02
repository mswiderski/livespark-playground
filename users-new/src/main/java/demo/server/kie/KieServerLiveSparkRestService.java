package demo.server.kie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.command.KieCommands;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.ProcessServicesClient;
import org.kie.server.client.RuleServicesClient;
import org.livespark.formmodeler.rendering.client.shared.LiveSparkRestService;
import org.livespark.formmodeler.rendering.client.shared.query.QueryCriteria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Decorator
public class KieServerLiveSparkRestService implements LiveSparkRestService {

    private static final Logger logger = LoggerFactory.getLogger(KieServerLiveSparkRestService.class);

    private static final String CONTAINER_ID = System.getProperty("containerId", "demo");
    private static final String PROCESS_ID = System.getProperty("processId", "createPerson");
    private static final String URL = System.getProperty("url", "http://localhost:8230/kie-server/services/rest/server");
    private static final String USER = System.getProperty("username", "kieserver");
    private static final String PASSWORD = System.getProperty("password", "kieserver1!");

    private KieCommands factory = KieServices.Factory.get().getCommands();

    @Inject
    @Delegate
    private LiveSparkRestService liveSparkRestService;

    private KieServicesClient client;

    @PostConstruct
    public void prepare() {

        KieServicesConfiguration conf = KieServicesFactory.newRestConfiguration(URL, USER, PASSWORD);
        conf.setCapabilities(Arrays.asList(KieServerConstants.CAPABILITY_BPM, KieServerConstants.CAPABILITY_BRM));
        client = KieServicesFactory.newKieServicesClient(conf);

    }

    @Override
    public Object create(Object model) {
        logger.info("About to create {}", model);
        ProcessServicesClient processClient = client.getServicesClient(ProcessServicesClient.class);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put(model.getClass().getSimpleName().toLowerCase(), model);
        long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID, parameters);

        logger.info("About to create {} with process {} in kie server", model, processInstanceId);

        return model;
    }

    @Override
    public List load() {
        return liveSparkRestService.load();
    }

    @Override
    public List load(int start, int end) {
        return liveSparkRestService.load(start, end);
    }

    @Override
    public List list(QueryCriteria criteria) {
        return liveSparkRestService.list(criteria);
    }

    @Override
    public Boolean update(Object model) {
        return liveSparkRestService.update(model);
    }

    @Override
    public Boolean delete(Object model) {
        logger.info("About to delete model {}", model);

        List<Command> commands = new ArrayList<>();
        BatchExecutionCommand command = factory.newBatchExecution(commands, "demo");
        commands.add(factory.newInsert(model, "model"));
        commands.add(factory.newGetObjects("objects"));

        RuleServicesClient ruleClient = client.getServicesClient(RuleServicesClient.class);
        ServiceResponse<ExecutionResults> results = ruleClient.executeCommandsWithResults(CONTAINER_ID, command);
        logger.info("Approval by rules response {}", results);
        if (results.getType().equals(ServiceResponse.ResponseType.SUCCESS)) {

            ExecutionResults data = results.getResult();
            Collection objects = (Collection) data.getValue("objects");

            Boolean approved = (Boolean)objects.stream().filter(o-> o instanceof Boolean).findFirst().orElse(false);
            logger.info("Rules approved the deletion {}", approved);
            if (approved) {
                return liveSparkRestService.delete(model);
            }
        }
        logger.info("Rules did not approve the deletion");
        return false;
    }
}

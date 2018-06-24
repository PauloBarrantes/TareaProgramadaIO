package DBMS_Sim.SourceCode;

import java.util.*;

/**
 * Esta clase consiste en el cuerpo de la simulación del
 * DBMS, donde simulamos el paso de las consultas por todos los módulos.
 * Guardando los datos estadísticos para generar mejores resultados a la hora
 * de optimizar el sistema.
 *
 * @author  Paulo Barrantes
 * @author  André Flasterstein
 * @author  Fabián Álvarez
 */
public class Simulator {

    private ClientAdminModule clientAdminModule;
    private double clock;
    private ExecutionModule executionModule;
    private ProcessAdminModule processAdminModule;
    private QueryGenerator queryGenerator;
    private QueryProcessingModule queryProcessingModule;
    private double runningTime;
    private StatisticsGenerator statisticsGenerator;
    private PriorityQueue<Event> tableOfEvents;
    private TransactionAndStorageModule transactionAndStorageModule;

    // ---------------------------------------------------------------------------------------------
    // ----------------------------- Beginning of constructors section -----------------------------
    // ---------------------------------------------------------------------------------------------
    public Simulator(int k, double t, int n, int p, int m){

        Comparator<Event> comparator = (event1, event2) -> {
            int cmp = 0;
            if (event1.getTime() < event2.getTime()) {
                cmp = -1;
            } else {
                if (event1.getTime() > event2.getTime()) {
                    cmp = 1;
                }
            }
            return cmp;
        };
        tableOfEvents = new PriorityQueue<>(100, comparator);
        clientAdminModule =  new ClientAdminModule(k, t);
        processAdminModule = new ProcessAdminModule(1,t);
        queryProcessingModule =  new QueryProcessingModule(n,t);
        transactionAndStorageModule = new TransactionAndStorageModule(p,t);
        executionModule = new ExecutionModule(m,t);
        queryGenerator = new QueryGenerator();
        statisticsGenerator = new StatisticsGenerator();
    }

    // ---------------------------------------------------------------------------------------------
    // ------------------------------ End of the constructors section ------------------------------
    // ---------------------------------------------------------------------------------------------



    // ---------------------------------------------------------------------------------------------
    // ----------------------- Beginning of the setters and getters section -----------------------
    // ---------------------------------------------------------------------------------------------
    public void setRunningTime(double runningTime) {
        this.runningTime = runningTime;
    }

    public double getRunningTime() {
        return runningTime;
    }

    public void setClock(double clock) {
        this.clock = clock;
    }

    public ClientAdminModule getClientAdminModule() {
        return clientAdminModule;
    }

    public double getClock() {
        return clock;
    }
    // ---------------------------------------------------------------------------------------------
    // ------------------------------ End of the constructors section ------------------------------
    // ---------------------------------------------------------------------------------------------


    // ---------------------------------------------------------------------------------------------
    // ------------------------------- Beginning of methods section -------------------------------
    // ---------------------------------------------------------------------------------------------



    public void appendInitialEvent(){
        Query initialQuery = queryGenerator.generate(0);
        initialQuery.setSubmissionTime(0);
        Event initialArrive = new Event(EventType.ArriveClientToModule,0, initialQuery);
        tableOfEvents.add(initialArrive);
    }
    public static void main(String args[]){
        Simulator simulator = new Simulator(15,15,3,2,1);
        simulator.setRunningTime(1500);
        simulator.simulate();
        simulator.getSimulationStatistics();
    }
    public void simulate(){
        appendInitialEvent();
        Event actualEvent;

        while(clock <= runningTime){
            actualEvent = tableOfEvents.poll();

            checkQueues(actualEvent.getTime());
            whereIgo(actualEvent);

            assert tableOfEvents.peek() != null;
            clock = tableOfEvents.peek().getTime();

        }

    }


    public double[] iterateSimulation(){
        double data []= new double[7];

        Event actualEvent =  tableOfEvents.poll();

        checkQueues(actualEvent.getTime());
        whereIgo(actualEvent);

        assert tableOfEvents.peek() != null;
        clock = tableOfEvents.peek().getTime();


        data[0] = clock;
        data[1] = (double) processAdminModule.queueSize();
        data[2] = (double) queryProcessingModule.queueSize();
        data[3] = (double) transactionAndStorageModule.queueSize();
        data[4] = (double) executionModule.queueSize();
        data[5] = (double) clientAdminModule.getDiscardedConnections();
        data[6] = (double) clientAdminModule.getTimeOutConnections();

        return data;
    }

    private void checkQueues(double clock){
        processAdminModule.checkQueue(clock,clientAdminModule);
        queryProcessingModule.checkQueue(clock,clientAdminModule);
        transactionAndStorageModule.checkQueue(clock, clientAdminModule);
        executionModule.checkQueue(clock, clientAdminModule);
    }

    ///CAMBIAR NOMBRE
    private void whereIgo(Event actualEvent){
        boolean queryTimeOut;
        switch (actualEvent.getType()) {
            case ArriveClientToModule:
                System.out.println("Hace una llegada");
                clientAdminModule.processArrival(actualEvent, tableOfEvents);
                break;
            case ArriveToProcessAdminModule:
                queryTimeOut = processAdminModule.processArrival(actualEvent, tableOfEvents);
                if(queryTimeOut){
                    clientAdminModule.timedOutConnection(actualEvent.getTime(), actualEvent.getQuery());
                }
                break;
            case ArriveToQueryProcessingModule:
                queryTimeOut =  queryProcessingModule.processArrival(actualEvent, tableOfEvents);
                if(queryTimeOut){
                    clientAdminModule.timedOutConnection(actualEvent.getTime(), actualEvent.getQuery());
                }
                break;
            case ArriveToTransactionModule:
                queryTimeOut = transactionAndStorageModule.processArrival(actualEvent, tableOfEvents);
                if(queryTimeOut){
                    clientAdminModule.timedOutConnection(actualEvent.getTime(), actualEvent.getQuery());
                }
                break;
            case ArriveToExecutionModule:
                queryTimeOut = executionModule.processArrival(actualEvent, tableOfEvents);
                if(queryTimeOut){
                    clientAdminModule.timedOutConnection(actualEvent.getTime(), actualEvent.getQuery());
                }
                break;
            case ShowResult:
                queryTimeOut = clientAdminModule.showResult(actualEvent, tableOfEvents);
                if(queryTimeOut){
                    clientAdminModule.timedOutConnection(actualEvent.getTime(), actualEvent.getQuery());
                }
                break;
            case ExitClientModule:
                queryTimeOut = clientAdminModule.processDeparture(actualEvent, tableOfEvents);
                if(queryTimeOut){
                    clientAdminModule.timedOutConnection(actualEvent.getTime(), actualEvent.getQuery());
                }
                break;
            case ExitProcessAdminModule:
                queryTimeOut= processAdminModule.processDeparture(actualEvent, tableOfEvents);
                if(queryTimeOut){
                    clientAdminModule.timedOutConnection(actualEvent.getTime(), actualEvent.getQuery());
                }
                break;
            case ExecuteQuery:
                queryTimeOut = executionModule.executeQuery(actualEvent, tableOfEvents);
                if(queryTimeOut){
                    clientAdminModule.timedOutConnection(actualEvent.getTime(), actualEvent.getQuery());
                }
                break;
            case ExitTransactionModule:
                queryTimeOut = transactionAndStorageModule.processDeparture(actualEvent, tableOfEvents);
                if(queryTimeOut){
                    clientAdminModule.timedOutConnection(actualEvent.getTime(), actualEvent.getQuery());
                }
                break;
            case ExitExecutionModule:
                queryTimeOut = executionModule.processDeparture(actualEvent, tableOfEvents);
                if(queryTimeOut){
                    clientAdminModule.timedOutConnection(actualEvent.getTime(), actualEvent.getQuery());
                }
                break;
            case ExitQueryProcessingModule:
                queryTimeOut = queryProcessingModule.processDeparture(actualEvent, tableOfEvents);
                if(queryTimeOut){
                    clientAdminModule.timedOutConnection(actualEvent.getTime(), actualEvent.getQuery());
                }
                break;
            case LexicalValidation:
                queryTimeOut = queryProcessingModule.lexicalValidation(actualEvent, tableOfEvents);
                if(queryTimeOut){
                    clientAdminModule.timedOutConnection(actualEvent.getTime(), actualEvent.getQuery());
                }
                break;
            case SintacticalValidation:
                queryTimeOut = queryProcessingModule.sintacticalValidation(actualEvent, tableOfEvents);
                if(queryTimeOut){
                    clientAdminModule.timedOutConnection(actualEvent.getTime(), actualEvent.getQuery());
                }
                break;
            case SemanticValidation:
                queryTimeOut = queryProcessingModule.semanticValidation(actualEvent, tableOfEvents);
                if(queryTimeOut){
                    clientAdminModule.timedOutConnection(actualEvent.getTime(), actualEvent.getQuery());
                }

                break;
            case PermissionVerification:
                queryTimeOut =queryProcessingModule.permissionVerification(actualEvent, tableOfEvents);
                if(queryTimeOut){
                    clientAdminModule.timedOutConnection(actualEvent.getTime(), actualEvent.getQuery());
                }

                break;
            case QueryOptimization:
                queryTimeOut =queryProcessingModule.queryOptimization(actualEvent, tableOfEvents);
                if(queryTimeOut){
                    clientAdminModule.timedOutConnection(actualEvent.getTime(), actualEvent.getQuery());
                }
                break;
        }
    }

    // ---------------------------------------------------------------------------------------------
    // ------------------------------- Statistical--------------------------------------------------
    // ---------------------------------------------------------------------------------------------



    public SimulationStatistics getSimulationStatistics(){
        SimulationStatistics simulationStatistics = new SimulationStatistics();

        simulationStatistics.setAcumulatedDiscardedConnections(clientAdminModule.getDiscardedConnections());
        statisticsGenerator.addDiscardedConnections(simulationStatistics.getAcumulatedDiscardedConnections());

        double[] acumulatedModuleQueueLength = new double[ModuleType.NUMMODULETYPES];
        acumulatedModuleQueueLength[ModuleType.CLIENTADMIN] = 0;

        acumulatedModuleQueueLength[ModuleType.PROCESSADMIN] = statisticsGenerator.queueLengthAverage(processAdminModule.getAcumulatedQueueLength(),processAdminModule.getCallsToQueueLength(),ModuleType.PROCESSADMIN);

        acumulatedModuleQueueLength[ModuleType.QUERYPROCESSING] = statisticsGenerator.queueLengthAverage(queryProcessingModule.getAcumulatedQueueLength(),queryProcessingModule.getCallsToQueueLength(),ModuleType.QUERYPROCESSING);
        acumulatedModuleQueueLength[ModuleType.TRANSACTIONANDSTORAGE] = statisticsGenerator.queueLengthAverage(transactionAndStorageModule.getAcumulatedQueueLength(),transactionAndStorageModule.getCallsToQueueLength(),ModuleType.TRANSACTIONANDSTORAGE);
        acumulatedModuleQueueLength[ModuleType.EXECUTION] = statisticsGenerator.queueLengthAverage(executionModule.getAcumulatedQueueLength(),executionModule.getCallsToQueueLength(),ModuleType.EXECUTION);
        simulationStatistics.setAcumulatedModuleQueueLength(acumulatedModuleQueueLength);

        double[][] acumulatedQueriesWaitTimeInModule = new double[ModuleType.NUMMODULETYPES][StatementType.NUMSTATEMENTS];
        acumulatedQueriesWaitTimeInModule[ModuleType.CLIENTADMIN] = statisticsGenerator.averagePassedTimeByStatementInModule(clientAdminModule.getTotalConnectionsByQueryType(),clientAdminModule.getTimeByQueryType(),ModuleType.CLIENTADMIN);
        acumulatedQueriesWaitTimeInModule[ModuleType.PROCESSADMIN] = statisticsGenerator.averagePassedTimeByStatementInModule(processAdminModule.getTotalConnectionsByQueryType(),processAdminModule.getTimeByQueryType(),ModuleType.PROCESSADMIN);
        acumulatedQueriesWaitTimeInModule[ModuleType.QUERYPROCESSING] = statisticsGenerator.averagePassedTimeByStatementInModule(queryProcessingModule.getTotalConnectionsByQueryType(),queryProcessingModule.getTimeByQueryType(),ModuleType.QUERYPROCESSING);
        acumulatedQueriesWaitTimeInModule[ModuleType.TRANSACTIONANDSTORAGE] = statisticsGenerator.averagePassedTimeByStatementInModule(transactionAndStorageModule.getTotalConnectionsByQueryType(),transactionAndStorageModule.getTimeByQueryType(),ModuleType.TRANSACTIONANDSTORAGE);
        acumulatedQueriesWaitTimeInModule[ModuleType.EXECUTION] = statisticsGenerator.averagePassedTimeByStatementInModule(executionModule.getTotalConnectionsByQueryType(),executionModule.getTimeByQueryType(),ModuleType.EXECUTION);
        simulationStatistics.setAcumulatedQueriesWaitTimeInModule(acumulatedQueriesWaitTimeInModule);

        simulationStatistics.setAcumulatedConnectionTime(statisticsGenerator.averageConnectionTime(clientAdminModule.getFinishedQueriesCounter(),clientAdminModule.getAccumulatedFinishedQueryTimes()));

        statisticsGenerator.increaseDoneSimulations();
        return simulationStatistics;
    }

    public SimulationStatistics finalStatistics(){
        SimulationStatistics simulationStatistics = new SimulationStatistics();
        simulationStatistics.setAcumulatedConnectionTime(statisticsGenerator.getdAverageConnectionTime());
        simulationStatistics.setAcumulatedQueriesWaitTimeInModule(statisticsGenerator.getAverageQueriesWaitedTimeInModule());
        simulationStatistics.setAcumulatedModuleQueueLength(statisticsGenerator.getAverageModuleQueueLength());
        simulationStatistics.setAcumulatedDiscardedConnections(statisticsGenerator.getAcumulatedDiscardedConnections());

        return simulationStatistics;
    }
    // ---------------------------------------------------------------------------------------------
    // -------------------------------- End of the methods section --------------------------------
    // ---------------------------------------------------------------------------------------------


}
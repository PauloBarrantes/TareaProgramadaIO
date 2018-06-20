package DBMS_Sim.SourceCode;

import java.util.PriorityQueue;

public class ExecutionModule extends Module{

    // ---------------------------------------------------------------------------------------------
    // ----------------------------- Beginning of constructors section -----------------------------
    // ---------------------------------------------------------------------------------------------

    public ExecutionModule(int maxFields, double timeout){
        super(maxFields,0,new PriorityQueue<Query>(),new double[NUMSTATEMENTS],timeout,new int[NUMSTATEMENTS]);
    }

    // ---------------------------------------------------------------------------------------------
    // ------------------------------ End of the constructors section ------------------------------
    // ---------------------------------------------------------------------------------------------



    // ---------------------------------------------------------------------------------------------
    // ------------------------------- Beginning of methods section -------------------------------
    // ---------------------------------------------------------------------------------------------

    /**
     * @param event, object that contains the information needed to execute each of the event types.
     * @param tableOfEvents, queue with a list of events to be executed.
     * @return  boolean that says if a query was removed, so other modules can also update their stats.
     * @function if there is space validates the query that arrived and send it to the exit window, else it is place on hold.
     */
    public boolean executeQuey(Event event, PriorityQueue<Event> tableOfEvents){
        boolean removedQuery = removeQuery(event.getTime(),event.getQuery());

        if(!removedQuery) {
            event.setType(EventType.ExitExecutionModule);

            Query query = event.getQuery();
            double time = 0.0;
            if(query.getStatementType() == DDL){
                time = 0.5;
            }else{
                if(query.getStatementType() == UPDATE){
                    time = 1.0;
                }else{
                    time = query.getLoadedBlocks()*query.getLoadedBlocks()*0.001;
                }
            }
            event.setTime(event.getTime() + time);

            tableOfEvents.add(event);
            System.out.println("Exec query");
            System.out.println(event.toString());
        }else{
            countStayedTime(event.getTime(),event.getQuery());
            addQueryInQueue(event.getTime(),tableOfEvents,EventType.LexicalValidation);
        }

        return removedQuery;
    }

    /**
     * @param event, object that contains the information needed to execute each of the event types.
     * @param tableOfEvents, queue with a list of events to be executed.
     * @return  boolean that says if a query was removed, so other modules can also update their stats.
     * @function send the query to the client admin module.
     */
    public boolean processDeparture(Event event, PriorityQueue<Event> tableOfEvents) {
        boolean removedQuery = removeQuery(event.getTime(),event.getQuery());

        if(!removedQuery){
            event.setType(EventType.ShowResult);
            tableOfEvents.add(event);
            --occupiedFields;
        }

        addQueryInQueue(event.getTime(),tableOfEvents,EventType.ArriveToExecutionModule);
        countStayedTime(event.getTime(),event.getQuery());
        return removedQuery;
    }

    // ---------------------------------------------------------------------------------------------
    // -------------------------------- End of the methods section --------------------------------
    // ---------------------------------------------------------------------------------------------
}

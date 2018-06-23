package DBMS_Sim;

import DBMS_Sim.SourceCode.Simulator;
import com.jfoenix.controls.*;
import javafx.application.Application;
import javafx.concurrent.Service;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.concurrent.Task;

import java.io.IOException;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    private Simulator simulator;


    private Random rnd = new Random(System.currentTimeMillis());
    @FXML NormalModeController normalModeController;



    // UI Variables - Home

    @FXML StackPane stackPane1;
    @FXML JFXButton btnIniciar;
    @FXML JFXToggleButton mode;
    @FXML JFXToggleButton graphic;
    @FXML JFXTextField txt_ntimes,txt_time,k,p,n,m,t;
    @FXML JFXTextField number;

    int ntimes, time, kCon, pProcess, nProcess, mProcess, timeout;
    private Service<Void> backgroundThread;
    private boolean validator;
    public Controller(){

    }
    public void run(String[] args){
        Application.launch(Initializer.class,args);
    }
    /**
     * Controlador de la
     *
     *
     *
     * @param event
     */
    @FXML
    void modeSlow(ActionEvent event){
        if(!mode.isSelected()){
            graphic.setSelected(false);
            graphic.setDisable(true);
        }else {
            graphic.setDisable(false);
        }
    }
    /**
     * Controlador del butón start, hacemos validaciones
     * Al ser el título obligatorio, si es nulo o vacío se lanzará
     * una excepción.
     *
     * @param event
     */


    @FXML
    void start(ActionEvent event) throws IOException, InterruptedException {

        validator = true;
        validate(txt_time);
        validate(txt_ntimes);
        validate(k);
        validate(t);
        validate(m);
        validate(p);
        validate(n);
        if(validator){

            //We store in the variables the values ​​that the user placed in the text-fields
            ntimes = Integer.parseInt(txt_ntimes.getText());
            time = Integer.parseInt(txt_time.getText());
            kCon = Integer.parseInt(k.getText());
            timeout = Integer.parseInt(t.getText());
            mProcess = Integer.parseInt(m.getText());
            pProcess = Integer.parseInt( p.getText());
            nProcess = Integer.parseInt(n.getText());
            //Cargamos la vista de normal mode
            normalModeScene(event);
            normalModeController.setTimeRunning(time);
            simulator = new Simulator(kCon, timeout, nProcess, pProcess, mProcess);
            simulator.setRunningTime((double)time);

            simulator.appendInitialEvent();
            System.out.println("Inicial");



            final Task<Void> task = new Task<Void>() {
                final double runningTime = time;

                @Override
                protected Void call() throws Exception {
                    double data [] = new double[7];
                    data [0] = 0;

                    while(data[0] <= runningTime){
                        data = simulator.iterateSimulation();
                        updateProgress(data[0], runningTime);
                        normalModeController.refreshScreen(data[0],(int)data[1],(int)data[2],(int)data[3], (int)data[4],(int)data[5],(int)data[6]);


                        Thread.sleep(1);
                    }

                    return null;
                }
            };

            normalModeController.pbProgress.progressProperty().bind(
                    task.progressProperty()
            );


            final Thread thread = new Thread(task, "task-thread");
            thread.setDaemon(true);
            thread.start();


        }else{
            JFXDialogLayout content = new JFXDialogLayout();
            JFXDialog dialog1 = new JFXDialog(stackPane1,content , JFXDialog.DialogTransition.CENTER,false);

            Label header = new Label("Warning");
            header.setTextFill(Color.RED);
            header.setFont(Font.font(20));
            content.setHeading(header);
            content.setBody(new Text("Missing Hace falta un espacio de llenar o escribió un paramétro \n"+
                    "al igual que Clarita inválido" +
                    " para así poder iniciar con la simulación." ));
            JFXButton button = new JFXButton("Okay");
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    dialog1.close();
                }
            });
            content.setActions(button);

            dialog1.show();
        }


    }

    /**
     *  Valida todos los text fields de la ventana de inicio, se asegura que haya algo y que sean números.
     *
     *
     * @param textField Recibe como parámetro el textfield ha validar.
     */
    private void validate(JFXTextField textField) {

        if (textField.getText().isEmpty()) {
            textField.setUnFocusColor(Color.rgb(244, 101, 66));
            validator = false;
        } else {
            if (!textField.getText().matches("[0-9]*")) {
                validator = false;
                textField.setUnFocusColor(Color.rgb(244, 101, 66));
            }
        }

    }
    /**
     * Despliega el scene en pantalla del normal mode, además guardamos el controller de esa escena.
     *
     * @param event
     */
    private void normalModeScene(ActionEvent event){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Views/simulationRunningNormalMode.fxml"));
            Parent root = (Parent) loader.load();
            normalModeController = loader.getController();

            Scene normalMode = new Scene(root);
            Stage appStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            appStage.hide();
            appStage.setScene(normalMode);
            appStage.show();
        }catch (IOException e) {
            e.printStackTrace();
        }

    }
    /**
     * Despliega el scene en pantalla del slow mode, además guardamos el controller de esa escena.
     *
     * @param event
     */
    private void slowModeScene(ActionEvent event){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Views/simulationRunningSlowMode.fxml"));
            Parent root = (Parent) loader.load();
            normalModeController = loader.getController();

            Scene normalMode = new Scene(root);
            Stage appStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            appStage.hide();
            appStage.setScene(normalMode);
            appStage.show();
        }catch (IOException e) {
            e.printStackTrace();
        }


    }
    /**
     * Despliega el scene en pantalla del graph mode, además guardamos el controller de esa escena.
     *
     * @param event
     */
    private void graphModeScene(ActionEvent event){



    }
    @Override
    public void initialize(URL arg1, ResourceBundle arg2){}

}




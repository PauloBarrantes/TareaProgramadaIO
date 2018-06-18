package DBMS_Sim;

import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class NormalModeController  {
    @FXML
    JFXProgressBar pbProgress ;
    @FXML
    JFXTextField Clock1;
    @FXML
    Label lblClock1,  lblMac, lblPAM ,lblQPM,lblTASM,lblEM,lblDiscarded;

    private double timeRunning;
    public void initialize(URL arg1, ResourceBundle arg2){
    }

    public void setTimeRunning(int timeRunning){
        this.timeRunning = (double)timeRunning;
    }


    public void refreshScreen(int clock, int MAC, int PAM, int QPM, int TASM, int EM, int discardedConnections){
        lblClock1.setText(""+ clock);
        lblMac.setText(""+ MAC);
        lblPAM.setText(""+ PAM);
        lblQPM.setText(""+ QPM);
        lblTASM.setText(""+ TASM);
        lblEM.setText(""+ EM);
        lblDiscarded.setText(""+ discardedConnections);

        pbProgress.setProgress(clock/timeRunning);


    }

}

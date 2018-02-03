package main;

import javafx.scene.control.Label;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class Uhr implements Runnable {


    @Override
    public void run() {
        while(true){
            LocalDateTime time = LocalDateTime.now();
            DateTimeFormatter format = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM);
            String text = time.format(format);
            //FensterController fc =
           // Label ZeitLabel = fc.getZeitLabel();
            //ZeitLabel.setText(text);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

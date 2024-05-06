package net.cubosoft.tafelab.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AdbCommandService {

	@Value("${app.adbCommand.path}")
	String adbPath;
	
    public void connect(String ip) {
        String command = adbPath+"adb connect " + ip;
        executeCommand(command);
    }

    public void reboot() {
        String command = adbPath+"adb shell reboot";
        executeCommand(command);
    }

    private void executeCommand(String command) {
    	 ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
         processBuilder.redirectErrorStream(true);

         Process process;
		try {
			process = processBuilder.start();
			// Lee la salida del proceso
	         BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	         String line;
	         while ((line = reader.readLine()) != null) {
	             System.out.println(line);
	         }

	         // Espera a que el proceso termine
	         try {
	             int exitCode = process.waitFor();
	             System.out.println("Comando terminado con código de salida: " + exitCode);
	         } catch (InterruptedException e) {
	             e.printStackTrace();
	         }
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}

         
    }
}
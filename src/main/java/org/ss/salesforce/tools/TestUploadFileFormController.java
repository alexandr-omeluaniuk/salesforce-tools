/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ss.salesforce.tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 * FXML Controller class
 *
 * @author ss
 */
public class TestUploadFileFormController implements Initializable {
    
    @FXML private TextField endpoint;
    
    @FXML private TextField fileLocation;
    
    @FXML private TextField token;
    
    @FXML private TextField sObjectID;
    
    @FXML private TextArea logOutput;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }
    
    @FXML
    private void onSubmit() throws IOException {
        logOutput.setText("");
        String endpointText = endpoint.getText();
        String fileLocationText = fileLocation.getText();
        String tokenText = token.getText();
        String sObjectIDText = sObjectID.getText();
        log("Start new request..........");
        if (endpointText == null || endpointText.isBlank()) {
            log("Endpoint is required! Force exit...");
        } else {
            log("Endpoint [" + endpointText + "]");
        }
        if (fileLocationText == null || fileLocationText.isBlank() || !new File(fileLocationText).exists()) {
            log("Wrong file location! Force exit...");
        } else {
            log("File location [" + fileLocationText + "]");
        }
        if (tokenText == null || tokenText.isBlank()) {
            log("Bearer token is required! Force exit...");
        } else {
            log("Bearer token [" + tokenText + "]");
        }
        if (sObjectIDText == null || sObjectIDText.isBlank()) {
            log("sObject ID is required! Force exit...");
        } else {
            log("sObject ID token [" + sObjectIDText + "]");
        }
        File sourceFile = new File(fileLocationText);
        File multipart = new File("multipart.json");
        FileOutputStream fos = new FileOutputStream(multipart);
        String jsonStart = "--boundary_string\n" +
                            "Content-Disposition: form-data; name=\"entity_content\";\n" +
                            "Content-Type: application/json\n" +
                            "\n" +
                            "{\n" +
                            "    \"ContentDocumentId\" : \"" + sObjectIDText + "\",\n" +
                            "    \"ReasonForChange\" : \"Salesforce Tool\",\n" +
                            "    \"Title\" : \"" + sourceFile.getName() + "\",\n" +
                            "    \"PathOnClient\" : \"" + sourceFile.getAbsolutePath() + "\"\n" +
                            "}\n" +
                            "\n" +
                            "--boundary_string\n" +
                            "    Content-Type: application/octet-stream\n" +
                            "    Content-Disposition: form-data; name=\"VersionData\"; filename=\""
                + sourceFile.getAbsolutePath() + "\"\n\n";
        fos.write(jsonStart.getBytes("UTF-8"));
        log("\n\n" + jsonStart);
        fos.write(Files.readAllBytes(sourceFile.toPath()));
        log(" Binary data");
        String jsonEnd = "\n--boundary_string--";
        fos.write(jsonEnd.getBytes("UTF-8"));
        log(jsonEnd);
        fos.close();
        request(endpointText, tokenText);
    }
    
    private void log(String message) {
        Platform.runLater(() -> {
            logOutput.setText(logOutput.getText() + "\n" + message);
        });
    }
    
    private void request(String endpoint, String token) {
        try {
            log(">>> HTTP request start");
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=\"boundary_string\"");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.getOutputStream().write(Files.readAllBytes(new File("multipart.json").toPath()));
            int responseCode = conn.getResponseCode();
            log("> response code [" + responseCode + "]");
            InputStream is = responseCode == 200 ? conn.getInputStream() : conn.getErrorStream();
            int len;
            byte[] buff = new byte[1024 * 10];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while ((len = is.read(buff)) != -1) {
                baos.write(buff, 0, len);
            }
            log("> Salesforce response:");
            log(new String(baos.toByteArray(), "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
            log("Request error: " + e.getMessage());
        }
    }
    
}

module org.ss.salesforce.tools {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;

    opens org.ss.salesforce.tools to javafx.fxml;
    exports org.ss.salesforce.tools;
}
module com.bombk1n.coursework1 {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.bombk1n.coursework1.view to javafx.fxml;
    exports com.bombk1n.coursework1;
}
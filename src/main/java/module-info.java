module com.example.bomb {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.bomb to javafx.fxml;
    exports com.example.bomb;
}
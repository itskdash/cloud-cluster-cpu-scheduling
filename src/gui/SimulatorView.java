package gui;

import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import models.Process;
import models.SchedulingResult;
import schedulers.ACSScheduler;
import schedulers.SJFScheduler;

public class SimulatorView extends BorderPane {
    private static final String[] COLORS = { "#3B82F6", "#10B981", "#F59E0B", "#8B5CF6", "#EC4899", "#06B6D4",
            "#F97316" };

    private final TextField quantum = new TextField("3");
    private final TextField cs = new TextField("0");
    private final TextField agingInterval = new TextField("4");
    private final TextField agingStep = new TextField("1");

    private final TableView<String[]> processTable = table("PID", "Arrival", "Burst", "Priority");
    private final TableView<String[]> resultTable = table("PID", "SJF Wait", "SJF TAT", "SJF Resp",
            "ACS Wait", "ACS TAT", "ACS Resp");
    private final TableView<String[]> matrixTable = table("Time", "Running", "Tier 1 (Round Robin)",
            "Tier 2 (Priority)", "Completed");
    private final VBox timelines = new VBox(6);

    public SimulatorView() {
        Button run = new Button("Run");
        run.setOnAction(e -> run());

        HBox top = new HBox(8, new Label("Q:"), small(quantum), new Label("CS:"), small(cs),
                new Label("Aging interval:"), small(agingInterval), new Label("Aging step:"), small(agingStep), run);
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(10));
        setTop(top);

        setLeft(processPanel());

        Tab results = new Tab("Timelines & Metrics", new VBox(10, timelines, resultTable));
        Tab matrix = new Tab("Demotion Matrix (every time step)", matrixTable);
        results.setClosable(false);
        matrix.setClosable(false);
        ((VBox) results.getContent()).setPadding(new Insets(10));
        VBox.setVgrow(resultTable, Priority.ALWAYS);
        setCenter(new TabPane(results, matrix));
    }

    private VBox processPanel() {
        TextField pid = field("PID"), arrival = field("Arrival"), burst = field("Burst"), priority = field("Priority");
        Button add = new Button("Add");
        Button remove = new Button("Remove");

        add.setOnAction(e -> {
            try {
                String[] row = { pid.getText().trim(), arrival.getText().trim(), burst.getText().trim(),
                        priority.getText().trim() };
                new Process(Integer.parseInt(row[0]), Integer.parseInt(row[1]), Integer.parseInt(row[2]),
                        Integer.parseInt(row[3]));
                for (String[] other : processTable.getItems())
                    if (other[0].equals(row[0]))
                        throw new IllegalArgumentException("PID " + row[0] + " already exists");
                processTable.getItems().add(row);
                pid.setText(String.valueOf(Integer.parseInt(row[0]) + 1));
            } catch (NumberFormatException ex) {
                error("Please enter whole numbers only.");
            } catch (IllegalArgumentException ex) {
                error(ex.getMessage());
            }
        });
        remove.setOnAction(e -> processTable.getItems().remove(processTable.getSelectionModel().getSelectedItem()));

        VBox panel = new VBox(8, new Label("Processes"), processTable, new HBox(4, pid, arrival, burst, priority),
                new HBox(8, add, remove));
        VBox.setVgrow(processTable, Priority.ALWAYS);
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(330);
        return panel;
    }

    private void run() {
        try {
            if (processTable.getItems().isEmpty())
                throw new IllegalArgumentException("Add at least one process.");
            int q = num(quantum), c = num(cs), interval = num(agingInterval), step = num(agingStep);
            SchedulingResult sjf = new SJFScheduler(c).schedule(processes());
            SchedulingResult acs = new ACSScheduler(c, q, interval, step).schedule(processes());

            timelines.getChildren().setAll(title(sjf), gantt(sjf), title(acs), gantt(acs));

            List<String[]> rows = new ArrayList<>();
            for (int i = 0; i < sjf.processes.size(); i++) {
                Process a = sjf.processes.get(i), b = acs.processes.get(i);
                rows.add(new String[] { "P" + a.pid, "" + a.waiting(), "" + a.turnaround(), "" + a.response(),
                        "" + b.waiting(), "" + b.turnaround(), "" + b.response() });
            }
            resultTable.setItems(FXCollections.observableArrayList(rows));
            matrixTable.setItems(FXCollections.observableArrayList(acs.steps));
        } catch (NumberFormatException ex) {
            error("Please enter whole numbers only.");
        } catch (IllegalArgumentException ex) {
            error(ex.getMessage());
        }
    }

    private List<Process> processes() {
        List<Process> list = new ArrayList<>();
        for (String[] r : processTable.getItems())
            list.add(new Process(Integer.parseInt(r[0]), Integer.parseInt(r[1]), Integer.parseInt(r[2]),
                    Integer.parseInt(r[3])));
        return list;
    }

    private Label title(SchedulingResult r) {
        Label label = new Label(String.format("%s   |   avg waiting %.2f   avg turnaround %.2f   avg response %.2f",
                r.algorithm, r.avgWaiting(), r.avgTurnaround(), r.avgResponse()));
        label.setStyle("-fx-font-weight: bold;");
        return label;
    }

    private Node gantt(SchedulingResult r) {
        HBox bar = new HBox();
        int end = 0;
        for (int[] s : r.gantt) {
            Label box = new Label(SchedulingResult.label(s[0]));
            box.setPrefSize((s[2] - s[1]) * 30, 32);
            box.setMinWidth((s[2] - s[1]) * 30);
            box.setAlignment(Pos.CENTER);
            box.setStyle("-fx-border-color: white; -fx-text-fill: white; -fx-background-color: " + color(s[0]));
            bar.getChildren().add(new VBox(2, box, new Label(String.valueOf(s[1]))));
            end = s[2];
        }
        bar.getChildren().add(new Label(String.valueOf(end)));
        ScrollPane scroll = new ScrollPane(bar);
        scroll.setFitToHeight(true);
        scroll.setPrefHeight(80);
        return scroll;
    }

    private String color(int label) {
        if (label < 0)
            return "#EF4444";
        if (label == 0)
            return "#94A3B8";
        return COLORS[label % COLORS.length];
    }

    private static TableView<String[]> table(String... titles) {
        TableView<String[]> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        for (int i = 0; i < titles.length; i++) {
            final int index = i;
            TableColumn<String[], String> column = new TableColumn<>(titles[i]);
            column.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[index]));
            table.getColumns().add(column);
        }
        return table;
    }

    private static TextField field(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        return f;
    }

    private static TextField small(TextField f) {
        f.setPrefWidth(50);
        return f;
    }

    private static int num(TextField f) {
        return Integer.parseInt(f.getText().trim());
    }

    private static void error(String message) {
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }
}
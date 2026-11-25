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
import models.TestCases;
import schedulers.ACSScheduler;
import schedulers.SJFScheduler;

public class SimulatorView extends BorderPane {
    private static final String[] COLORS = { "#3B82F6", "#10B981", "#F59E0B", "#8B5CF6", "#EC4899", "#06B6D4",
            "#F97316" };

    private final TextField quantum = small("3");
    private final TextField cs = small("0");
    private final TextField agingInterval = small("4");
    private final TextField agingStep = small("1");

    private final TableView<String[]> processTable = table("PID", "Arrival", "Burst", "Priority");
    private final TableView<String[]> resultTable = table("PID", "SJF Wait", "SJF TAT", "SJF Resp",
            "ACS Wait", "ACS TAT", "ACS Resp");
    private final TableView<String[]> matrixTable = table("Time", "Running", "Tier 1 (Round Robin)",
            "Tier 2 (Priority)", "Completed");
    private final VBox timelines = new VBox(8);

    public SimulatorView() {
        setStyle("-fx-background-color: #F8FAFC; -fx-font-family: 'Segoe UI', system-ui, sans-serif;");

        HBox top = new HBox(10,
                paramLabel("Quantum (Q):"), quantum,
                paramLabel("Context Switch:"), cs,
                paramLabel("Aging Interval:"), agingInterval,
                paramLabel("Aging Step:"), agingStep,
                actionBtn("Run Simulation", "#10B981", "5 14", this::run),
                actionBtn("Test 1: Starvation", "#8B5CF6", "5 10", () -> loadTestCase(TestCases.starvationTestCase())),
                actionBtn("Test 2: Demotion", "#F59E0B", "5 10",
                        () -> loadTestCase(TestCases.demotionAndContextSwitchTestCase())));
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(10, 14, 10, 14));
        top.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E2E8F0; -fx-border-width: 0 0 1 0;");
        setTop(top);

        setLeft(processPanel());

        resultTable.setFixedCellSize(24);
        setColumnWidth(matrixTable, 0, 55, 55);
        setColumnWidth(matrixTable, 1, 75, 75);
        setColumnWidth(matrixTable, 4, 140, 160);

        VBox resultsContainer = container(10, timelines, resultTable);
        VBox.setVgrow(resultTable, Priority.ALWAYS);

        VBox matrixContainer = container(0, matrixTable);
        VBox.setVgrow(matrixTable, Priority.ALWAYS);

        Tab results = new Tab("Timelines & Metrics", resultsContainer);
        Tab matrix = new Tab("Demotion Matrix (every time step)", matrixContainer);
        results.setClosable(false);
        matrix.setClosable(false);

        setCenter(new TabPane(results, matrix));
    }

    private void loadTestCase(TestCases.TestCase tc) {
        quantum.setText(String.valueOf(tc.quantum));
        cs.setText(String.valueOf(tc.contextSwitch));
        agingInterval.setText(String.valueOf(tc.agingInterval));
        agingStep.setText(String.valueOf(tc.agingStep));

        processTable.getItems().setAll(tc.processes.stream()
                .map(p -> new String[] { "" + p.pid, "" + p.arrival, "" + p.burst, "" + p.priority })
                .toList());
        run();
    }

    private VBox processPanel() {
        TextField pid = field("PID"), arrival = field("Arrival"), burst = field("Burst"), priority = field("Priority");
        pid.setText("1");

        Runnable addAction = () -> {
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
                arrival.clear();
                burst.clear();
                priority.clear();
                arrival.requestFocus();
            } catch (NumberFormatException ex) {
                error("Please enter whole numbers only.");
            } catch (IllegalArgumentException ex) {
                error(ex.getMessage());
            }
        };

        Button add = actionBtn("Add", "#3B82F6", "4 10", addAction);
        Button remove = actionBtn("Remove", "#EF4444", "4 10",
                () -> processTable.getItems().remove(processTable.getSelectionModel().getSelectedItem()));
        add.setMaxWidth(Double.MAX_VALUE);
        remove.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(add, Priority.ALWAYS);
        HBox.setHgrow(remove, Priority.ALWAYS);
        priority.setOnAction(e -> addAction.run());

        Label header = new Label("Processes Queue");
        header.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1E293B;");

        VBox panel = new VBox(8, header, processTable, new HBox(6, pid, arrival, burst, priority),
                new HBox(8, add, remove));
        VBox.setVgrow(processTable, Priority.ALWAYS);
        panel.setPadding(new Insets(10, 12, 10, 12));
        panel.setPrefWidth(320);
        panel.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E2E8F0; -fx-border-width: 0 1 0 0;");
        return panel;
    }

    private void run() {
        try {
            if (processTable.getItems().isEmpty())
                throw new IllegalArgumentException("Add at least one process.");
            int q = num(quantum), c = num(cs), interval = num(agingInterval), step = num(agingStep);
            SchedulingResult sjf = new SJFScheduler(c).schedule(processes());
            SchedulingResult acs = new ACSScheduler(c, q, interval, step).schedule(processes());

            timelines.getChildren().setAll(ganttCard(sjf), ganttCard(acs));

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

    private VBox ganttCard(SchedulingResult r) {
        VBox card = new VBox(4, title(r), gantt(r));
        card.setPadding(new Insets(8, 12, 6, 12));
        card.setStyle(
                "-fx-background-color: #FFFFFF; -fx-border-color: #E2E8F0; -fx-border-radius: 6; -fx-background-radius: 6;");
        return card;
    }

    private List<Process> processes() {
        List<Process> list = new ArrayList<>();
        for (String[] r : processTable.getItems())
            list.add(new Process(Integer.parseInt(r[0]), Integer.parseInt(r[1]), Integer.parseInt(r[2]),
                    Integer.parseInt(r[3])));
        return list;
    }

    private Label title(SchedulingResult r) {
        Label label = new Label(String.format("%s   •   Avg Wait: %.2f   |   Avg TAT: %.2f   |   Avg Response: %.2f",
                r.algorithm, r.avgWaiting(), r.avgTurnaround(), r.avgResponse()));
        label.setStyle("-fx-font-weight: bold; -fx-font-size: 12.5px; -fx-text-fill: #1E293B;");
        return label;
    }

    private Node gantt(SchedulingResult r) {
        HBox bar = new HBox(2);
        bar.setPadding(new Insets(2, 2, 0, 2));
        int end = 0;
        for (int[] s : r.gantt) {
            int width = Math.max((s[2] - s[1]) * 32, 26);
            Label box = new Label(SchedulingResult.label(s[0]));
            box.setMinSize(width, 28);
            box.setAlignment(Pos.CENTER);
            box.setStyle(
                    "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-background-radius: 4; -fx-background-color: "
                            + color(s[0]));

            Label timeLabel = new Label(String.valueOf(s[1]));
            timeLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 10px;");

            bar.getChildren().add(new VBox(2, box, timeLabel));
            end = s[2];
        }
        Label endLabel = new Label(String.valueOf(end));
        endLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 10px; -fx-padding: 30 0 0 2;");
        bar.getChildren().add(endLabel);

        ScrollPane scroll = new ScrollPane(bar);
        scroll.setFitToHeight(true);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setPrefHeight(62);
        scroll.setStyle(
                "-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        return scroll;
    }

    private String color(int label) {
        if (label < 0)
            return "#EF4444";
        if (label == 0)
            return "#94A3B8";
        return COLORS[label % COLORS.length];
    }

    private static VBox container(double spacing, Node... children) {
        VBox box = new VBox(spacing, children);
        box.setPadding(new Insets(10, 14, 10, 14));
        box.setStyle("-fx-background-color: #F8FAFC;");
        return box;
    }

    private static Button actionBtn(String text, String color, String padding, Runnable action) {
        Button btn = new Button(text);
        btn.setStyle(
                "-fx-base: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: "
                        + padding + ";");
        btn.setOnAction(e -> action.run());
        return btn;
    }

    private static void setColumnWidth(TableView<?> table, int index, double min, double max) {
        table.getColumns().get(index).setMinWidth(min);
        table.getColumns().get(index).setMaxWidth(max);
    }

    private static TableView<String[]> table(String... titles) {
        TableView<String[]> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setStyle(
                "-fx-background-color: #FFFFFF; -fx-border-color: #E2E8F0; -fx-border-radius: 6; -fx-background-radius: 6;");
        for (int i = 0; i < titles.length; i++) {
            final int index = i;
            TableColumn<String[], String> column = new TableColumn<>(titles[i]);
            column.setCellValueFactory(d -> new SimpleStringProperty(d.getValue()[index]));
            column.setStyle("-fx-alignment: CENTER;");
            table.getColumns().add(column);
        }
        return table;
    }

    private static Label paramLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 12px;");
        return l;
    }

    private static TextField field(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setStyle("-fx-background-radius: 4; -fx-border-color: #CBD5E1; -fx-border-radius: 4; -fx-padding: 4 6;");
        return f;
    }

    private static TextField small(String value) {
        TextField f = new TextField(value);
        f.setPrefWidth(48);
        f.setAlignment(Pos.CENTER);
        f.setStyle("-fx-background-radius: 4; -fx-border-color: #CBD5E1; -fx-border-radius: 4; -fx-padding: 3 5;");
        return f;
    }

    private static int num(TextField f) {
        return Integer.parseInt(f.getText().trim());
    }

    private static void error(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setHeaderText("Invalid Input");
        alert.showAndWait();
    }
}
package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.io.File;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.HBox;
import javafx.geometry.Side;
import javafx.scene.layout.StackPane;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;

public class ReportUI {

    private final Stage stage;
    private VBox dashboard;

    private final String[] categories = {
        "Furniture",
        "Stationery",
        "Computer sets",
        "Miscellaneous"
    };

    public ReportUI(String period) {
        stage = new Stage();
        stage.setTitle(period + " Report");

        Label title = new Label(period + " Inventory Report");
        title.setStyle(
            "-fx-font-size: 26px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #2c3e50;"
        );

        Label purchase = new Label();
        Label issued = new Label();
        Label returnInv = new Label();
        Label returnSup = new Label();

        HBox cards = new HBox(20,
            createCard("Total Purchase", purchase, "#1abc9c"),
            createCard("Total Issued", issued, "#3498db"),
            createCard("Return to Inventory", returnInv, "#9b59b6"),
            createCard("Sent to Supplier", returnSup, "#e74c3c")
        );
        cards.setAlignment(Pos.CENTER);

        dashboard = new VBox(20);
        dashboard.setAlignment(Pos.CENTER);

        Button saveBtn = new Button("⬇ Save Report");
        saveBtn.setStyle(
            "-fx-background-color: #2c3e50;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 10 24 10 24;" +
            "-fx-cursor: hand;"
        );
        saveBtn.setOnMouseEntered(e -> saveBtn.setStyle(
            "-fx-background-color: #1a252f;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 10 24 10 24;" +
            "-fx-cursor: hand;"
        ));
        saveBtn.setOnMouseExited(e -> saveBtn.setStyle(
            "-fx-background-color: #2c3e50;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 10 24 10 24;" +
            "-fx-cursor: hand;"
        ));

        VBox root = new VBox(35, title, cards, dashboard, saveBtn);
        root.setPadding(new Insets(40));
        root.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #f8f9fc, #eef1f7);"
        );
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));

        saveBtn.setOnAction(e -> handleSave(root));

        Map<String, model.ReportData> data = loadReport(period);

        int p = 0, i = 0;
        int rInv = 0, rSup = 0;

        for (model.ReportData d : data.values()) {
            p    += d.purchase;
            i    += d.issued;
            rInv += d.returnToInventory;
            rSup += d.returnToSupplier;
        }

        purchase.setText(String.valueOf(p));
        issued.setText(String.valueOf(i));
        returnInv.setText(String.valueOf(rInv));
        returnSup.setText(String.valueOf(rSup));

        HBox charts = new HBox(60,
            createBarChart(data),
            createCategoryPie()
        );
        charts.setAlignment(Pos.CENTER);
        charts.setPadding(new Insets(20, 0, 0, 0));

        dashboard.getChildren().add(charts);

        stage.setScene(new Scene(root, 900, 700));
    }

    public void show() {
        stage.show();
    }

    private LocalDate[] getDateRange(String period) {
        LocalDate today = LocalDate.now();

        switch (period) {
            case "Weekly": {
                LocalDate start = today.with(java.time.DayOfWeek.SUNDAY);
                if (today.getDayOfWeek() != java.time.DayOfWeek.SUNDAY) {
                    start = today.with(java.time.temporal.TemporalAdjusters.previous(java.time.DayOfWeek.SUNDAY));
                }
                LocalDate end = start.plusDays(6);
                return new LocalDate[]{start, end};
            }
            case "Monthly": {
                LocalDate start = today.withDayOfMonth(1);
                LocalDate end = today.with(java.time.temporal.TemporalAdjusters.lastDayOfMonth());
                return new LocalDate[]{start, end};
            }
            case "Yearly": {
                LocalDate start = LocalDate.of(today.getYear(), 1, 1);
                LocalDate end = LocalDate.of(today.getYear(), 12, 31);
                return new LocalDate[]{start, end};
            }
            default:
                return new LocalDate[]{LocalDate.of(2020, 1, 1), today};
        }
    }

    private int getSum(Connection con, String sql, String category, LocalDate[] range) throws Exception {
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, category);
        ps.setDate(2, Date.valueOf(range[0]));
        ps.setDate(3, Date.valueOf(range[1]));
        ResultSet rs = ps.executeQuery();
        return rs.next() ? rs.getInt(1) : 0;
    }

    private Map<String, model.ReportData> loadReport(String period) {
        Map<String, model.ReportData> map = new HashMap<>();
        LocalDate[] range = getDateRange(period);

        try (Connection con = db.DBConnection.getConnection()) {

            for (String cat : categories) {
                model.ReportData rd = new model.ReportData();

                rd.purchase = getSum(con,
                    "SELECT COALESCE(SUM(b.qty_received), 0) " +
                    "FROM bill_invoice b " +
                    "JOIN product p ON b.pid = p.pid " +
                    "JOIN product_type pt ON p.ptype_id = pt.ptype_id " +
                    "WHERE LOWER(pt.ptype_name) LIKE LOWER(?) " +
                    "AND b.record_status = 'ACTIVE' " +
                    "AND b.received_date BETWEEN ? AND ?",
                    "%" + cat + "%", range
                );

                rd.issued = getSum(con,
                    "SELECT COALESCE(SUM(i.qty_issued), 0) " +
                    "FROM issue i " +
                    "JOIN product p ON i.pid = p.pid " +
                    "JOIN product_type pt ON p.ptype_id = pt.ptype_id " +
                    "WHERE LOWER(pt.ptype_name) LIKE LOWER(?) " +
                    "AND i.date BETWEEN ? AND ?",
                    "%" + cat + "%", range
                );

                rd.returnToInventory = getSum(con,
                    "SELECT COALESCE(SUM(x.quantity), 0) " +
                    "FROM ( " +
                    "    SELECT r.quantity, p.ptype_id, r.date AS dt " +
                    "    FROM return_table r " +
                    "    JOIN product p ON r.pid = p.pid " +
                    "    UNION ALL " +
                    "    SELECT rts.quantity, p.ptype_id, rts.rts_date AS dt " +
                    "    FROM rts_table rts " +
                    "    JOIN product p ON rts.pid = p.pid " +
                    "    WHERE rts.record_status = 'RECEIVED' " +
                    ") x " +
                    "JOIN product_type pt ON x.ptype_id = pt.ptype_id " +
                    "WHERE LOWER(pt.ptype_name) LIKE LOWER(?) " +
                    "AND x.dt BETWEEN ? AND ?",
                    "%" + cat + "%", range
                );

                rd.returnToSupplier = getSum(con,
                    "SELECT COALESCE(SUM(rts.quantity), 0) " +
                    "FROM rts_table rts " +
                    "JOIN product p ON rts.pid = p.pid " +
                    "JOIN product_type pt ON p.ptype_id = pt.ptype_id " +
                    "WHERE LOWER(pt.ptype_name) LIKE LOWER(?) " +
                    "AND rts.record_status = 'SENT' " +
                    "AND rts.rts_date BETWEEN ? AND ?",
                    "%" + cat + "%", range
                );

                rd.returnFromSupplier = 0;

                map.put(cat, rd);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    // ── Queries actual qty_in_stock from product table, grouped by category ──
    private Map<String, Integer> loadStockFromDB() {
        Map<String, Integer> map = new HashMap<>();
        String sql =
            "SELECT pt.ptype_name, COALESCE(SUM(p.qty_in_stock), 0) AS total " +
            "FROM product p " +
            "JOIN product_type pt ON p.ptype_id = pt.ptype_id " +
            "GROUP BY pt.ptype_name";
        try (Connection con = db.DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String typeName = rs.getString("ptype_name");
                int qty = rs.getInt("total");
                for (String cat : categories) {
                    if (typeName.equalsIgnoreCase(cat)) {
                        map.put(cat, qty);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    private VBox createCard(String title, Label valueLabel, String color) {

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: white;");

        valueLabel.setStyle("-fx-font-size: 26px; -fx-text-fill: white;");

        VBox box = new VBox(8, titleLabel, valueLabel);
        box.setPadding(new Insets(15));
        box.setAlignment(Pos.CENTER);
        box.setMinWidth(180);
        box.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-background-radius: 18;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 15, 0.3, 0, 6);"
        );
        box.setOnMouseEntered(e -> {
            box.setScaleX(1.08);
            box.setScaleY(1.08);
            box.setStyle(
                "-fx-background-color: " + color + ";" +
                "-fx-background-radius: 18;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 25, 0.4, 0, 10);"
            );
        });
        box.setOnMouseExited(e -> {
            box.setScaleX(1);
            box.setScaleY(1);
            box.setStyle(
                "-fx-background-color: " + color + ";" +
                "-fx-background-radius: 18;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 15, 0.3, 0, 6);"
            );
        });

        return box;
    }

    private BarChart<String, Number> createBarChart(Map<String, model.ReportData> data) {

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Type");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Quantity");
        yAxis.setForceZeroInRange(true);

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle("Inventory Movement");
        chart.setPrefHeight(320);
        chart.setHorizontalGridLinesVisible(true);
        chart.setVerticalGridLinesVisible(false);
        chart.setLegendVisible(false);
        chart.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 18;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 18, 0.3, 0, 6);"
        );

        int totalPurchase = 0, totalIssued = 0, totalReturned = 0;
        for (model.ReportData d : data.values()) {
            totalPurchase += d.purchase;
            totalIssued   += d.issued;
            totalReturned += d.returnToInventory;
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("Purchase",      totalPurchase));
        series.getData().add(new XYChart.Data<>("Issued",        totalIssued));
        series.getData().add(new XYChart.Data<>("Return to Inv", totalReturned));

        chart.getData().add(series);

        String[] barColors = {"#1abc9c", "#3498db", "#9b59b6"};

        for (int idx = 0; idx < series.getData().size(); idx++) {
            String color = barColors[idx];
            series.getData().get(idx).nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle(
                        "-fx-background-radius: 14 14 0 0;" +
                        "-fx-background-color: " + color + ";"
                    );
                }
            });
        }
        javafx.application.Platform.runLater(() -> {
            for (int idx = 0; idx < series.getData().size(); idx++) {
                String color = barColors[idx];
                javafx.scene.Node node = series.getData().get(idx).getNode();
                if (node != null) {
                    node.setStyle(
                        "-fx-background-radius: 14 14 0 0;" +
                        "-fx-background-color: " + color + ";"
                    );
                }
            }
        });

        displayBarValues(series);
        chart.setAnimated(true);
        chart.setCategoryGap(40);
        chart.setBarGap(10);

        return chart;
    }

    // ── Pie chart reads qty_in_stock directly from DB, period-independent ──
    private VBox createCategoryPie() {

        PieChart pie = new PieChart();
        pie.setTitle("Stock by Category");
        pie.setLegendVisible(false);
        pie.setLabelsVisible(false);
        pie.setPrefSize(320, 320);
        pie.setMinSize(320, 320);
        pie.setClip(null);

        Map<String, String> colorMap = Map.of(
            "Stationery",    "#3498db",
            "Furniture",     "#f39c12",
            "Computer sets", "#e74c3c",
            "Miscellaneous", "#2ecc71"
        );

        Map<String, Integer> stockByCategory = loadStockFromDB();

        int totalStock = 0;
        VBox legend = new VBox(10);
        legend.setAlignment(Pos.CENTER_LEFT);

        for (String name : categories) {
            int stock = stockByCategory.getOrDefault(name, 0);

            totalStock += stock;

            if (stock <= 0) continue;

            PieChart.Data slice = new PieChart.Data(name, stock);
            pie.getData().add(slice);

            legend.getChildren().add(
                createLegendItem(colorMap.getOrDefault(name, "#bdc3c7"), name)
            );
        }

        for (PieChart.Data d : pie.getData()) {
            String color = colorMap.getOrDefault(d.getName(), "#bdc3c7");
            d.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle("-fx-pie-color: " + color + ";");
                }
            });
        }
        javafx.application.Platform.runLater(() -> {
            for (PieChart.Data d : pie.getData()) {
                String color = colorMap.getOrDefault(d.getName(), "#bdc3c7");
                if (d.getNode() != null) {
                    d.getNode().setStyle("-fx-pie-color: " + color + ";");
                }
            }
        });

        Label centerLabel = new Label("TOTAL STOCK\n" + totalStock);
        centerLabel.setAlignment(Pos.CENTER);
        centerLabel.setStyle(
            "-fx-font-size: 16px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #34495e;" +
            "-fx-background-color: white;" +
            "-fx-background-radius: 50;" +
            "-fx-padding: 12 18 12 18;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 10, 0.3, 0, 2);"
        );

        StackPane donut = new StackPane();
        javafx.scene.shape.Circle hole = new javafx.scene.shape.Circle(60);
        hole.setStyle("-fx-fill: #f4f6f9;");
        hole.setMouseTransparent(true);
        donut.getChildren().addAll(pie, hole, centerLabel);
        donut.setPadding(new Insets(10));

        VBox container = new VBox(15, donut, legend);
        container.setAlignment(Pos.CENTER);
        return container;
    }

    private HBox createLegendItem(String color, String text) {
        Label dot = new Label();
        dot.setStyle(
            "-fx-background-color: " + color + ";" +
            "-fx-background-radius: 50%;" +
            "-fx-min-width: 14px;" +
            "-fx-min-height: 14px;"
        );
        Label label = new Label(text);
        label.setStyle(
            "-fx-font-size: 14px;" +
            "-fx-text-fill: #2c3e50;" +
            "-fx-font-weight: 600;"
        );
        return new HBox(8, dot, label);
    }

    private void displayBarValues(XYChart.Series<String, Number> series) {
        for (XYChart.Data<String, Number> data : series.getData()) {
            Label label = new Label(data.getYValue().toString());
            label.setStyle(
                "-fx-font-size: 14px;" +
                "-fx-font-weight: bold;" +
                "-fx-text-fill: #2c3e50;"
            );
            label.setTranslateY(-10);
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    ((javafx.scene.layout.StackPane) newNode).getChildren().add(label);
                }
            });
        }
    }

    // ── Save Feature ─────────────────────────────────────────────────────────

    private void handleSave(VBox root) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Report As");
        chooser.setInitialFileName("inventory_report");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("JPG Image", "*.jpg")
        );
        File file = chooser.showSaveDialog(stage);
        if (file != null) saveAsJpg(root, file);
    }

    private void saveAsJpg(VBox root, File file) {
        try {
            javafx.scene.image.WritableImage image = root.snapshot(
                new javafx.scene.SnapshotParameters(), null
            );
            java.awt.image.BufferedImage buffered = SwingFXUtils.fromFXImage(image, null);

            java.awt.image.BufferedImage rgb = new java.awt.image.BufferedImage(
                buffered.getWidth(), buffered.getHeight(),
                java.awt.image.BufferedImage.TYPE_INT_RGB
            );
            java.awt.Graphics2D g = rgb.createGraphics();
            g.drawImage(buffered, 0, 0, java.awt.Color.WHITE, null);
            g.dispose();

            ImageIO.write(rgb, "jpg", file);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Saved");
            alert.setHeaderText(null);
            alert.setContentText("Report saved as JPG:\n" + file.getAbsolutePath());
            alert.showAndWait();

        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Failed to save JPG: " + ex.getMessage());
        }
    }

    private void saveAsPdf(VBox root, File file) {
        try {
            javafx.scene.image.WritableImage fxImage = root.snapshot(
                new javafx.scene.SnapshotParameters(), null
            );
            java.awt.image.BufferedImage buffered = SwingFXUtils.fromFXImage(fxImage, null);

            java.awt.image.BufferedImage rgb = new java.awt.image.BufferedImage(
                buffered.getWidth(), buffered.getHeight(),
                java.awt.image.BufferedImage.TYPE_INT_RGB
            );
            java.awt.Graphics2D g = rgb.createGraphics();
            g.drawImage(buffered, 0, 0, java.awt.Color.WHITE, null);
            g.dispose();

            java.io.ByteArrayOutputStream imgBytes = new java.io.ByteArrayOutputStream();
            ImageIO.write(rgb, "jpg", imgBytes);
            byte[] jpgData = imgBytes.toByteArray();

            writePdfWithImage(file, jpgData, rgb.getWidth(), rgb.getHeight());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Saved");
            alert.setHeaderText(null);
            alert.setContentText("Report saved as PDF:\n" + file.getAbsolutePath());
            alert.showAndWait();

        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Failed to save PDF: " + ex.getMessage());
        }
    }

    private void writePdfWithImage(File out, byte[] jpgData, int imgW, int imgH)
            throws Exception {

        float pageW = 595f, pageH = 842f;
        float scale = Math.min(pageW / imgW, pageH / imgH);
        float drawW = imgW * scale;
        float drawH = imgH * scale;
        float x     = (pageW - drawW) / 2f;
        float y     = (pageH - drawH) / 2f;

        java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
        long[] offsets = new long[5];
        int obj = 0;

        offsets[obj++] = buf.size();
        buf.write("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n".getBytes());

        offsets[obj++] = buf.size();
        buf.write("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n".getBytes());

        offsets[obj++] = buf.size();
        buf.write(String.format(
            "3 0 obj\n<< /Type /Page /Parent 2 0 R" +
            " /MediaBox [0 0 %.2f %.2f]" +
            " /Contents 4 0 R /Resources << /XObject << /Img 5 0 R >> >> >>\nendobj\n",
            pageW, pageH).getBytes());

        String stream = String.format(
            "q %.2f 0 0 %.2f %.2f %.2f cm /Img Do Q", drawW, drawH, x, y);
        offsets[obj++] = buf.size();
        buf.write(String.format(
            "4 0 obj\n<< /Length %d >>\nstream\n%s\nendstream\nendobj\n",
            stream.length(), stream).getBytes());

        offsets[obj++] = buf.size();
        buf.write(String.format(
            "5 0 obj\n<< /Type /XObject /Subtype /Image" +
            " /Width %d /Height %d /ColorSpace /DeviceRGB" +
            " /BitsPerComponent 8 /Filter /DCTDecode /Length %d >>\nstream\n",
            imgW, imgH, jpgData.length).getBytes());
        buf.write(jpgData);
        buf.write("\nendstream\nendobj\n".getBytes());

        long xrefOffset = buf.size();
        buf.write("xref\n0 6\n".getBytes());
        buf.write("0000000000 65535 f \n".getBytes());
        for (long off : offsets) {
            buf.write(String.format("%010d 00000 n \n", off).getBytes());
        }

        buf.write(String.format(
            "trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n%d\n%%%%EOF\n",
            xrefOffset).getBytes());

        try (java.io.FileOutputStream fos = new java.io.FileOutputStream(out)) {
            fos.write(buf.toByteArray());
        }
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

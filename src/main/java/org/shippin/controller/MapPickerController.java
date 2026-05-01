package org.shippin.controller;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.embed.swing.SwingNode;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.*;
import org.shippin.app.FromCoordsDataGetter;
import org.shippin.services.NavigationService;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;

public class MapPickerController {

    private static double selectedLat = 0;
    private static double selectedLng = 0;

    public static void open() {
        Stage dialog = new Stage();
        dialog.setTitle(NavigationService.getBundle().getString("map_picker.title"));
        dialog.setWidth(1000);
        dialog.setHeight(750);
        dialog.initModality(Modality.APPLICATION_MODAL);

        JXMapViewer map = new JXMapViewer();

        TileFactoryInfo info = new TileFactoryInfo(
                0, 19, 19,
                256, true, true,
                "https://a.tile.openstreetmap.org",
                "x", "y", "z"
        ) {
            public String getTileUrl(int x, int y, int zoom) {
                int z = 19 - zoom;
                return this.baseURL + "/" + z + "/" + x + "/" + y + ".png";
            }
        };

        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        map.setTileFactory(tileFactory);

        GeoPosition start = new GeoPosition(48.7, 19.0);
        map.setAddressLocation(start);
        map.setZoom(7);
        map.setPanEnabled(true);

        map.addMouseWheelListener(e -> {
            if (e.getWheelRotation() < 0)
                map.setZoom(map.getZoom() - 1);
            else
                map.setZoom(map.getZoom() + 1);
        });

        MouseAdapter ma = new MouseAdapter() {
            Point last;

            public void mousePressed(MouseEvent e) {
                last = e.getPoint();
            }

            public void mouseDragged(MouseEvent e) {
                Point current = e.getPoint();
                double dx = current.getX() - last.getX();
                double dy = current.getY() - last.getY();

                map.setCenter(
                        new Point(
                                (int) (map.getCenter().getX() - dx),
                                (int) (map.getCenter().getY() - dy)
                        )
                );
                last = current;
            }
        };

        map.addMouseListener(ma);
        map.addMouseMotionListener(ma);

        final GeoPosition[] selected = new GeoPosition[1];
        final Set<Waypoint> baseWaypoints = new HashSet<>();

        baseWaypoints.add(new DefaultWaypoint(48.1486, 17.1077));
        baseWaypoints.add(new DefaultWaypoint(48.7164, 21.2611));
        baseWaypoints.add(new DefaultWaypoint(49.2230, 18.7390));
        baseWaypoints.add(new DefaultWaypoint(48.8945, 18.0444));
        baseWaypoints.add(new DefaultWaypoint(48.3089, 18.0879));

        WaypointPainter<Waypoint> painter = new WaypointPainter<Waypoint>() {
            @Override
            protected void doPaint(Graphics2D g, JXMapViewer map, int w, int h) {
                Rectangle rect = map.getViewportBounds();

                for (Waypoint wp : baseWaypoints) {
                    Point2D pt = map.getTileFactory()
                            .geoToPixel(wp.getPosition(), map.getZoom());

                    int x = (int) (pt.getX() - rect.getX());
                    int y = (int) (pt.getY() - rect.getY());

                    g.setColor(Color.BLUE);
                    g.fillOval(x - 6, y - 6, 12, 12);
                    g.setColor(Color.BLACK);
                    g.setStroke(new BasicStroke(1));
                    g.drawOval(x - 6, y - 6, 12, 12);
                }

                if (selected[0] != null) {
                    Point2D pt = map.getTileFactory()
                            .geoToPixel(selected[0], map.getZoom());

                    int x = (int) (pt.getX() - rect.getX());
                    int y = (int) (pt.getY() - rect.getY());

                    g.setColor(Color.RED);
                    g.fillOval(x - 4, y - 4, 8, 8);
                    g.setColor(Color.BLACK);
                    g.setStroke(new BasicStroke(1));
                    g.drawOval(x - 4, y - 4, 8, 8);
                }
            }
        };
        painter.setWaypoints(baseWaypoints);
        map.setOverlayPainter(painter);

        map.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                Point click = e.getPoint();
                GeoPosition clickedGeo = map.convertPointToGeoPosition(click);

                GeoPosition found = null;

                for (Waypoint wp : baseWaypoints) {
                    Point2D wpPoint = map.getTileFactory()
                            .geoToPixel(wp.getPosition(), map.getZoom());

                    Rectangle rect = map.getViewportBounds();

                    int x = (int) (wpPoint.getX() - rect.getX());
                    int y = (int) (wpPoint.getY() - rect.getY());

                    double dist = click.distance(x, y);

                    if (dist < 15) {
                        found = wp.getPosition();
                        break;
                    }
                }

                if (found != null)
                    selected[0] = found;
                else
                    selected[0] = clickedGeo;

                map.repaint();
            }
        });

        SwingNode swingNode = new SwingNode();
        swingNode.setContent(map);

        Button okButton = new Button("OK");
        okButton.setPrefWidth(120);
        okButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px;");
        okButton.setOnAction(e -> {
            if (selected[0] != null) {
                selectedLat = selected[0].getLatitude();
                selectedLng = selected[0].getLongitude();
                FromCoordsDataGetter.reverse(selectedLat, selectedLng);
            }
            dialog.close();
        });

        HBox buttonBox = new HBox(okButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setStyle("-fx-padding: 15px;");

        BorderPane root = new BorderPane();
        root.setCenter(swingNode);
        root.setBottom(buttonBox);

        Scene scene = new Scene(root);
        dialog.setScene(scene);
        dialog.showAndWait();
    }
}
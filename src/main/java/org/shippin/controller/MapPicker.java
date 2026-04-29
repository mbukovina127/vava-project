package org.shippin.app;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.*;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.Point;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;

public class MapPicker {

public static void open() {

    JDialog dialog = new JDialog();
    dialog.setTitle("Vyber miesto");
    dialog.setSize(900,700);
    dialog.setModal(true);

    JXMapViewer map = new JXMapViewer(); //komponenty mapy z kniznice

    TileFactoryInfo info = new TileFactoryInfo(
            0, 19, 19,
            256, true, true,
            "https://a.tile.openstreetmap.fr/hot", //nacita grafiku mapy
            "x", "y", "z"
    ) {
        public String getTileUrl(int x, int y, int zoom) {
            int z = 19 - zoom;
            return this.baseURL + "/" + z + "/" + x + "/" + y + ".png";
        }
    };

    DefaultTileFactory tileFactory = new DefaultTileFactory(info); 
    map.setTileFactory(tileFactory);

    GeoPosition start = new GeoPosition(48.7, 19.7); //pociatocna pozicia
    map.setAddressLocation(start);
    map.setZoom(5); //pocaitocny zoom
    map.setPanEnabled(true);

    map.addMouseWheelListener(e -> { //ze koliesko mysi ovlada zoom
        if(e.getWheelRotation() < 0)
            map.setZoom(map.getZoom() - 1);
        else
            map.setZoom(map.getZoom() + 1);
    });

    MouseAdapter ma = new MouseAdapter() { //dragovanie myšou
        Point last;

        public void mousePressed(MouseEvent e) {
            last = e.getPoint();
        }

        public void mouseDragged(MouseEvent e) { //posunie sa stred mapy v smere posunu
            Point current = e.getPoint();
            double dx = current.getX() - last.getX();
            double dy = current.getY() - last.getY();

            map.setCenter(
                    new Point(
                            (int)(map.getCenter().getX() - dx),
                            (int)(map.getCenter().getY() - dy)
                    )
            );
            last = current;
        }
    };

    map.addMouseListener(ma);
    map.addMouseMotionListener(ma);

    final GeoPosition[] selected = new GeoPosition[1];
    final Set<Waypoint> baseWaypoints = new HashSet<>();

    //vykreslim nejake markre na mape
    baseWaypoints.add(new DefaultWaypoint(48.1486, 17.1077)); // Bratislava
    baseWaypoints.add(new DefaultWaypoint(48.7164, 21.2611)); // Kosice
    baseWaypoints.add(new DefaultWaypoint(49.2230, 18.7390)); // Zilina
    baseWaypoints.add(new DefaultWaypoint(48.8945, 18.0444)); // Trencin
    baseWaypoints.add(new DefaultWaypoint(48.3089, 18.0879)); // Nitra

    WaypointPainter<Waypoint> painter = new WaypointPainter<>();
    painter.setWaypoints(baseWaypoints);
    map.setOverlayPainter(painter);

    map.addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {

            Point click = e.getPoint();
            GeoPosition clickedGeo = map.convertPointToGeoPosition(click);

            GeoPosition found = null;

            for(Waypoint wp : baseWaypoints) {

                Point2D wpPoint = map.getTileFactory()
                        .geoToPixel(wp.getPosition(), map.getZoom());

                Rectangle rect = map.getViewportBounds();

                int x = (int)(wpPoint.getX() - rect.getX());
                int y = (int)(wpPoint.getY() - rect.getY());

                double dist = click.distance(x, y);

                if(dist < 10) {
                    found = wp.getPosition();
                    break;
                }
            }

            if(found != null)
                selected[0] = found;
            else
                selected[0] = clickedGeo;

            WaypointPainter<Waypoint> painter = new WaypointPainter<Waypoint>() {
                @Override
                protected void doPaint(Graphics2D g, JXMapViewer map, int w, int h) {

                    Rectangle rect = map.getViewportBounds();

                    for(Waypoint wp : baseWaypoints) {

                        Point2D pt = map.getTileFactory()
                                .geoToPixel(wp.getPosition(), map.getZoom());

                        int x = (int)(pt.getX() - rect.getX());
                        int y = (int)(pt.getY() - rect.getY());

                        if(wp.getPosition().equals(selected[0])) {
                            g.setColor(Color.RED);
                            g.fillOval(x-8, y-8, 16, 16);
                        } else {
                            g.setColor(Color.BLUE);
                            g.fillOval(x-6, y-6, 12, 12);
                        }
                    }

                    if(selected[0] != null) {

                        Point2D pt = map.getTileFactory()
                                .geoToPixel(selected[0], map.getZoom());

                        int x = (int)(pt.getX() - rect.getX());
                        int y = (int)(pt.getY() - rect.getY());

                        g.setColor(Color.RED);
                        g.fillOval(x-8, y-8, 16, 16);
                    }
                }
            };

            map.setOverlayPainter(painter);
        }
    });

    JButton ok = new JButton("OK");

    ok.addActionListener(e -> { //tlacitko na potvrdenie
        if(selected[0] != null) { //posielam do funkcie v triede FromCoordsDataGetter x a y vybraneho miesta
            FromCoordsDataGetter.reverse(selected[0].getLatitude(),selected[0].getLongitude());
        }
        dialog.dispose(); //zavrie okno s mapou
    });

    dialog.setLayout(new BorderLayout());
    dialog.add(map, BorderLayout.CENTER);
    dialog.add(ok, BorderLayout.SOUTH);

    dialog.setVisible(true);
}

}
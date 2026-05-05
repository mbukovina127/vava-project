package ServiceTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.shippin.services.MapService;

public class MapServiceTest {

    @Test
    void getInstanceReturnsSameInstance() {
        MapService first = MapService.getInstance();
        MapService second = MapService.getInstance();

        assertNotNull(first);
        assertSame(first, second);
    }

    @Test
    void buildStaticMapUrlContainsExpectedCoordinatesAndMapParameters() {
        MapService service = new MapService();

        double fromLat = 48.1486;
        double fromLon = 17.1077;
        double toLat = 48.7164;
        double toLon = 21.2611;

        String url = service.buildStaticMapUrl(fromLat, fromLon, toLat, toLon);

        String expectedFromCoordinates = String.format("%.4f,%.4f", fromLat, fromLon);
        String expectedToCoordinates = String.format("%.4f,%.4f", toLat, toLon);
        String expectedPath = String.format(
                "path=color:0xff0000|weight:2|%.4f,%.4f|%.4f,%.4f",
                fromLat, fromLon, toLat, toLon
        );

        assertTrue(url.startsWith("https://maps.googleapis.com/maps/api/staticmap?"));
        assertTrue(url.contains("size=900x200"));
        assertTrue(url.contains("markers=color:white|" + expectedFromCoordinates));
        assertTrue(url.contains("markers=color:red|" + expectedToCoordinates));
        assertTrue(url.contains(expectedPath));
        assertTrue(url.contains("&key="));
    }
}
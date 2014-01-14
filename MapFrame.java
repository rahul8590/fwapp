package edu.umass.cs.gns.gnslocationclient;

import edu.umass.cs.gns.client.DesktopGnsClient;
import edu.umass.cs.gns.client.GnsProtocol;
import edu.umass.cs.gns.client.GuidEntry;
import edu.umass.cs.gns.utils.Format;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.json.JSONArray;
import org.json.JSONException;
import org.westy.jmapviewer.DefaultMapController;
import org.westy.jmapviewer.JMapViewer;
import org.westy.jmapviewer.MapMarkerLabeledDot;
import org.westy.jmapviewer.OsmFileCacheTileLoader;
import org.westy.jmapviewer.OsmTileLoader;
import org.westy.jmapviewer.events.JMVCommandEvent;
import org.westy.jmapviewer.interfaces.JMapViewerEventListener;
import org.westy.jmapviewer.interfaces.MapMarker;
import org.westy.jmapviewer.interfaces.TileLoader;
import org.westy.jmapviewer.interfaces.TileSource;
import org.westy.jmapviewer.tilesources.BingAerialTileSource;
import org.westy.jmapviewer.tilesources.MapQuestOpenAerialTileSource;
import org.westy.jmapviewer.tilesources.MapQuestOsmTileSource;
import org.westy.jmapviewer.tilesources.OsmTileSource;

/**
 *
 * Uses {@link JMapViewer} to plot the location of walkers.
 *
 */
public class MapFrame extends JFrame implements JMapViewerEventListener {

  private static final long serialVersionUID = 1L;
  private JMapViewer map = null;
  private JLabel zoomLabel = null;
  private JLabel zoomValue = null;
  private JLabel mperpLabelName = null;
  private JLabel mperpLabelValue = null;
  // we need this to speed up mouse to point lookups
  private ConcurrentMap<String, String> nameToGuidTable = new ConcurrentHashMap<String, String>();

  public MapFrame() {
    super("JMapViewer Demo");
    setSize(400, 400);

    map = new JMapViewer();

    // Listen to the map viewer for user operations so components will
    // recieve events and update
    map.addJMVListener(this);

    // final JMapViewer map = new JMapViewer(new MemoryTileCache(),4);
    // map.setTileLoader(new OsmFileCacheTileLoader(map));
    // new DefaultMapController(map);

    new DefaultMapController(map) {
      @Override
      public void mouseClicked(MouseEvent e) {
        Point point = e.getPoint();
        //System.out.println("POINT: " + point + " " + map.getPosition(point));
        MapMarker marker = map.findMapMarkerNear(point);
        if (marker != null) {
          if (marker instanceof MapMarkerLabeledDot) {
            MapMarkerLabeledDot labeledMarker = (MapMarkerLabeledDot) marker;
            //System.out.println("MARKER: " + labeledMarker.getLabel());
            String walkerName = labeledMarker.getLabel();
            String guid = nameToGuidTable.get(walkerName);
            if (guid != null) {
              System.out.println("GUID: " + guid);
            }
          }
        }
        super.mouseClicked(e);
      }
    };

    setLayout(new BorderLayout());
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setExtendedState(JFrame.MAXIMIZED_BOTH);
    JPanel panel = new JPanel();
    JPanel helpPanel = new JPanel();

    mperpLabelName = new JLabel("Meters/Pixels: ");
    mperpLabelValue = new JLabel(String.format("%s", map.getMeterPerPixel()));

    zoomLabel = new JLabel("Zoom: ");
    zoomValue = new JLabel(String.format("%s", map.getZoom()));

    add(panel, BorderLayout.NORTH);
    add(helpPanel, BorderLayout.SOUTH);
    JLabel helpLabel = new JLabel("Use right mouse button to move,\n "
            + "left double click or mouse wheel to zoom.");
    helpPanel.add(helpLabel);
    JButton button = new JButton("setDisplayToFitMapMarkers");
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        map.setDisplayToFitMapMarkers();
      }
    });
    JComboBox tileSourceSelector = new JComboBox(new TileSource[]{new OsmTileSource.Mapnik(),
              new OsmTileSource.CycleMap(), new BingAerialTileSource(), new MapQuestOsmTileSource(), new MapQuestOpenAerialTileSource()});
    tileSourceSelector.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        map.setTileSource((TileSource) e.getItem());
      }
    });
    JComboBox tileLoaderSelector;
    try {
      tileLoaderSelector = new JComboBox(new TileLoader[]{new OsmFileCacheTileLoader(map),
                new OsmTileLoader(map)});
    } catch (IOException e) {
      tileLoaderSelector = new JComboBox(new TileLoader[]{new OsmTileLoader(map)});
    }
    tileLoaderSelector.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        map.setTileLoader((TileLoader) e.getItem());
      }
    });
    map.setTileLoader((TileLoader) tileLoaderSelector.getSelectedItem());
    panel.add(tileSourceSelector);
    panel.add(tileLoaderSelector);
    final JCheckBox showMapMarker = new JCheckBox("Map markers visible");
    showMapMarker.setSelected(map.getMapMarkersVisible());
    showMapMarker.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        map.setMapMarkerVisible(showMapMarker.isSelected());
      }
    });
    panel.add(showMapMarker);
    final JCheckBox showTileGrid = new JCheckBox("Tile grid visible");
    showTileGrid.setSelected(map.isTileGridVisible());
    showTileGrid.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        map.setTileGridVisible(showTileGrid.isSelected());
      }
    });
    panel.add(showTileGrid);
    final JCheckBox showZoomControls = new JCheckBox("Show zoom controls");
    showZoomControls.setSelected(map.getZoomContolsVisible());
    showZoomControls.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        map.setZoomContolsVisible(showZoomControls.isSelected());
      }
    });
    panel.add(showZoomControls);
    panel.add(button);

    panel.add(zoomLabel);
    panel.add(zoomValue);
    panel.add(mperpLabelName);
    panel.add(mperpLabelValue);

    add(map, BorderLayout.CENTER);

//    //
//    map.addMapMarker(new MapMarkerLabeledDot("One", 49.814284999, 8.642065999));
//    map.addMapMarker(new MapMarkerLabeledDot("Two", 49.91, 8.24));
//    map.addMapMarker(new MapMarkerLabeledDot("Three", 49.71, 8.74));
//    map.addMapMarker(new MapMarkerLabeledDot("Four", 49.51, 7.9));
//    map.addMapMarker(new MapMarkerLabeledDot("Five", 49.8588, 8.643));
//
    //map.setDisplayPositionByLatLon(49.807, 8.6, 10);
    // map.setTileGridVisible(true);
  }

//  /**
//   * @param args
//   */
//  public static void main(String[] args) {
//    // java.util.Properties systemProperties = System.getProperties();
//    // systemProperties.setProperty("http.proxyHost", "localhost");
//    // systemProperties.setProperty("http.proxyPort", "8008");
//    new Demo().setVisible(true);
//  }
  private void updateZoomParameters() {
    if (mperpLabelValue != null) {
      mperpLabelValue.setText(String.format("%s", map.getMeterPerPixel()));
    }
    if (zoomValue != null) {
      zoomValue.setText(String.format("%s", map.getZoom()));
    }
  }

  @Override
  public void processCommand(JMVCommandEvent command) {
    if (command.getCommand().equals(JMVCommandEvent.COMMAND.ZOOM)
            || command.getCommand().equals(JMVCommandEvent.COMMAND.MOVE)) {
      updateZoomParameters();
    }
  }

  public void setDisplayPosition(double lat, double lon, int zoom) {
    map.setDisplayPositionByLatLon(lat, lon, zoom);
  }

  public void update(GuidEntry masterGuid, GuidEntry guid) {
    try {
      DesktopGnsClient gnsClient = new DesktopGnsClient(GNSLocationClient.gnsHost, GNSLocationClient.gnsPort);
      JSONArray result = gnsClient.getGroupMembers(guid.getGuid(), guid);
      Set<MapMarker> newMarkers = new HashSet<MapMarker>();
      for (int i = 0; i < result.length(); i++) {
        String walkerGuid = result.getString(i);
        String walkerName = ObserverWithMap.getInstance().getHumanReadableName(walkerGuid);
        nameToGuidTable.put(walkerName, walkerGuid); // save this for when we do a mouse lookup
        JSONArray location = gnsClient.readFieldList(walkerGuid, GnsProtocol.LOCATION_FIELD_NAME, masterGuid);
        String messages = getMessages(gnsClient, walkerGuid, masterGuid);
        String markerText = walkerName != null ? (walkerName + (!messages.isEmpty() ? ": \n" + messages : "")) : walkerGuid.substring(0, 6);
        newMarkers.add(new MapMarkerLabeledDot(markerText, location.getDouble(1), location.getDouble(0)));
      }
      map.removeAllMapMarkers();
      map.addMapMarker(new MapMarkerLabeledDot("Origin", Color.GREEN, GNSLocationClient.STARTING_POINT.getLat(), GNSLocationClient.STARTING_POINT.getLong()));
      for (MapMarker marker : newMarkers) {
        map.addMapMarker(marker);
      }
    } catch (Exception e) {
      GNSLocationClient.getLogger().warning("Problem accessing GNS during map update: " + e);
    }
  }

  /**
   * Returns the last 6 messages in sorted order.
   * 
   * @param gnsClient
   * @param guid
   * @param masterGuid
   * @return
   * @throws Exception 
   */
  private String getMessages(DesktopGnsClient gnsClient, String guid, GuidEntry masterGuid) throws Exception {
    StringBuilder result = new StringBuilder();
    // a lot of rigamarole because the GNS doesn't guarantee the same order for return value as insertion order in lists 
    ArrayList messagesList = JSONArrayToDateSortedArrayListString(gnsClient.readFieldList(guid, WalkingObserver.MESSAGES_FIELD, masterGuid));
    String prefix = "";
    // only show the last 6 messages
    int startIndex = Math.max(0, messagesList.size() - 6);
    for (int i = startIndex; i < messagesList.size(); i++) {
      result.append(prefix);
      result.append(messagesList.get(i));
      prefix = "\n";
    }
    return result.toString();
  }

  private static ArrayList<String> JSONArrayToDateSortedArrayListString(JSONArray jsonArray) throws JSONException {
    ArrayList<String> list = new ArrayList();
    for (int i = 0; i < jsonArray.length(); i++) {
      list.add(jsonArray.getString(i));
    }
    Collections.sort(list, new StringDateComparator());
    return list;
  }

  public static class StringDateComparator implements Comparator<String> {
    @Override
    public int compare(String s1, String s2) {
      try {
        Date date1 = Format.parseDateTimeOnlyUTC(s1.split(" ")[0]);
        Date date2 = Format.parseDateTimeOnlyUTC(s2.split(" ")[0]);
        //ascending order
        return date1.compareTo(date2);
      } catch (ParseException e) {
        return 0;
      }
      //descending order
      //return date2.compareTo(date1);
    }
  };
}

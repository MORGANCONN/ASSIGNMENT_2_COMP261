import java.awt.*;
import java.util.ArrayList;

public class Segment {
    double segmentLength;
    boolean isSelected;
    String roadId;
    Node startNode, endNode;
    ArrayList<Point> Points = new ArrayList<>();
    ArrayList<Location> segmentLocations = new ArrayList<>();

    public Segment(String roadId, double length, Node startNode, Node endNode, ArrayList<Location> Locations) {
        segmentLength = length;
        this.startNode = startNode;
        this.endNode = endNode;
        this.roadId = roadId;
        this.segmentLocations = Locations;
    }

    /**
     * Draws lines between all of the segments locations with the color depending if the segment is selected
     */
    public void draw(Graphics g, Location origin, double scale) {
        if(isSelected){
            g.setColor(Color.red);
        }
        else{
            g.setColor(Color.black);
        }
        for(int i = 0; i< segmentLocations.size()-1; i++){
            g.drawLine(segmentLocations.get(i).asPoint(origin,scale).x, segmentLocations.get(i).asPoint(origin,scale).y, segmentLocations.get(i+1).asPoint(origin,scale).x, segmentLocations.get(i+1).asPoint(origin,scale).y);
        }
    }

}

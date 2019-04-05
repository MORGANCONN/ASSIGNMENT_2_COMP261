import java.awt.*;
import java.util.ArrayList;

public class Node {
    String nodeID;
    boolean isSelected;
    Point nodePoints;
    Location nodeOriginLocation;
    ArrayList<Segment> outgoingEdges = new ArrayList<>();

    public Node(String Id, Point nodePoints, Location nodeOriginLocation){
        nodeID = Id;
        this.nodePoints = nodePoints;
        this.nodeOriginLocation = nodeOriginLocation;
    }

    /**
     * Calls draw method on all of the outgoing edges and draws the node with the color depending on if the node is selected or not
     */
    public void redraw(Graphics g,Location origin, double scale){
        for(Segment s: outgoingEdges){
            s.draw(g,origin,scale);
        }
        if(isSelected){
            g.setColor(Color.red);
        }
        else{
            g.setColor(Color.blue);
        }
        g.fillOval(nodePoints.x-2,nodePoints.y-2,4,4);
    }

    /**
     * Scales the point of the node depending on supplied scale and origin
     */
    public void scalePoints(double scale, Location origin){
        nodePoints = nodeOriginLocation.asPoint(origin,scale);
    }
}

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;

public class Road {
    String RoadId,StreetName, City;
    boolean Oneway, NotForCar, NotForPedestrain, NotForBicycle;
    Double Length;
    int StartNode, EndNode, Type, Speed, RoadClass;
    ArrayList<Segment> roadSegments = new ArrayList<>();
    ArrayList<Node> roadNodes = new ArrayList<>();

    public Road(String roadId,Double length, int StartNode, int EndNode){
        this.RoadId = roadId;
        this.Length = length;
        this.StartNode = StartNode;
        this.EndNode = EndNode;
    }
    public Road(String roadId,int Type, String StreetName, String City, Boolean OneWay, int Speed, int Roadclass, Boolean notForCar, Boolean notForPedestrain, Boolean notForBicycle){
        this.RoadId = roadId;
        this.Type = Type;
        this.StreetName = StreetName;
        this.City = City;
        this.Oneway = OneWay;
        this.Speed = Speed;
        this.RoadClass = Roadclass;
        this.NotForCar = notForCar;
        this.NotForPedestrain = notForPedestrain;
        this.NotForBicycle = notForBicycle;
    }
    public void addRoadSegments(Segment segment){
        roadSegments.add(segment);
    }

    public void addRoadNodes(Node node){
        roadNodes.add(node);
    }
}

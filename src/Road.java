import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;

public class Road {
    String RoadId,StreetName, City;
    Double Length;
    int StartNode, EndNode, Type, Speed, RoadClass, Oneway, NotForCar, NotForPedestrain, NotForBicycle;
    HashSet<Segment> roadSegments = new HashSet<>();
    HashSet<Node> roadNodes = new HashSet<>();

    public Road(String roadId,Double length, int StartNode, int EndNode){
        this.RoadId = roadId;
        this.Length = length;
        this.StartNode = StartNode;
        this.EndNode = EndNode;
    }
    public Road(String roadId,int Type, String StreetName, String City, int OneWay, int Speed, int Roadclass, int notForCar, int notForPedestrain, int notForBicycle){
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

package MAXPYL;
public class Move{
    GeoArea area;
    Region donor;
    Region receiver;

    public Move(GeoArea area, Region donor, Region receiver)
    {
        this.area = area;
        this.donor = donor;
        this.receiver = receiver;
    }

    public GeoArea get_area()
    {
        return area;
    }

    public Region get_donor()
    {
        return donor;
    }

    public Region getReceiver()
    {
        return receiver;
    }


    public boolean equals(Object obj) {
        Move m = (Move)obj;
        return (area.equals(m.get_area())) && (receiver.equals(m.getReceiver())) && (donor.equals(m.get_donor()));
    }

}
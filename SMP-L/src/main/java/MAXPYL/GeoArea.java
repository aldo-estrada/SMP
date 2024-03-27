package MAXPYL;

import com.google.common.geometry.S2LatLng;
import com.google.common.geometry.S2Point;
import org.locationtech.jts.geom.Coordinate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class GeoArea implements Cloneable , Serializable {

    private int index;
    private long internal_attr;
    private long extensive_attr;
    private List<S2Point> coor_array;
    private S2Point centroid;
    private ArrayList<Integer> neigh_area_index;
    private int associate_region_index;

    public GeoArea(int index , long internal_attr , long extensive_attr , List<S2Point> coor_array)
    {
        this.index = index;
        this.internal_attr = internal_attr;
        this.extensive_attr = extensive_attr;
        this.coor_array = coor_array;
        //set_centroid();
        neigh_area_index = new ArrayList<>();
        associate_region_index = -1;
    }



    public void set_centroid()
    {
//        double total_x = 0.0;
//        double total_y = 0.0;
//        for (Coordinate coordinate : coor_array) {
//            total_x += coordinate.getX();
//            total_y += coordinate.getY();
//        }
//        double ave_x = total_x / coor_array.length;
//        double ave_y = total_y / coor_array.length;
//        centroid = new Coordinate(ave_x , ave_y);
        double total_x = 0.0;
        double total_y = 0.0;
        for (S2Point coordinate : coor_array) {
            S2LatLng coorLatLng = new S2LatLng(coordinate);
            total_x += coorLatLng.latDegrees();
            total_y += coorLatLng.lngDegrees();
        }
        double ave_x = total_x / coor_array.size();
        double ave_y = total_y / coor_array.size();
        S2LatLng latLngCentroid = S2LatLng.fromDegrees(ave_x, ave_y);

        centroid = new S2Point(latLngCentroid.toPoint().getX(), latLngCentroid.toPoint().getY(), latLngCentroid.toPoint().getZ());
    }

    public void set_centroid(S2Point centroid)
    {
        this.centroid = centroid;
    }

    public void set_region(int region_index)
    {
        this.associate_region_index = region_index;
    }

    public void add_neighbor(int add_index)
    {
        neigh_area_index.add(add_index);
    }

    public void set_neighbor_once(ArrayList<Integer> neighbor_to_set)
    {
        this.neigh_area_index = neighbor_to_set;
    }

    public int get_geo_index() { return index; }

    public long get_internal_attr()
    {
        return internal_attr;
    }

    public long get_extensive_attr()
    {
        return extensive_attr;
    }


    public ArrayList<GeoArea> get_neigh_area(ArrayList<GeoArea> all_areas) {
        ArrayList<GeoArea> neigh_areas = new ArrayList<>();
        for(int neigh_index : neigh_area_index)
        {
            neigh_areas.add(all_areas.get(neigh_index));
        }
        return neigh_areas;
    }

    public ArrayList<Integer> get_neigh_area_index()
    {
        return neigh_area_index;
    }

    public int get_associated_region_index() { return associate_region_index; }

    public List<S2Point> get_coordinates() { return coor_array; }

    public S2Point get_centroid() { return centroid; }


    public long compute_hetero(GeoArea neigh_area) {
        return Math.abs(internal_attr - neigh_area.get_internal_attr());
    }

    public void initialize_neighbor() {
        neigh_area_index = new ArrayList<>();
    }



    @Override
    protected Object clone() {
        GeoArea g = new GeoArea(this.get_geo_index() , this.get_internal_attr() , this.get_extensive_attr() , this.get_coordinates());
        g.set_region(this.get_associated_region_index());
        g.set_neighbor_once((ArrayList<Integer>)neigh_area_index.clone());
        g.set_centroid(this.get_centroid());
        return g;
    }


    public static ArrayList<GeoArea> area_list_copy(ArrayList<GeoArea> all_areas) throws CloneNotSupportedException {
        ArrayList<GeoArea> returned_areas = new ArrayList<>();
        for(GeoArea g : all_areas)
        {
            returned_areas.add((GeoArea)g.clone());
        }
        return returned_areas;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GeoArea)) return false;
        return this.get_geo_index() == ((GeoArea) o).get_geo_index();
    }





}

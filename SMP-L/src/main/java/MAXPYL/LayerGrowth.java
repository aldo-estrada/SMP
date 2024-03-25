package MAXPYL;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

public class LayerGrowth {
    public ArrayList<GeoArea> all_areas;
    public ArrayList<Region> regions;
    public boolean[] assigned;
    public long threshold;
    public ArrayList<GeoArea> enclaves;


     public LayerGrowth(ArrayList<GeoArea> all_areas , long threshold)
     {
        this.all_areas = all_areas;
        this.regions = new ArrayList<>();
        this.assigned = new boolean[all_areas.size()];
        this.threshold = threshold;
        this.enclaves = new ArrayList<>();
        Layer_Growth();

        //testing

     }

    public void Layer_Growth()
    {
        //System.out.println("layer growth entered");
        GeoArea starting_area = all_areas.get(new Random().nextInt(all_areas.size()));
        int current_region_id = 0;

        while(true)
        {
            Region new_r = new Region(current_region_id , starting_area , threshold , all_areas);
            assigned[starting_area.get_geo_index()] = true;
            while(!new_r.is_region_complete())
            {
                //System.out.println(new_r.get_region_extensive_attr());
                boolean add_flag = false;
                ArrayList<GeoArea> r_neigh_areas = new_r.get_neigh_areas();

                for(GeoArea area : r_neigh_areas)
                {
                    if(!assigned[area.get_geo_index()])
                    {
                        new_r.add_area_to_region(area);
                        assigned[area.get_geo_index()] = true;
                        add_flag = true;
                        break;
                    }
                }

                if(!add_flag)
                {
                    //System.out.println("broken because no area to add");
                    break;
                }

            }

            if(new_r.is_region_complete())
            {
                regions.add(new_r);
                current_region_id ++;
            }

            else
            {
                //System.out.println("the number of new enclaves area " + new_r.get_areas_in_region().size() + " the current region id is " + new_r.get_region_index());
                enclaves.addAll(new_r.get_areas_in_region());
            }



            starting_area = find_backtrack_area();

            if(starting_area == null)
            {
                return;
            }



        }

    }

    public GeoArea find_backtrack_area()
    {
        for(int i = regions.size() - 1 ; i >= 0 ; i--)
        {
            Region bt_r = regions.get(i);
            for(GeoArea area : bt_r.get_neigh_areas())
            {
                if(!assigned[area.get_geo_index()])
                {
                    //System.out.println("the returned area index is " + area.get_geo_index());
                    return area;
                }
            }

        }

        return null;
    }

    public int get_regions_num()
    {
        return regions.size();
    }

    public ArrayList<GeoArea> get_enclaves()
    {
        return enclaves;
    }

    public ArrayList<GeoArea> get_areas()
    {
        return all_areas;
    }

    public ArrayList<Region> get_regions()
    {
        return regions;
    }

}

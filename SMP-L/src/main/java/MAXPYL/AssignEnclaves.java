package MAXPYL;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class AssignEnclaves {
    ArrayList<GeoArea> all_areas;
    Queue<GeoArea> enclaves;
    Region[] regions;

    public AssignEnclaves(ArrayList<GeoArea> all_areas , ArrayList<GeoArea> enclaves, Region[] regions)
    {
        this.all_areas = all_areas;
        for(GeoArea area : enclaves)
        {
            area.set_region(-1);
        }
        this.enclaves = new LinkedList<>();
        this.enclaves.addAll(enclaves);
        this.regions = regions;
        assign_enclaves();
    }

    private void assign_enclaves()
    {
        while(enclaves.size() != 0)
        {
            //System.out.println("enclaves size " + enclaves.size());
            GeoArea g = enclaves.remove();

            Region optimal_complete_region = find_best_neigh_r(g);
            if(optimal_complete_region != null)
            {
                optimal_complete_region.add_area_to_region(g);
                continue;
            }

            enclaves.add(g);
        }
    }



    /*
        this method is called when all the current area's neighboring regions are complete
        in this case, the algorithm assign the area to the region that minimizes the increment on the heterogeneity
     */
    private Region find_best_neigh_r(GeoArea g)
    {
        ArrayList<Region> region_neigh = new ArrayList<>();

        for(GeoArea current_neigh_area : g.get_neigh_area(all_areas))
        {
            if(current_neigh_area.get_associated_region_index() == -1)
            {
                continue;
            }

            Region associate_region = regions[current_neigh_area.get_associated_region_index()];
            if(!region_neigh.contains(associate_region))
            {
                region_neigh.add(associate_region);
            }
        }


        Region optimal_region = null;
        long optimal_hetero_incre = Long.MAX_VALUE;
        for (Region current_region : region_neigh) {
            long hetero_incre = current_region.compute_hetero_incre(g);
            if (hetero_incre < optimal_hetero_incre) {
                optimal_hetero_incre = hetero_incre;
                optimal_region = current_region;
            }
        }

        return optimal_region;
    }

}

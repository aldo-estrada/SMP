package MAXPYL;

import java.io.IOException;
import java.util.ArrayList;

public class maxp {
    public ArrayList<GeoArea> all_areas;
    public String dataset;
    public long threshold;
    public long total_runtime;
    public int iter;
    public Region[] regions;
    public long best_p;
    public long hetero;
    public long before_hetero;
    public int max_no_improve;
    public double alpha;
    public long parallel_time;
    public long local_search_time;
    public long preprocess_time;
    public long cons_time;
    public long enclaves_time;

    public maxp(long threshold , int iter , int max_no_improve , double alpha , String dataset) throws InterruptedException, CloneNotSupportedException, IOException {
        this.threshold = threshold;
        this.iter = iter;
        this.max_no_improve = max_no_improve;
        this.alpha = alpha;
        this.dataset = dataset;
        maxp_start();
    }

    public void maxp_start() throws CloneNotSupportedException, InterruptedException, IOException {

        long start_preprocess = System.currentTimeMillis();
        all_areas = Preprocess.GeoSetBuilder(dataset);
        long end_preprocess = System.currentTimeMillis();
        preprocess_time += (end_preprocess - start_preprocess);
        System.out.println("Preprocess finished " + preprocess_time + " ms");

        long start_time = System.currentTimeMillis();
        best_p = -1;;
        ArrayList<GeoArea> optimal_all_areas = null;
        ArrayList<GeoArea> optimal_enclaves = null;
        ArrayList<Region> optimal_regions = null;

        long cons_start = System.currentTimeMillis();
        for(int i = 0 ; i < iter ; i++)
        {
            LayerGrowth lg = new LayerGrowth(GeoArea.area_list_copy(all_areas) , threshold);
            int p = lg.get_regions_num();
            if(p > best_p)
            {
                best_p = p;
                optimal_all_areas = lg.get_areas();
                optimal_enclaves = lg.get_enclaves();
                optimal_regions = lg.get_regions();
            }
        }
        long cons_end = System.currentTimeMillis();
        cons_time = cons_end - cons_start;


        assert optimal_regions != null;
        regions = new Region[optimal_regions.size()];
        for(int i = 0 ; i < regions.length ; i++)
        {
            regions[i] = optimal_regions.get(i);
        }

        this.all_areas = optimal_all_areas;

        long enclaves_start = System.currentTimeMillis();
        new AssignEnclaves(all_areas , optimal_enclaves , regions);
        long enclaves_end = System.currentTimeMillis();
        enclaves_time = enclaves_end - enclaves_start;

        long uh_start = System.currentTimeMillis();
        this.before_hetero = Region.get_all_region_hetero(regions);
        UpdatedHeuristics uh = new UpdatedHeuristics(this , max_no_improve , alpha , threshold);
        hetero = uh.getAfter_hetero();
        long uh_end = System.currentTimeMillis();
        local_search_time = uh_end - uh_start;

        long end_time = System.currentTimeMillis();
        this.total_runtime = end_time - start_time;

    }


    public long get_total_time()
    {
        return total_runtime;
    }

    public long get_p()
    {
        return best_p;
    }

    public long get_hetero()
    {
        return hetero;
    }

    public long get_before_hetero() { return before_hetero; }

    public ArrayList<GeoArea> get_areas()
    {
        return all_areas;
    }

    public Region[] get_regions()
    {
        return regions;
    }

    public long get_local_search_time()
    {
        return local_search_time;
    }

    public long get_parallel_time()
    {
        return parallel_time;
    }

    public long getPreprocess_time()
    {
        return preprocess_time;
    }

    public long getCons_time()
    {
        return cons_time;
    }

    public long getEnclaves_time()
    {
        return enclaves_time;
    }


}

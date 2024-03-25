package MAXPYL;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class UpdatedHeuristics {
    private int max_no_improve;
    private ArrayList<Move> tabu_list;
    private double temperature = 1.0;
    private double alpha; //cooling rate
    private ArrayList<GeoArea> all_areas;
    private ArrayList<GeoArea> best_area_label;
    private Region[] regions;
    private Region[] best_regions;
    private long threshold;
    private final int tabu_len = 100;
    private long after_hetero;
    private long total_time;

    public UpdatedHeuristics(maxp mp , int max_no_improve , double alpha,  long threshold) throws CloneNotSupportedException {
        this.max_no_improve = max_no_improve;
        this.alpha = alpha;
        this.all_areas = mp.get_areas();
        this.regions = mp.get_regions();
        this.threshold = threshold;
        tabu_list = new ArrayList<>();
        long start = System.currentTimeMillis();
        heuristic();
        long end = System.currentTimeMillis();
        this.total_time = end - start;
    }


    public ArrayList<GeoArea> get_best_area_label()
    {
        return best_area_label;
    }

    public long get_local_time()
    {
        return total_time;
    }


    public void heuristic() throws CloneNotSupportedException {
        ArrayList<GeoArea> movable_units = new ArrayList<>();
        int no_improving_move = 0;

        long optimal_hetero = Region.get_all_region_hetero(regions);
        //System.out.println("the current hetero is " + optimal_hetero);
        //when no_improving reaches max_no_improve, that means the algorithm didn't update the best solution in #max_no_improve iterations, and we terminate
        while(no_improving_move < max_no_improve)
        {

            if(movable_units.size() == 0)
            {
                movable_units = search_movable_units();
            }

            //we randomly select an area from the movable_units list and process this area based on a greedy method
            Object[] results = greedy_find(movable_units);

            //in this case, all the area's neighbor belongs to the same region or removing this area will cause the region not satisfy the threshold constraint
            if(results.length == 1)
            {
                continue;
            }

            GeoArea area_to_move = (GeoArea)results[0];
            Region donor = regions[area_to_move.get_associated_region_index()];
            Region receiver = (Region)results[1];
            long optimal_hetero_decre = (long)results[2];


            boolean move_flag;

            //suggesting the move increase the heterogeneity of the current partition
            if(optimal_hetero_decre > 0)
            {
                //System.out.println("the first case entered");

                tabu_list.add(new Move(area_to_move , receiver , donor));
                if(tabu_list.size() == tabu_len)
                {
                    tabu_list.remove(0);
                }

                move_flag = true;
                donor.remove_area_in_region(area_to_move);
                receiver.add_area_to_region(area_to_move);

                movable_units.remove(area_to_move);

                long total_hetero = Region.get_all_region_hetero(regions);

                //suggesting the move increase the heterogeneity of the best partition
                if(total_hetero < optimal_hetero)
                {
                    no_improving_move = 0;
                    optimal_hetero = total_hetero;
                    //System.out.println("now the best hetero is " + optimal_hetero);
                    best_area_label = GeoArea.area_list_copy(all_areas);
                }

                //suggesting the move does not increase the heterogeneity of the best partition
                else
                {
                    no_improving_move ++;
                }
            }


            //if the move does not improve the quality of the current partition, whether or not it is accepted depends on the boltzmann probability
            else
            {
                no_improving_move ++;
                double random_num = Math.random();
                double Boltzmann = Math.pow(Math.E , (optimal_hetero_decre / temperature));
                //System.out.println("the optimal_hetero_decre is " + optimal_hetero_decre + " the bolman prob is " + Boltzmann);
                //double Boltzmann = Math.pow(Math.E , (  - ((double)(Region.get_all_region_hetero(regions) - optimal_hetero)/optimal_hetero) / temperature) );
                //double Boltzmann = Math.pow(Math.E , ((optimal_hetero_decre) / temperature));
                if(Boltzmann > random_num)
                {
                    if(tabu_list.contains(new Move(area_to_move , donor , receiver)))
                    {
                        //System.out.println("the seoncd case entered");
                        move_flag = false;
                    }

                    else
                    {
                        tabu_list.add(new Move(area_to_move , receiver , donor));
                        donor.remove_area_in_region(area_to_move);
                        receiver.add_area_to_region(area_to_move);
                        move_flag = true;
                        //System.out.println("the second 2 case entered");
                        //best_area_label = GeoArea.area_list_copy(all_areas);
                    }
                }

                else
                {
                    move_flag = false;
                }

            }
            movable_units.remove(area_to_move);

            if(move_flag)
            {
                ArrayList<GeoArea> area_to_remove = new ArrayList<>();
                for(GeoArea area : movable_units)
                {
                    if((area.get_associated_region_index() == donor.get_region_index()) || (area.get_associated_region_index() == receiver.get_region_index()))
                    {
                        area_to_remove.add(area);
                    }
                }
                movable_units.removeAll(area_to_remove);
                //System.out.println(donor.is_connected());
            }


            temperature = temperature * alpha;


        }

        //System.out.println("now the hetero is " + Region.get_all_region_hetero(regions) +"the optimal hetero is " + optimal_hetero);
        this.after_hetero = optimal_hetero;
    }


    //in this greedy method, the parameter is the list of all movable units, we randomly process one of these movable units and try to ressign the unit
    //to the region with maximum heterogeneity decrease
    public Object[] greedy_find(ArrayList<GeoArea> movable_units)
    {
        GeoArea area = movable_units.get(new Random().nextInt(movable_units.size()));


        int current_r_index = area.get_associated_region_index();

        /*//suggesting that removing this area will cause the region to fall below the threshold
        if(regions[current_r_index].get_region_extensive_attr() - area.get_extensive_attr() < threshold)
        {
            movable_units.remove(area);
            return new Object[]{null};
        }*/


        ArrayList<Region> region_neighbors = new ArrayList<>();

        for(GeoArea neigh_area : area.get_neigh_area(all_areas))
        {
            if(neigh_area.get_associated_region_index() != current_r_index)
            {
                Region r = regions[neigh_area.get_associated_region_index()];
                if(!region_neighbors.contains(r))
                {
                    region_neighbors.add(r);
                }
            }
        }

        if(region_neighbors.size() == 0)
        {
            movable_units.remove(area);
            return new Object[]{null};
        }

        long optimal_hetero_decre = Long.MIN_VALUE;
        Region best_region = null;
        for(Region r : region_neighbors)
        {
            Region belonging_region = regions[area.get_associated_region_index()];
            long hetero_decre = belonging_region.compute_hetero_decre(area) - r.compute_hetero_incre(area);
            if(hetero_decre > optimal_hetero_decre)
            {
                optimal_hetero_decre = hetero_decre;
                best_region = r;
            }

        }
        return new Object[]{area , best_region , optimal_hetero_decre};


    }

    //we assign a thread for each of the regions to find all the movable units from that region
    //here the movable units satisfy: 1.the unit is on the margin 2.removing the unit does not disconnect the region
    /*public ArrayList<GeoArea> parallel_search_movable_units() {
        ArrayList<GeoArea> movable_units = new ArrayList<>();
        ReentrantLock lock = new ReentrantLock();
        ExecutorService threadPool = Executors.newFixedThreadPool(cores);
        //ExecutorService threadPool = Executors.newFixedThreadPool(16);
        ArrayList<ParallelMovableUnitsSearch> tasks = new ArrayList<>();
        for (Region region : regions) {
            tasks.add(new ParallelMovableUnitsSearch(region, movable_units, lock));
        }

        for(ParallelMovableUnitsSearch task : tasks)
        {
            threadPool.execute(task);
        }

        threadPool.shutdown();
        try {
            threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        }
        catch (InterruptedException e)
        {}

        *//*
        ParallelMovableUnitsSearch[] search_thread = new ParallelMovableUnitsSearch[regions.length];
        for(int i = 0 ; i < regions.length ; i++) {
            search_thread[i] = new ParallelMovableUnitsSearch(regions[i], movable_units, lock);
            search_thread[i].start();
        }

        for(int i = 0 ; i < search_thread.length ; i++)
        {
            search_thread[i].join();
        }

         *//*

        return movable_units;
    }*/

    public ArrayList<GeoArea> search_movable_units() {
        ArrayList<GeoArea> movable_units = new ArrayList<>();

        for(Region r : regions)
        {
            ArrayList<GeoArea> r_articulation_pts = new Tarjan(r , all_areas).findAPs_Tarjan();
            ArrayList<GeoArea> movable_areas = (ArrayList<GeoArea>)r.getAreas_on_margin().clone();
            //take the intersect from all the articulation points and areas on the margin
            movable_areas.removeAll(r_articulation_pts);
            ArrayList<GeoArea> movable_areas_ex = new ArrayList<>();
            for(GeoArea area : movable_areas)
            {
                if(r.get_region_extensive_attr() - area.get_extensive_attr() > threshold)
                {
                    movable_areas_ex.add(area);
                }
            }
            movable_units.addAll(movable_areas_ex);

        }


        return movable_units;
    }

    public long getAfter_hetero()
    {
        return after_hetero;
    }



    class ParallelMovableUnitsSearch extends Thread
    {
        Region r;
        ArrayList<GeoArea> all_movable_units;
        ReentrantLock lock;
        ArrayList<GeoArea> areas_in_r;

        public ParallelMovableUnitsSearch(Region r , ArrayList<GeoArea> all_movable_units , ReentrantLock lock)
        {
            this.r = r;
            this.lock = lock;
            this.all_movable_units = all_movable_units;
            areas_in_r = r.get_areas_in_region();
        }

        public void run()
        {
            ArrayList<GeoArea> r_articulation_pts = new Tarjan(r , all_areas).findAPs_Tarjan();
            ArrayList<GeoArea> movable_areas = (ArrayList<GeoArea>)r.getAreas_on_margin().clone();
            //take the intersect from all the articulation points and areas on the margin
            movable_areas.removeAll(r_articulation_pts);
            ArrayList<GeoArea> movable_areas_ex = new ArrayList<>();
            for(GeoArea area : movable_areas)
            {
                if(r.get_region_extensive_attr() - area.get_extensive_attr() > threshold)
                {
                    movable_areas_ex.add(area);
                }
            }
            lock.lock();
            all_movable_units.addAll(movable_areas_ex);
            lock.unlock();
        }





    }










}

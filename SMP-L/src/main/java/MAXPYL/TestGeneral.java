package MAXPYL;

import java.io.IOException;
import java.util.ArrayList;

public class TestGeneral {


    public static void main(String args[]) throws InterruptedException, CloneNotSupportedException, IOException {
        long default_threshold = 250000000;
        int default_iter_con = 1; // change from 40 to 1
        int default_no_improve = 50;
        double default_alpha = 0.9;
        String default_dataset = "30k"; // change from 70k to 10k
        // test commit

        long[] thresholds = new long[]{50000000}; // , 150000000 , 250000000 , 375000000 , 500000000};
        int[] iter_cons = new int[]{20,30,40,50,60};
        int[] no_improves = new int[]{10,30,50,70,90};
        double[] alphas = new double[]{0.9 , 0.998};
        String[] datasets = new String[]{"10k"}; //,"20k","30k","35k","40k","50k","60k","70k"};



        var_threshold(thresholds , default_iter_con , default_no_improve , default_alpha , default_dataset);
     //   var_iter_con(default_threshold , iter_cons , default_no_improve , default_alpha , default_dataset);
       // var_alphas(default_threshold , default_iter_con , default_no_improve , alphas , default_dataset);
       // var_datasets(default_threshold , default_iter_con , default_no_improve , default_alpha , datasets);
       // var_nomove(default_threshold , default_iter_con , no_improves , default_alpha , default_dataset);
       //already commented out //var_cores(default_threshold , default_iter_con , default_no_improve , default_alpha , default_dataset, num_cores);

    }

    public static void var_threshold(long[] threshold , int default_iter_con , int default_no_improve, double default_alpha , String default_dataset) throws IOException, CloneNotSupportedException, InterruptedException {
        System.out.println("the variable is threshold");
        for(long thre : threshold)
        {
            ArrayList<Long> before_hetero = new ArrayList<>();
            ArrayList<Long> hetero = new ArrayList<>();
            ArrayList<Long> runtime = new ArrayList<>();
            ArrayList<Long> p = new ArrayList<>();

            for(int i = 0 ; i < 1 ; i++) // changed from 10 to 1
            {
                maxp mp = new maxp(thre, default_iter_con , default_no_improve , default_alpha , default_dataset);
                runtime.add(mp.get_total_time());
                before_hetero.add(mp.get_before_hetero());
                hetero.add(mp.get_hetero());
                p.add(mp.get_p());
            }
            System.out.println("the current threshold is " + thre + " the avg runtime is " + compute_long_ave(runtime) + " the avg p is " + compute_long_ave(p) + " the avg hetero is " + compute_long_ave(hetero) + "ave before hetero is " + compute_long_ave(before_hetero) + "avg hetero impro " + ((compute_long_ave(before_hetero) - compute_long_ave(hetero)) * 1.0 / compute_long_ave(before_hetero)));
        }
    }


    public static void var_iter_con(long default_threshold, int[] iter_cons , int default_no_improve, double default_alpha, String default_dataset) throws IOException, CloneNotSupportedException, InterruptedException {
        System.out.println("the variable is iteration in construction");
        for(int iter_con : iter_cons)
        {
            ArrayList<Long> hetero = new ArrayList<>();
            ArrayList<Long> runtime = new ArrayList<>();
            ArrayList<Long> p = new ArrayList<>();
            ArrayList<Long> before_hetero = new ArrayList<>();
            for(int i = 0 ; i < 10 ; i++)
            {
                maxp mp = new maxp(default_threshold , iter_con , default_no_improve , default_alpha , default_dataset);
                runtime.add(mp.get_total_time());
                hetero.add(mp.get_hetero());
                before_hetero.add(mp.get_before_hetero());
                p.add(mp.get_p());
            }
            System.out.println("the current iteration in construction is " + iter_con +  "the avg runtime is " + compute_long_ave(runtime) + " the avg p is " + compute_long_ave(p) + " the avg hetero is " + compute_long_ave(hetero) + "ave before hetero is " + compute_long_ave(before_hetero) + "avg hetero impro " + ((compute_long_ave(before_hetero) - compute_long_ave(hetero)) * 1.0 / compute_long_ave(before_hetero)));
        }
    }

    public static void var_nomove(long default_threshold , int default_iter_con , int[] nomoves , double default_alpha , String default_dataset) throws IOException, CloneNotSupportedException, InterruptedException {
        System.out.println("the variable is no move");
        int size = Preprocess.GeoSetBuilder("70k").size();
        for(int nomove : nomoves)
        {
            ArrayList<Long> hetero = new ArrayList<>();
            ArrayList<Long> runtime = new ArrayList<>();
            ArrayList<Long> p = new ArrayList<>();
            ArrayList<Long> before_hetero = new ArrayList<>();

            for(int i = 0 ; i < 10 ; i++)
            {
                maxp mp = new maxp(default_threshold , default_iter_con , nomove , default_alpha , default_dataset);
                runtime.add(mp.get_total_time());
                hetero.add(mp.get_hetero());
                before_hetero.add(mp.get_before_hetero());
                p.add(mp.get_p());
            }
            System.out.println("the current nomove is " + nomove +  "the avg runtime is " + compute_long_ave(runtime) + " the avg p is " + compute_long_ave(p) + " the avg hetero is " + compute_long_ave(hetero) + "ave before hetero is " + compute_long_ave(before_hetero) + "avg hetero impro " + ((compute_long_ave(before_hetero) - compute_long_ave(hetero)) * 1.0 / compute_long_ave(before_hetero)));
        }
    }


    public static void var_alphas(long default_threshold, int default_iter_con, int default_no_move , double[] alphas , String default_dataset) throws IOException, CloneNotSupportedException, InterruptedException {
        System.out.println("the variable is alpha");
        for(double alpha : alphas)
        {
            ArrayList<Long> hetero = new ArrayList<>();
            ArrayList<Long> runtime = new ArrayList<>();
            ArrayList<Long> p = new ArrayList<>();
            ArrayList<Long> before_hetero = new ArrayList<>();

            for(int i = 0 ; i < 10 ; i++)
            {
                maxp mp = new maxp(default_threshold , default_iter_con , default_no_move , alpha , default_dataset);
                runtime.add(mp.get_total_time());
                hetero.add(mp.get_hetero());
                before_hetero.add(mp.get_before_hetero());
                p.add(mp.get_p());
            }
            System.out.println("the current alpha " + alpha +  "the avg runtime is " + compute_long_ave(runtime) + " the avg p is " + compute_long_ave(p) + " the avg hetero is " + compute_long_ave(hetero) + "ave before hetero is " + compute_long_ave(before_hetero) + "avg hetero impro " + ((compute_long_ave(before_hetero) - compute_long_ave(hetero)) * 1.0 / compute_long_ave(before_hetero)));
        }
    }

    public static void var_datasets(long default_threshold , int default_iter_con , int default_no_improve, double default_alpha , String[] datasets) throws IOException, CloneNotSupportedException, InterruptedException {
        System.out.println("the variable is dataset");
        for(String dataset : datasets)
        {
            ArrayList<Long> hetero = new ArrayList<>();
            ArrayList<Long> runtime = new ArrayList<>();
            ArrayList<Long> p = new ArrayList<>();
            ArrayList<Long> before_hetero = new ArrayList<>();

            for(int i = 0 ; i < 10 ; i++)
            {
                maxp mp = new maxp(default_threshold , default_iter_con , default_no_improve , default_alpha , dataset);
                runtime.add(mp.get_total_time());
                hetero.add(mp.get_hetero());
                before_hetero.add(mp.get_before_hetero());
                p.add(mp.get_p());
            }
            System.out.println("the current dataset is " + dataset + " the avg runtime is " + compute_long_ave(runtime) + " the avg p is " + compute_long_ave(p) + " the avg hetero is " + compute_long_ave(hetero) + "ave before hetero is " + compute_long_ave(before_hetero) + "avg hetero impro " + ((compute_long_ave(before_hetero) - compute_long_ave(hetero)) * 1.0 / compute_long_ave(before_hetero)));
        }
    }


    /*public static void var_cores(long default_threshold, int default_iter_con, int default_no_move , double default_alpha , String default_dataset , int[] cores) throws IOException, CloneNotSupportedException, InterruptedException {
        System.out.println("the variable is the number of cores");
        for(int core : cores)
        {
            ArrayList<Long> hetero = new ArrayList<>();
            ArrayList<Long> runtime = new ArrayList<>();
            ArrayList<Long> p = new ArrayList<>();
            ArrayList<Long> before_hetero = new ArrayList<>();
            ArrayList<Long> local_time = new ArrayList<>();
            ArrayList<Long> parallel_time = new ArrayList<>();
            ArrayList<Long> preprocess_time = new ArrayList<>();
            ArrayList<Long> cons_time = new ArrayList<>();
            ArrayList<Long> enclaves_time = new ArrayList<>();
            for(int i = 0 ; i < 10 ; i++)
            {
                maxp mp = new maxp(default_threshold , default_iter_con , default_no_move , default_alpha , default_dataset, core);
                runtime.add(mp.get_total_time());
                hetero.add(mp.get_hetero());
                before_hetero.add(mp.get_before_hetero());
                p.add(mp.get_p());
                local_time.add(mp.get_local_search_time());
                parallel_time.add(mp.get_parallel_time());
                preprocess_time.add(mp.getPreprocess_time());
                cons_time.add(mp.getCons_time());
                enclaves_time.add(mp.getEnclaves_time());
            }
            System.out.println("the current core num is " + core +  "the avg runtime is " + compute_long_ave(runtime) + " the avg p is " + compute_long_ave(p) + " the avg hetero is " + compute_long_ave(hetero) + "ave before hetero is " + compute_long_ave(before_hetero) + "avg hetero impro " + ((compute_long_ave(before_hetero) - compute_long_ave(hetero)) * 1.0 / compute_long_ave(before_hetero)) + "local time is " + compute_long_ave(local_time) + " parallel time is " + compute_long_ave(parallel_time)) ;

        }

    }*/




















    public static long compute_long_ave(ArrayList<Long> long_arr)
    {
        if(long_arr.size() == 0)
        {
            return -1;
        }
        long total = 0;
        for(long l : long_arr)
        {
            total += l;
        }

        return total / long_arr.size();
    }



}

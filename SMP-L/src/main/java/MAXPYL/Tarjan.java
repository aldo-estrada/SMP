package MAXPYL;

import java.util.ArrayList;
import java.util.Arrays;

public class Tarjan
{
    Region r;
    ArrayList<GeoArea> all_areas;
    ArrayList<GeoArea> areas_in_r;
    int time;

    public Tarjan(Region r , ArrayList<GeoArea> all_areas)
    {
        this.r = r;
        this.all_areas = all_areas;
        areas_in_r = r.get_areas_in_region();
        time = 0;
    }

    //targin's algorithm that finds all the articulation points(areas that do not disconnect the region)from a region
    public ArrayList<GeoArea> findAPs_Tarjan()
    {
        ArrayList<GeoArea> r_articulation_points = new ArrayList<>();

        int size = areas_in_r.size();

        int[] disc = new int[size];
        Arrays.fill(disc , -1);

        int[] low = new int[size];
        Arrays.fill(low , -1);

        int[] parent = new int[size];
        Arrays.fill(parent , -1);

        boolean[] articulation_label = new boolean[size];
        Arrays.fill(articulation_label , false);

        for(int i = 0 ; i < size ; i++)
        {
            if(disc[i] == -1)
            {
                DFS(i , disc , low, parent , articulation_label);
            }
        }

        for(int i = 0 ; i < size ; i++)
        {
            if(articulation_label[i])
            {
                r_articulation_points.add(areas_in_r.get(i));
            }
        }

        return r_articulation_points;

    }

    private void DFS(int u , int[] disc, int[] low, int[] parent, boolean[] articulation_label)
    {
        disc[u] = low[u] = time;
        time += 1;
        int children = 0;

        for(GeoArea neigh_area : areas_in_r.get(u).get_neigh_area(all_areas))
        {
            if(areas_in_r.contains(neigh_area))
            {
                int v = areas_in_r.indexOf(neigh_area);

                if(disc[v] == -1)
                {
                    children += 1;
                    parent[v] = u;
                    DFS(v , disc , low , parent , articulation_label);
                    low[u] = Math.min(low[u] , low[v]);

                    if(parent[u] == -1 && children > 1)
                    {
                        articulation_label[u] = true;
                    }

                    if(parent[u] != -1 && low[v] >= disc[u])
                    {
                        articulation_label[u] = true;
                    }
                }

                else if(v != parent[u])
                {
                    low[u] = Math.min(low[u] , disc[v]);
                }
            }
        }
    }

}
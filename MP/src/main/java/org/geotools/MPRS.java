package org.geotools;

import java.nio.charset.Charset;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileWriter;
import java.io.File;

import java.util.*;
import java.util.concurrent.*;
import java.lang.Math;
import java.util.concurrent.locks.ReentrantLock;

import com.opencsv.CSVWriter;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureSource;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.shapefile.dbf.DbaseFileReader;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.FeatureCollection;

import org.locationtech.jts.geom.*;

import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;



class Region {

    private ArrayList<Integer> areas;
    private int ID;
    private long regionalThreshold;
    private long dissimilarity;

    public Region(Region region) {

        this.areas = new ArrayList<>(region.areas);
        this.ID = region.ID;
        this.regionalThreshold = region.regionalThreshold;
        this.dissimilarity = region.dissimilarity;
    }

    public Region(int id) {

        this.areas = new ArrayList<>();
        this.ID = id;
        this.regionalThreshold = 0;
        this.dissimilarity = 0;
    }

    public void setID(int id) {

        this.ID = id;
    }

    public int getID() {

        return this.ID;
    }

    public void addArea(Integer area) {

        this.areas.add(area);
    }

    public void removeArea(Integer area) {

        this.areas.remove(area);
    }

    public ArrayList<Integer> getAreas() {

        return this.areas;
    }

    public void setRegionalThreshold(long threshold) {

        this.regionalThreshold = threshold;
    }

    public long getRegionalThreshold() {

        return this.regionalThreshold;
    }

    public void setDissimilarity(long dissimilarity) {

        this.dissimilarity = dissimilarity;
    }

    public long getDissimilarity() {

        return this.dissimilarity;
    }
}


class Partition {

    private int ID;
    private HashMap<Integer, Region> regions;
    private ArrayList<Integer> enclaves;
    private ArrayList<Integer> assignedAreas;
    private HashMap<Integer, Integer> areasWithRegions;
    private long dissimilarity;

    public Partition(Partition partition) {

        this.ID = partition.ID;
        this.regions = new HashMap<>(partition.regions);
        this.enclaves = new ArrayList<>(partition.enclaves);
        this.assignedAreas = new ArrayList<>(partition.assignedAreas);
        this.areasWithRegions = new HashMap<>(partition.areasWithRegions);
        this.dissimilarity = partition.dissimilarity;
    }

    public Partition(int ID, HashMap<Integer, Region> regions, long dissimilarity) {

        this.ID = ID;
        this.regions = new HashMap<>(regions);
        this.dissimilarity = dissimilarity;
    }

    public Partition(int id) {

        this.ID = id;
        this.regions = new HashMap<>();
        this.enclaves = new ArrayList<>();
        this.assignedAreas = new ArrayList<>();
        this.areasWithRegions = new HashMap<>();
        this.dissimilarity = 0;
    }

    public Partition() {

        this.regions = new HashMap<>();
        this.enclaves = new ArrayList<>();
        this.assignedAreas = new ArrayList<>();
        this.areasWithRegions = new HashMap<>();
        this.dissimilarity = 0;
    }

    public void addRegion(int regionID, Region region) {

        this.regions.put(regionID, region);
    }

    public HashMap<Integer, Region> getRegions() {

        return this.regions;
    }

    public void addEnclaves(ArrayList<Integer> enclave) {

        this.enclaves.addAll(enclave);
    }

    public ArrayList<Integer> getEnclaves() {

        return this.enclaves;
    }

    public void addAssignedAreas(ArrayList<Integer> assignedArea) {

        this.assignedAreas.addAll(assignedArea);
    }

    public ArrayList<Integer> getAssignedAreas() {

        return this.assignedAreas;
    }

    public void resetRegions(HashMap<Integer, Region> newRegions) {

        regions.clear();
        this.regions.putAll(newRegions);
    }

    public void setAreasWithRegions(HashMap<Integer, Integer> areasWithRegions) {

        this.areasWithRegions = areasWithRegions;
    }

    public HashMap<Integer, Integer> getAreasWithRegions() {

        return this.areasWithRegions;
    }

    public void updateAreasWithRegions(int newRegion, int area) {

        this.areasWithRegions.put(area, newRegion);
    }

    public void setDissimilarity(long dissimilarity) {

        this.dissimilarity = dissimilarity;
    }

    public long getDissimilarity() {

        return this.dissimilarity;
    }

    public long calculateDissimilarity() {

        long dissimilarity = 0;

        for (int regionID : this.regions.keySet()) {

            long regionDissimilarity = this.regions.get(regionID).getDissimilarity();
            dissimilarity = dissimilarity + regionDissimilarity;
        }

        return dissimilarity;
    }

    public int getPartitionID() {

        return this.ID;
    }
}


class Move {

    private int partitionID;
    private int recipientRegion;
    private int donorRegion;
    private int movedArea;
    private long donorRegionH;
    private long recipientRegionH;
    private long hetImprovement;

    public Move() {

        this.recipientRegion = 00;
        this.donorRegion = 00;
        this.movedArea = 00;
        this.donorRegionH = 00;
        this.recipientRegionH = 00;
        this.hetImprovement = 00;
    }

    public Move(Move move) {

        this.recipientRegion = move.recipientRegion;
        this.donorRegion = move.donorRegion;
        this.movedArea = move.movedArea;
        this.donorRegionH = move.donorRegionH;
        this.recipientRegionH = move.recipientRegionH;
        this.hetImprovement = move.hetImprovement;
    }


    public void setRecipientRegion(int region) {

        this.recipientRegion = region;
    }

    public void setDonorRegion(int region) {

        this.donorRegion = region;
    }

    public void setMovedArea(int area) {

        this.movedArea = area;
    }

    public void setDonorRegionH(long donorRegionH) {

        this.donorRegionH = donorRegionH;
    }

    public void setRecipientRegionH(long recipientRegionH) {

        this.recipientRegionH = recipientRegionH;
    }

    public void setHetImprovement(long hetImprovement) {

        this.hetImprovement = hetImprovement;
    }

    public int getRecipientRegion() {

        return this.recipientRegion;
    }

    public int getDonorRegion() {

        return this.donorRegion;
    }

    public int getMovedArea() {

        return this.movedArea;
    }

    public long getDonorRegionH() {

        return this.donorRegionH;
    }

    public long getRecipientRegionH() {

        return this.recipientRegionH;
    }

    public long getHetImprovement() {

        return this.hetImprovement;
    }
}



public class MPRS {


    public static Random rand = new Random();

    public static void readFiles(int dataSet,
                                 ArrayList<Long> household,
                                 ArrayList<Long> population,
                                 ArrayList<Geometry> polygons) throws IOException {

        String dbfFile = null;
        String shpFile = null;

        // reading the files on my computer
        /*switch (dataSet) {

            case 1:
                dbfFile = "/Users/hessah/Desktop/Research/Max-P Regions/Datasets/Polygons15/Polygons15.dbf";
                shpFile = "/Users/hessah/Desktop/Research/Max-P Regions/Datasets/Polygons15/Polygons15.shp";
                break;

            case 2:
                dbfFile = "/Users/hessah/Desktop/Research/Max-P Regions/Datasets/Polygons57/Polygons57.dbf";
                shpFile = "/Users/hessah/Desktop/Research/Max-P Regions/Datasets/Polygons57/Polygons57.shp";
                break;

            case 3:
                dbfFile = "/Users/hessah/Desktop/Research/Max-P Regions/Datasets/2K/2K.dbf";
                shpFile = "/Users/hessah/Desktop/Research/Max-P Regions/Datasets/2K/2K.shp";
                break;

            case 4:
                dbfFile = "/Users/hessah/Desktop/Research/Max-P Regions/Datasets/5K/5K.dbf";
                shpFile = "/Users/hessah/Desktop/Research/Max-P Regions/Datasets/5K/5K.shp";
                break;

            case 5:
                dbfFile = "/Users/hessah/Desktop/Research/Max-P Regions/Datasets/10K/10K.dbf";
                shpFile = "/Users/hessah/Desktop/Research/Max-P Regions/Datasets/10K/10K.shp";
                break;

            case 6:
                dbfFile = "/Users/hessah/Desktop/Research/Max-P Regions/Datasets/20K/20K.dbf";
                shpFile = "/Users/hessah/Desktop/Research/Max-P Regions/Datasets/20K/20K.shp";
                break;

            case 7:
                dbfFile = "/Users/hessah/Desktop/Research/Max-P Regions/Datasets/30K/30K.dbf";
                shpFile = "/Users/hessah/Desktop/Research/Max-P Regions/Datasets/30K/30K.shp";
                break;

            case 8:
                dbfFile = "/Users/hessah/Desktop/Research/Max-P Regions/Datasets/40K/40K.dbf";
                shpFile = "/Users/hessah/Desktop/Research/Max-P Regions/Datasets/40K/40K.shp";
                break;

            case 9:
                dbfFile = "/Users/hessah/Desktop/Research/Max-P Regions/Datasets/50K/50K.dbf";
                shpFile = "/Users/hessah/Desktop/Research/Max-P Regions/Datasets/50K/50K.shp";
                break;

            case 10:
                dbfFile = "/Users/hessah/Desktop/Research/Max-P Regions/Datasets/60K/60K.dbf";
                shpFile = "/Users/hessah/Desktop/Research/Max-P Regions/Datasets/60K/60K.shp";
                break;

            case 11:
                dbfFile = "/Users/hessah/Desktop/Research/Max-P Regions/Datasets/70K/70K.dbf";
                shpFile = "/Users/hessah/Desktop/Research/Max-P Regions/Datasets/70K/70K.shp";
                break;

            case 12:
                dbfFile = "/Users/hessah/Desktop/Research/Max-P Regions/Datasets/80K/80K.dbf";
                shpFile = "/Users/hessah/Desktop/Research/Max-P Regions/Datasets/80K/80K.shp";
                break;
        }*/

        // reading the files on the big machine
        /*switch (dataSet) {

            case 1:
                dbfFile = "/home/gepspatial/Desktop/maxp/Datasets/Polygons15/Polygons15.dbf";
                shpFile = "/home/gepspatial/Desktop/maxp/Datasets/Polygons15/Polygons15.shp";
                break;

            case 2:
                dbfFile = "/home/gepspatial/Desktop/maxp/Datasets/Polygons57/Polygons57.dbf";
                shpFile = "/home/gepspatial/Desktop/maxp/Datasets/Polygons57/Polygons57.shp";

                break;

            case 3:
                dbfFile = "/home/gepspatial/Desktop/maxp/Datasets/2K/2K.dbf";
                shpFile = "/home/gepspatial/Desktop/maxp/Datasets/2K/2K.shp";
                break;

            case 4:
                dbfFile = "/home/gepspatial/Desktop/maxp/Datasets/5K/5K.dbf";
                shpFile = "/home/gepspatial/Desktop/maxp/Datasets/5K/5K.shp";
                break;

            case 5:
                dbfFile = "/home/gepspatial/Desktop/maxp/Datasets/10K/10K.dbf";
                shpFile = "/home/gepspatial/Desktop/maxp/Datasets/10K/10K.shp";
                break;

            case 6:
                dbfFile = "/home/gepspatial/Desktop/maxp/Datasets/20K/20K.dbf";
                shpFile = "/home/gepspatial/Desktop/maxp/Datasets/20K/20K.shp";
                break;

            case 7:
                dbfFile = "/home/gepspatial/Desktop/maxp/Datasets/30K/30K.dbf";
                shpFile = "/home/gepspatial/Desktop/maxp/Datasets/30K/30K.shp";
                break;

            case 8:
                dbfFile = "/home/gepspatial/Desktop/maxp/Datasets/40K/40K.dbf";
                shpFile = "/home/gepspatial/Desktop/maxp/Datasets/40K/40K.shp";
                break;

            case 9:
                dbfFile = "/home/gepspatial/Desktop/maxp/Datasets/50K/50K.dbf";
                shpFile = "/home/gepspatial/Desktop/maxp/Datasets/50K/50K.shp";
                break;

            case 10:
                dbfFile = "/home/gepspatial/Desktop/maxp/Datasets/60K/60K.dbf";
                shpFile = "/home/gepspatial/Desktop/maxp/Datasets/60K/60K.shp";
                break;

            case 11:
                dbfFile = "/home/gepspatial/Desktop/maxp/Datasets/70K/70K.dbf";
                shpFile = "/home/gepspatial/Desktop/maxp/Datasets/70K/70K.shp";
                break;

            case 13:
                dbfFile = "/home/gepspatial/Desktop/maxp/Datasets/cousub/cousub.dbf";
                shpFile = "/home/gepspatial/Desktop/maxp/Datasets/cousub/cousub.shp";
                break;
        }*/

        switch (dataSet) {

            case 1:
                dbfFile = "Datasets/Polygons15/Polygons15.dbf";
                shpFile = "Datasets/Polygons15/Polygons15.shp";
                break;

            case 2:
                dbfFile = "Datasets/Polygons57/Polygons57.dbf";
                shpFile = "Datasets/Polygons57/Polygons57.shp";

                break;

            case 3:
                dbfFile = "Datasets/2K/2K.dbf";
                shpFile = "Datasets/2K/2K.shp";
                break;

            case 4:
                dbfFile = "Datasets/5K/5K.dbf";
                shpFile = "Datasets/5K/5K.shp";
                break;

            case 5:
                dbfFile = "Datasets/10K/10K.dbf";
                shpFile = "Datasets/10K/10K.shp";
                break;

            case 6:
                dbfFile = "Datasets/20K/20K.dbf";
                shpFile = "Datasets/20K/20K.shp";
                break;

            case 7:
                dbfFile = "Datasets/30K/30K.dbf";
                shpFile = "Datasets/30K/30K.shp";
                break;

            case 8:
                dbfFile = "Datasets/40K/40K.dbf";
                shpFile = "Datasets/40K/40K.shp";
                break;

            case 9:
                dbfFile = "Datasets/50K/50K.dbf";
                shpFile = "Datasets/50K/50K.shp";
                break;

            case 10:
                dbfFile = "Datasets/60K/60K.dbf";
                shpFile = "Datasets/60K/60K.shp";
                break;

            case 11:
                dbfFile = "Datasets/70K/70K.dbf";
                shpFile = "Datasets/70K/70K.shp";
                break;

            case 13:
                dbfFile = "Datasets/cousub/cousub.dbf";
                shpFile = "Datasets/cousub/cousub.shp";
                break;
        }

        // read dbf file
        FileInputStream fis = new FileInputStream(dbfFile);
        DbaseFileReader dbfReader = new DbaseFileReader(fis.getChannel(),
                false, Charset.forName("ISO-8859-1"));

        while (dbfReader.hasNext()) {
            final Object[] fields = dbfReader.readEntry();
            Long populationField = Long.MIN_VALUE;
            Long householdField = Long.MIN_VALUE;

            if (dataSet == 1 || dataSet == 2) {
                householdField = Long.valueOf((Integer) fields[50]);
                populationField = Long.valueOf((Integer) fields[49]);
            } else if (dataSet == 0 || dataSet == 3) {
                populationField = Long.valueOf((String) fields[15]);
                householdField = Long.valueOf((String) fields[13]);
            } else if (dataSet >= 4 && dataSet <= 12) {
                populationField = Long.valueOf((Long) fields[9]);
                householdField = Long.valueOf((Long) fields[8]);
            } else if (dataSet >= 13) {
                populationField = Long.valueOf((Long) fields[15]);
                householdField = Long.valueOf((Long) fields[14]);
            }

            population.add(populationField);
            household.add(householdField);
        }

        if (dbfReader != null)
            dbfReader.close();
        fis.close();

        // read shape file
        File file = new File(shpFile);
        Map<String, Object> map = new HashMap<>();
        map.put("url", file.toURI().toURL());

        DataStore dataStore = DataStoreFinder.getDataStore(map);
        String typeName = dataStore.getTypeNames()[0];

        FeatureSource<SimpleFeatureType, SimpleFeature> source =
                dataStore.getFeatureSource(typeName);
        Filter filter = Filter.INCLUDE;

        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(filter);

        try (FeatureIterator<SimpleFeature> features = collection.features()) {

            while (features.hasNext()) {

                SimpleFeature feature = features.next();
                //System.out.print(feature.getID());
                //System.out.print(": ");
                //System.out.println(feature.getDefaultGeometryProperty().getValue());
                Geometry polygon = (Geometry) feature.getDefaultGeometry();
                polygons.add(polygon);
            } // end while
        } // end

        dataStore.dispose();
    } // end readFiles


    public static ArrayList<List> createNeighborsList(ArrayList<Geometry> polygons) {

        ArrayList<List> neighbors = new ArrayList<>();

        for (int i = 0; i < polygons.size(); i++) {

            neighbors.add(new ArrayList());
        }


        for (int i = 0; i < polygons.size(); i++) {

            for (int j = i + 1; j < polygons.size(); j++) {

                if (polygons.get(i).intersects(polygons.get(j))) {

                    Geometry intersection = polygons.get(i).intersection(polygons.get(j));

                    if (intersection.getGeometryType() != "Point") {

                        neighbors.get(i).add(j);
                        neighbors.get(j).add(i);

                    } // end if
                } // end if
            } // end for
        } // end for

        return neighbors;
    } // end createNeighborsList


    public static void printNeighborsList(ArrayList<List> neighbors) {

        for (List<Integer> n1 : neighbors) {

            System.out.print(neighbors.indexOf(n1) + ": ");

            for (Integer n2 : n1) {
                System.out.print(n2 + ", ");
            } // end for

            System.out.println();
        } // end for
    } // end printNeighborsList


   public static Partition growRegions(long threshold,
                                        ArrayList<List> neighbors,
                                        ArrayList<Long> household,
                                        ArrayList<Integer> areas) {

        ArrayList<Integer> enclaves = new ArrayList<>();
        ArrayList<Integer> assignedAreas = new ArrayList<>();
        ArrayList<Integer> unassignedAreas = new ArrayList<>(areas);

        Partition partition = new Partition();
        int regionID = 0;

        while (!unassignedAreas.isEmpty()) {

            int seedArea = selectRandomArea(unassignedAreas);
            unassignedAreas.remove((Integer) seedArea);
            assignedAreas.add(seedArea);

            long spatiallyExtensiveAttribute = household.get(seedArea);

            if (spatiallyExtensiveAttribute >= threshold) {

                Region region = new Region(regionID);
                region.addArea(seedArea);
                region.setRegionalThreshold(spatiallyExtensiveAttribute);
                partition.addRegion(regionID, region);

            } // end if

            else if (spatiallyExtensiveAttribute < threshold) {

                Region region = new Region(regionID);
                region.addArea(seedArea);

                ArrayList<Integer> seedAreaNeighbors = new ArrayList<>(neighbors.get(seedArea));
                seedAreaNeighbors.removeAll(assignedAreas);

                int feasible = 1;
                long regionalThreshold = spatiallyExtensiveAttribute;

                while (regionalThreshold < threshold) {

                    if (!seedAreaNeighbors.isEmpty()) {

                        int similarArea = selectRandomArea(seedAreaNeighbors);
                        seedAreaNeighbors.remove((Integer)similarArea);

                        region.addArea(similarArea);

                        List<Integer> similarAreaNeighbors = neighbors.get(similarArea);
                        for (int area : similarAreaNeighbors) {
                            if (!seedAreaNeighbors.contains(area) && !assignedAreas.contains(area))
                                seedAreaNeighbors.add(area);
                        }
                        //similarAreaNeighbors.removeAll(assignedAreas);
                        //similarAreaNeighbors.removeAll(seedAreaNeighbors);
                        //seedAreaNeighbors.addAll(similarAreaNeighbors);

                        regionalThreshold += household.get(similarArea);
                        unassignedAreas.remove((Integer)similarArea);
                        assignedAreas.add(similarArea);

                    } // end if

                    if (seedAreaNeighbors.isEmpty() && regionalThreshold < threshold) {

                        //ArrayList<Integer> regionAreas = new ArrayList<>(region.getAreas());
                        //regionAreas.removeAll(enclaves);
                        enclaves.addAll(region.getAreas());
                        assignedAreas.removeAll(region.getAreas());
                        feasible = 0;
                        break;
                    } // end if
                } // end while

                if (feasible == 1) {
                    region.setRegionalThreshold(regionalThreshold);
                    partition.addRegion(regionID, region);
                }
            } // end else if
            regionID++;
        } // end while

        partition.addEnclaves(enclaves);
        partition.addAssignedAreas(assignedAreas);

        return partition;
    } // end growRegions


    public static Partition growRegionsOptimized(long threshold,
                                        ArrayList<List> neighbors,
                                        ArrayList<Long> household,
                                        ArrayList<Integer> areas) {

        ArrayList<Integer> enclaves = new ArrayList<>();
        HashSet<Integer> assignedAreas = new HashSet<>();
        ArrayList<Integer> unassignedAreas = new ArrayList<>(areas);

        Partition partition = new Partition();
        int regionID = 0;

        while (!unassignedAreas.isEmpty()) {

            int seedArea = selectRandomArea(unassignedAreas);
            //int seedArea = selectRandomArea(new ArrayList<>(unassignedAreas));

            unassignedAreas.remove((Integer)seedArea);
            assignedAreas.add(seedArea);

            long spatiallyExtensiveAttribute = household.get(seedArea);

            if (spatiallyExtensiveAttribute >= threshold) {

                Region region = new Region(regionID);
                region.addArea(seedArea);
                region.setRegionalThreshold(spatiallyExtensiveAttribute);
                partition.addRegion(regionID, region);

            } // end if

            else if (spatiallyExtensiveAttribute < threshold) {

                Region region = new Region(regionID);
                region.addArea(seedArea);

                HashSet<Integer> seedAreaNeighbors = new HashSet<>();
                List<Integer> seedNeighbors = neighbors.get(seedArea);
                for (int neighbor : seedNeighbors) {
                    if (!assignedAreas.contains(neighbor))
                        seedAreaNeighbors.add(neighbor);
                }

                int feasible = 1;
                long regionalThreshold = spatiallyExtensiveAttribute;

                while (regionalThreshold < threshold) {

                    if (!seedAreaNeighbors.isEmpty()) {

                        int similarArea = selectRandomArea(new ArrayList<>(seedAreaNeighbors));
                        /*int similarArea = -1;
                        for (int a : seedAreaNeighbors) {
                            similarArea = a;
                            break;
                        }*/

                        region.addArea(similarArea);
                        seedAreaNeighbors.remove((Integer)similarArea);

                        List<Integer> similarAreaNeighbors = neighbors.get(similarArea);
                        for (int area : similarAreaNeighbors) {
                            if (!assignedAreas.contains(area))
                                seedAreaNeighbors.add(area);
                        }

                        regionalThreshold += household.get(similarArea);
                        unassignedAreas.remove((Integer)similarArea);
                        assignedAreas.add(similarArea);

                    } // end if

                    if (seedAreaNeighbors.isEmpty() && regionalThreshold < threshold) {

                        //ArrayList<Integer> regionAreas = new ArrayList<>(region.getAreas());
                        //regionAreas.removeAll(enclaves);
                        enclaves.addAll(region.getAreas());
                        assignedAreas.removeAll(region.getAreas());
                        feasible = 0;
                        break;
                    } // end if
                } // end while

                if (feasible == 1) {
                    region.setRegionalThreshold(regionalThreshold);
                    partition.addRegion(regionID, region);
                }
            } // end else if
            regionID++;
        } // end while

        partition.addEnclaves(enclaves);
        partition.addAssignedAreas(new ArrayList<>(assignedAreas));

        return partition;
    } // end growRegions



    public static int selectRandomArea(ArrayList<Integer> areas) {

        return areas.get(rand.nextInt(areas.size()));

    } // end selectRandomArea


    public static long calculateDissimilarity(long areaPopulation,
                                              ArrayList<Integer> regionAreas,
                                              ArrayList<Long> population) {

        long dissimilarity = 0;

        for (int i = 0; i < regionAreas.size(); i++) {

            long regionAreaPopulation = population.get(regionAreas.get(i));

            dissimilarity += Math.abs(areaPopulation - regionAreaPopulation);
        }

        return dissimilarity;
    } // end calculateDissimilarity


    public static Partition enclavesAssignment(ArrayList<Long> population,
                                               ArrayList<Long> household,
                                               ArrayList<List> neighbors,
                                               Partition currentPartition) {

        ArrayList<Integer> assignedAreas = currentPartition.getAssignedAreas();
        ArrayList<Integer> enclaves = currentPartition.getEnclaves();
        HashMap<Integer, Region> regions = currentPartition.getRegions();
        HashMap<Integer, Integer> areasWithRegions = currentPartition.getAreasWithRegions();

        while (!enclaves.isEmpty()) {

            Integer enclave = selectEnclave(assignedAreas, enclaves, neighbors);

            HashSet<Region> neighboringRegions = new HashSet<>();

            List<Integer> enclaveNeighbors = neighbors.get(enclave);

            for (Integer neighbor : enclaveNeighbors) {

                if (areasWithRegions.containsKey(neighbor)) {

                    int regionID = areasWithRegions.get(neighbor);
                    Region region = regions.get(regionID);

                        neighboringRegions.add(region);

                }
            }

            Region similarRegion = getSimilarRegion(enclave, population, neighboringRegions);

            long enclaveHousehold = household.get(enclave);
            long regionThreshold = similarRegion.getRegionalThreshold();
            long updatedThreshold = enclaveHousehold + regionThreshold;

            similarRegion.setRegionalThreshold(updatedThreshold);
            similarRegion.addArea(enclave);
            areasWithRegions.put(enclave, similarRegion.getID());
            assignedAreas.add(enclave);
            enclaves.remove(enclave);

        } // end while

        return currentPartition;
    }


    public static int selectEnclave(ArrayList<Integer> assignedAreas,
                                    ArrayList<Integer> enclaves,
                                    ArrayList<List> neighbors) {

        Integer enclave = null;

        loop:
        for (int i = 0; i < enclaves.size(); i++) {

            int currentEnclave = enclaves.get(i);

            for (int j = 0; j < assignedAreas.size(); j++) {

                List<Integer> areaNeighbors = neighbors.get(assignedAreas.get(j));

                if (areaNeighbors.contains(currentEnclave)) {

                    enclave = currentEnclave;
                    break loop;
                }
            }
        } // end for

        return enclave;
    } // end selectEnclave


    public static Region getSimilarRegion(int enclave,
                                          ArrayList<Long> population,
                                          HashSet<Region> neighboringRegions) {

        Region similarRegion = null;
        long minDissimilarity = Long.MAX_VALUE;
        long enclavePopulation = population.get(enclave);

        for (Region region : neighboringRegions) {

            ArrayList<Integer> areas = region.getAreas();
            long dissimilarity = calculateDissimilarity(enclavePopulation, areas, population);


            if (dissimilarity < minDissimilarity) {

                similarRegion = region;
                minDissimilarity = dissimilarity;
            }
        }

        return similarRegion;
    } // end getSimilarRegion


    public static boolean checkSpatialContiguity(ArrayList<Integer> areas,
                                                 ArrayList<List> neighbors) {

        boolean connected = true;

        int numOfRegions = 0;

        HashMap<Integer, Boolean> visitedAreas = new HashMap<>();

        // fill the hash map (key: area, value: false)
        for (int i = 0; i < areas.size(); i++) {

            visitedAreas.put(areas.get(i), false);

        } // end for

        for (Integer area : visitedAreas.keySet()) {

            if (visitedAreas.get(area) == false) {

                listTraversal(visitedAreas, area, areas, neighbors);
                numOfRegions++;

                if (numOfRegions > 1) {
                    connected = false;
                    break;
                } //end if
            } // end if
        } // end for

        //System.out.println("numOfRegions: " + numOfRegions);
        return connected;
    }


    public static void listTraversal(HashMap<Integer,
            Boolean> visitedAreas,
                                     int area,
                                     ArrayList<Integer> areas,
                                     ArrayList<List> neighbors) {

        visitedAreas.put(area, true);

        List<Integer> neighborsList = neighbors.get(area);

        for (Integer neighbor : neighborsList) {

            if (areas.contains(neighbor)) {

                if (visitedAreas.get(neighbor) == false) {

                    listTraversal(visitedAreas, neighbor, areas, neighbors);

                } // end if
            } // end if
        } // end for
    }


    public static HashMap<Integer, Integer> createAreasWithRegions(Partition feasiblePartition) {

        // create a list of areas with their region ID
        HashMap<Integer, Integer> areasWithRegions = new HashMap<>();
        HashMap<Integer, Region> partitionRegions = feasiblePartition.getRegions();
        for (Integer regionID : partitionRegions.keySet()) {

            Region region = partitionRegions.get(regionID);
            ArrayList<Integer> areas = region.getAreas();

            for (int j = 0; j < areas.size(); j++) {
                areasWithRegions.put(areas.get(j), regionID);
            } // end for
        } // end for

        return areasWithRegions;
    }


    public static long calculatePartitionH(Partition partition,
                                           ArrayList<Long> population) {

        long H = 0;

        HashMap<Integer, Region> regions = partition.getRegions();

        for (Integer regionID : regions.keySet()) {

            H += calculateRegionH(regions.get(regionID), population);

        }

        partition.setDissimilarity(H);

        return H;
    }


    public static long calculateRegionH(Region region,
                                        ArrayList<Long> population) {

        long H = 0;

        ArrayList<Integer> areas = region.getAreas();


        for (int i = 0; i < areas.size(); i++) {

            for (int j = i + 1; j < areas.size(); j++) {

                H += Math.abs(Math.abs(population.get(areas.get(i))) - Math.abs(population.get(areas.get(j))));

            }
        }

        region.setDissimilarity(H);

        return H;
    }


    public static Partition modifiedSA(int lengthTabu,
                                        int max_no_improve,
                                        double alpha,
                                        double t,
                                        long threshold,
                                        Partition feasiblePartition,
                                        ArrayList<Long> household,
                                        ArrayList<Long> population,
                                        ArrayList<List> neighbors) {

        // p = feasiblePartition
        Partition p = new Partition(feasiblePartition);
        //currentP = feasiblePartition
        Partition currentP = new Partition(feasiblePartition);
        long pDissimilarity = p.getDissimilarity();
        long currentPDissimilarity = currentP.getDissimilarity();

        ArrayList<Move> tabuList = new ArrayList<>();
        ArrayList<Integer> movable_units = new ArrayList<>();
        int no_improving_move = 0;

        while (no_improving_move < max_no_improve) {

            //System.out.println("c = " + c);

            if (movable_units.isEmpty()) {
                movable_units = sequential_search_movable_units(currentP, neighbors);
                if (movable_units.isEmpty()) {
                    break;
                }
            } // end if

            Move move = selectRandomMove(movable_units, household, population, currentP, threshold, neighbors);

            if (move == null)
                continue;


            int area_to_move = move.getMovedArea();
            int donor = move.getDonorRegion();
            int receiver = move.getRecipientRegion();
            long improvement = move.getHetImprovement();

            boolean moveFlag;

            long newPDissimilarity = currentPDissimilarity - improvement; // subtraction because positive improvement means decrease in het and negative improvement means increase in het
            if (improvement > 0) {

                makeMove(currentP, move, household.get(move.getMovedArea()), population, newPDissimilarity);
                currentPDissimilarity = newPDissimilarity;
                moveFlag = true;
                movable_units.remove((Integer)area_to_move);

                if (tabuList.size() == lengthTabu)
                    tabuList.remove(0);

                tabuList.add(move);

                if (currentPDissimilarity < pDissimilarity) {

                    no_improving_move = 0;
                    p.resetRegions(currentP.getRegions());
                    pDissimilarity = newPDissimilarity;
                }

                else
                {
                    no_improving_move ++;
                }
            } // end if

            else {

                //System.out.println(newPDissimilarity + " > "+ pDissimilarity);

                no_improving_move ++;
                double probability =  Math.pow(Math.E, (improvement / t));

                if (probability > Math.random())
                {
                    //System.out.println("prob > random");

                    if (isTabu(move, tabuList)) {

                        moveFlag = false;
                        movable_units.remove((Integer)area_to_move);
                    }

                    else {

                        makeMove(currentP, move, household.get(move.getMovedArea()), population, newPDissimilarity);
                        currentPDissimilarity = newPDissimilarity;
                        moveFlag = true;
                        movable_units.remove((Integer)area_to_move);

                    }

                } // end if

                else {
                    moveFlag = false;
                    movable_units.remove((Integer)area_to_move);

                } // end else
            } // end else

            if (moveFlag) {

                /*ArrayList<Integer> toRemove = new ArrayList<>(currentP.getRegions().get(donor).getAreas());
                toRemove.removeAll(currentP.getRegions().get(receiver).getAreas());
                toRemove.addAll(currentP.getRegions().get(receiver).getAreas());*/

                for (int area : currentP.getRegions().get(donor).getAreas())
                    movable_units.remove((Integer)area);
                for (int area : currentP.getRegions().get(receiver).getAreas())
                    movable_units.remove((Integer)area);

            } //end if

            t = t * alpha;

        } // end while

        p.setDissimilarity(pDissimilarity);
        return p;
    } // end modifiedSA


    public static ArrayList<Integer> sequential_search_movable_units(Partition currentP,
                                                                     ArrayList<List> neighbors) {

        ArrayList<Integer> movable_units = new ArrayList<>();
        ArrayList<Integer> borderAreas = new ArrayList<>();
        HashMap<Integer, Integer> areasWithRegions = currentP.getAreasWithRegions();
        HashMap<Integer, Region> regions = currentP.getRegions();

        for (int area1 : areasWithRegions.keySet()) {

            List<Integer> area1neighbors = neighbors.get(area1);

            for (int area2 : area1neighbors) {

                if (areasWithRegions.get(area1) != areasWithRegions.get(area2)) {
                    if (!borderAreas.contains(area2)) {
                        borderAreas.add(area2);
                    }
                }
            }
        }

        for (int area : borderAreas) {

            ArrayList<Integer> updatedRegion = new ArrayList<>(regions.get(areasWithRegions.get(area)).getAreas());
            updatedRegion.remove((Integer) area);
            boolean connected = checkSpatialContiguity(updatedRegion, neighbors);

            if (connected)
                movable_units.add(area);
        }

        return movable_units;

    }


    public static Move selectRandomMove(ArrayList<Integer> movable_units,
                                        ArrayList<Long> household,
                                        ArrayList<Long> population,
                                        Partition currentP,
                                        Long threshold,
                                        ArrayList<List> neighbors) {

        Move move = new Move();

        int area = selectRandomArea(movable_units);
        HashMap<Integer, Integer> areasWithRegions = currentP.getAreasWithRegions();
        HashMap<Integer, Region> regions = currentP.getRegions();
        int areaRegionID = areasWithRegions.get(area);
        Region areaRegion = regions.get(areaRegionID);

        if (areaRegion.getRegionalThreshold() - household.get(area) < threshold) {
            movable_units.remove((Integer)area);
            return null;
        }

        HashSet<Integer> neighboringRegions = new HashSet<>();
        List<Integer> areaNeighbors = neighbors.get(area);

        for (int neighbor : areaNeighbors) {

            int neighborRegionID = areasWithRegions.get(neighbor);

            if (neighborRegionID != areaRegionID) {

                neighboringRegions.add(neighborRegionID);

            }
        }

        if (neighboringRegions.isEmpty()) {
            movable_units.remove((Integer)area);
            return null;
        }

        long optimal_hetero_decre = Long.MIN_VALUE;
        long optimal_r1Hetero = 0;
        long optimal_r2Hetero = 0;
        int best_region = -1;

        for (Integer r : neighboringRegions)
        {

            long r1Hetero = 0;
            ArrayList<Integer> r1_areas = areaRegion.getAreas();
            for (int area1 : r1_areas) {
                if (area1 != area)
                    r1Hetero += Math.abs(population.get(area) - population.get(area1));
            }

            long r2Hetero = 0;
            ArrayList<Integer> r2_areas = regions.get(r).getAreas();
            for (int area2 : r2_areas) {
                r2Hetero += Math.abs(population.get(area) - population.get(area2));
            }

            long hetero_decre = r1Hetero - r2Hetero;//heteroChange(areaRegion, regions.get(r), population, area, move);
            if(hetero_decre > optimal_hetero_decre)
            {
                optimal_hetero_decre = hetero_decre;
                best_region = r;
                optimal_r1Hetero = r1Hetero;
                optimal_r2Hetero = r2Hetero;
            }

        }

        move.setRecipientRegion(best_region);
        move.setDonorRegion(areaRegion.getID());
        move.setMovedArea(area);
        move.setHetImprovement(optimal_hetero_decre);
        move.setDonorRegionH(optimal_r1Hetero);
        move.setRecipientRegionH(optimal_r2Hetero);

        return move;
    } // end selectRandomMove


    public static boolean isTabu(Move move, ArrayList<Move> tabuList) {

        boolean isTabu = false;

        int donorRegionID = move.getDonorRegion();
        int recipientRegionID = move.getRecipientRegion();
        int areaID = move.getMovedArea();

        for (int i = 0; i < tabuList.size(); i++) {

            Move tabuMove = tabuList.get(i);

            int donorRegionTabuID = tabuMove.getRecipientRegion();
            int recipientRegionTabuID = tabuMove.getDonorRegion();
            int areaTabuID = tabuMove.getMovedArea();

            if ((donorRegionID == donorRegionTabuID)
                    && (recipientRegionID == recipientRegionTabuID)
                    && (areaID == areaTabuID)) {

                isTabu = true;
                //System.out.println("tabu move is found.");
                break;
            }

        }

        return isTabu;
    }


    public static void makeMove(Partition currentP,
                                 Move move,
                                 long movedAreaHousehold,
                                 ArrayList<Long> population,
                                 long newPDissimilarity) {

        HashMap<Integer, Region> regions = currentP.getRegions();
        Region donorRegion = regions.get(move.getDonorRegion());
        Region recipientRegion = regions.get(move.getRecipientRegion());
        int movedArea = move.getMovedArea();

        long newDonorThreshold = Math.abs(donorRegion.getRegionalThreshold() - movedAreaHousehold);
        long newRecipientThreshold = Math.abs(recipientRegion.getRegionalThreshold() + movedAreaHousehold);
        donorRegion.setRegionalThreshold(newDonorThreshold);
        recipientRegion.setRegionalThreshold(newRecipientThreshold);

        donorRegion.removeArea((Integer)movedArea);
        //regions.put(donorRegion.getID(), donorRegion);

        recipientRegion.addArea(movedArea);
        //regions.put(recipientRegion.getID(), recipientRegion);

        long donorHet = calculateRegionH(donorRegion, population);
        long recipientHet = calculateRegionH(recipientRegion, population);
        donorRegion.setDissimilarity(donorHet);
        recipientRegion.setDissimilarity(recipientHet);

        currentP.getAreasWithRegions().put(movedArea, recipientRegion.getID());
        currentP.setDissimilarity(newPDissimilarity);
    }





    public static void main(String[] args) throws IOException, InterruptedException {


        // arguments for the jar file execution

        /*int maxItr = Integer.parseInt(args[0]);
        long threshold = Long.parseLong(args[1]);
        int convSA = Integer.parseInt(args[2]);
        int lengthTabu = 100;
        double t = 1;
        double alpha = Double.parseDouble(args[3]);
        int dataset = Integer.parseInt(args[4]);
        int optimized = Integer.parseInt(args[5]);*/


        int maxItr = 40;
        long threshold = 250000000;
        int lengthTabu = 100;
        double t = 1;
        int convSA = 90;
        double alpha = 0.9;
        int dataset = 11;
        int optimized = 1;


        System.out.println("maxItr: " + maxItr);
        System.out.println("Threshold: " + threshold);
        System.out.println("convSA: " + convSA);
        System.out.println("lengthTabu: " + lengthTabu);
        System.out.println("t: " + t);
        System.out.println("alpha: " + alpha);
        System.out.println("Dataset: " + dataset);
        System.out.println("Optimized: " + optimized);
        System.out.println();


        long start = System.currentTimeMillis();


        // ********************************************************************
        // **** READING FILES ****
        // ********************************************************************

        System.out.println("-----------------------------------------------------------------------------------");
        System.out.println("Reading files . . .");
        System.out.println("-----------------------------------------------------------------------------------");

        long startReadingFile = System.currentTimeMillis();

        // dissimilarity attribute col#9 j AWATER
        ArrayList<Long> population = new ArrayList<>();
        // spatially extensive attribute col#8 i ALAND
        ArrayList<Long> household = new ArrayList<>();
        // areas as polygons
        ArrayList<Geometry> polygons = new ArrayList<>();

        readFiles(dataset, household, population, polygons);

        ArrayList<Integer> areas = new ArrayList<>();
        for (int i = 0; i < polygons.size(); i++) {
            areas.add(i);
        }

        System.out.println("Number of areas: " + areas.size());

        long endReadingFile = System.currentTimeMillis();
        float totalReadingFile = (endReadingFile - startReadingFile) / 1000F;
        System.out.println("Total time for Reading the Files is : " + totalReadingFile + "\n\n");


        // ********************************************************************
        // **** FINDING THE NEIGHBORS FOR EACH AREA ****
        // ********************************************************************

        System.out.println("-----------------------------------------------------------------------------------");
        System.out.println("Finding neighbors . . .");
        System.out.println("-----------------------------------------------------------------------------------");

        long startFindingNeighbors = System.currentTimeMillis();

        ArrayList<List> neighbors = createNeighborsList(polygons);

        long endFindingNeighbors = System.currentTimeMillis();
        float totalFindingNeighbors = (endFindingNeighbors - startFindingNeighbors) / 1000F;
        System.out.println("Total time for Finding the Neighbors is: " + totalFindingNeighbors + "\n\n");

        // print neighbors list
        //printNeighborsList(neighbors);


        // ********************************************************************
        // **** CONSTRUCTION PHASE ****
        // ********************************************************************

        int maxP = 0;
        Partition bestP = new Partition();
        ArrayList<Partition> partitionsBeforeEnclaves = new ArrayList<>();
        ArrayList<Partition> feasiblePartitions = new ArrayList<>();

        // **** GROW REGIONS ****

        System.out.println("-----------------------------------------------------------------------------------");
        System.out.println("Growing the regions . . .");
        System.out.println("-----------------------------------------------------------------------------------");

        long startGrowRegions = System.currentTimeMillis();

        for (int itr = 0; itr < maxItr; itr++) {
            //System.out.println("Iteration " + itr + " . . .");

            Partition partition;
            if (optimized == 0)
                partition = growRegions(threshold, neighbors, household, areas);
            else
                partition = growRegionsOptimized(threshold, neighbors, household, areas);

            int p = partition.getRegions().size();
            //System.out.println("Number of regions: " + p);

            if (p > maxP) {

                HashMap<Integer, Integer> areasWithRegions = createAreasWithRegions(partition);
                partition.setAreasWithRegions(areasWithRegions);

                partitionsBeforeEnclaves.clear();
                partitionsBeforeEnclaves.add(partition);
                maxP = p;

            } else if (p == maxP) {

                HashMap<Integer, Integer> areasWithRegions = createAreasWithRegions(partition);
                partition.setAreasWithRegions(areasWithRegions);

                partitionsBeforeEnclaves.add(partition);

            } else if (p < maxP) {

                // pass
            }
        } // end for

        System.out.println("\nMaxP: " + maxP);
        System.out.println("Number of partitions after growing the regions: " + partitionsBeforeEnclaves.size());

        long endGrowRegions = System.currentTimeMillis();
        float totalGrowRegions = (endGrowRegions - startGrowRegions) / 1000F;
        System.out.println("Total time for Growing the Regions is : " + totalGrowRegions + "\n\n");

        // writing the grow regions phase output to a CSV file
        //writingPartitionsToFile(partitionsBeforeEnclaves, polygons, "");


        // **** ENCLAVES ASSIGNMENT ****

        System.out.println("-----------------------------------------------------------------------------------");
        System.out.println("Enclaves Assignment . . .");
        System.out.println("-----------------------------------------------------------------------------------");

        long startAssignEnclaves = System.currentTimeMillis();

        for (int i = 0; i < partitionsBeforeEnclaves.size(); i++) {

            Partition currentPartition = partitionsBeforeEnclaves.get(i);
            Partition feasiblePartition = enclavesAssignment(population, household, neighbors, currentPartition);
            long heterogeneity = calculatePartitionH(feasiblePartition, population);
            feasiblePartition.setDissimilarity(heterogeneity);
            feasiblePartitions.add(feasiblePartition);
        }

        // writing enclaves assignment output to a cvs file
        //writingPartitionsToFile(feasiblePartitions, polygons, "/Users/hessah/Desktop/Output/enclaves");

        long endAssignEnclaves = System.currentTimeMillis();
        float totalAssignEnclaves = (endAssignEnclaves - startAssignEnclaves) / 1000F;
        System.out.println("Total time for Assign Enclaves is : " + totalAssignEnclaves + "\n\n");


        // ********************************************************************
        // **** LOCAL SEARCH PHASE ****
        // ********************************************************************

        System.out.println("-----------------------------------------------------------------------------------");
        System.out.println("Local Search . . .");
        System.out.println("-----------------------------------------------------------------------------------");

        long startSearch = System.currentTimeMillis();

        long improvement;
        long oldHeterogeneity = Long.MIN_VALUE;
        long minDissimilarity = Long.MAX_VALUE;

        for (Partition feasiblePartition : feasiblePartitions) {

            long oldPartitionDissimilarity = feasiblePartition.getDissimilarity();

            Partition partition = modifiedSA(lengthTabu, convSA, alpha, t, threshold, feasiblePartition, household, population, neighbors);

            if (partition.getDissimilarity() < minDissimilarity) {
                bestP = new Partition(partition);
                minDissimilarity = partition.getDissimilarity();
                oldHeterogeneity = oldPartitionDissimilarity;
            }
        }

        long pH = bestP.getDissimilarity();

        improvement = oldHeterogeneity - pH;
        float percentage = ((float)improvement / (float)oldHeterogeneity);
        System.out.println("Heterogeneity before local search: " + oldHeterogeneity);
        System.out.println("Heterogeneity after local search: " + pH);
        System.out.println("Improvement in heterogeneity: " + improvement);
        System.out.println("Percentage of improvement: " + percentage);
        System.out.println("Max-p: " + maxP);

        long endSearch = System.currentTimeMillis();
        float totalSearch = (endSearch - startSearch) / 1000F;
        System.out.println("Total time for Local Search is : " + totalSearch);

        long end = System.currentTimeMillis();
        float totalTime = totalFindingNeighbors + totalGrowRegions + totalAssignEnclaves + totalSearch;
        System.out.println("\nTotal time is : " + (totalTime));


        // writing the final output to a CSV file

        /*
        FileWriter outputFile = new FileWriter(
                new File("/Users/hessah/Desktop/BestPartition" + bestP.getPartitionID() + ".csv"));
        CSVWriter csv = new CSVWriter(outputFile);

        String[] header = {"Region ID", "Area ID", "Area Polygon in WKT"};
        csv.writeNext(header);

        HashMap<Integer, Region> regionsList = bestP.getRegions();

        for (Integer regionID : regionsList.keySet()) {

            ArrayList<Integer> areasList = regionsList.get(regionID).getAreas();
            String ID = String.valueOf(regionID);

            for (int area : areasList) {

                String[] row = {ID, String.valueOf(area), polygons.get(area).toText()};
                csv.writeNext(row);
            }
        }
        csv.close();
        */

        String path;
        if (optimized == 0)
            //path = "/home/gepspatial/Desktop/maxp/MPRS.csv";
            path = "MPRS.csv";
        else
            //path = "/home/gepspatial/Desktop/maxp/MPRSOptimized.csv";
            path = "MPRSOptimized.csv";

        FileWriter outputFile = new FileWriter(
                new File(path), true);
        CSVWriter csv = new CSVWriter(outputFile);

        String[] row = {String.valueOf(dataset), String.valueOf(maxItr), String.valueOf(threshold), String.valueOf(convSA), String.valueOf(alpha),
                String.valueOf(maxP), String.valueOf(oldHeterogeneity), String.valueOf(pH), String.valueOf(improvement), String.valueOf(percentage),
                String.valueOf(totalReadingFile), String.valueOf(totalFindingNeighbors), String.valueOf(totalGrowRegions), String.valueOf(totalAssignEnclaves),
                String.valueOf(totalSearch), String.valueOf(totalTime)};
        csv.writeNext(row);

        csv.close();


    } // end main
} // end class MPRS

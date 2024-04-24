package MAXPYL;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

import com.google.common.geometry.*; // S2 library


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Preprocess {
    private static ArrayList<Double> list_vals = new ArrayList<>();

    public static ArrayList<GeoArea> GeoSetBuilder(String dataset) throws IOException {
        ArrayList<GeoArea> geoAreas = new ArrayList<>();
        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = preprocess(dataset);
        ArrayList<S2CellUnion> polylines = initial_construct(collection, geoAreas, dataset);
        //System.out.println("construction finished");
        setNeighborsCells(polylines, geoAreas);
        //System.out.println("setNeighbor finished");
        return geoAreas;
    }

    private static FeatureCollection<SimpleFeatureType, SimpleFeature> preprocess(String dataset) throws IOException {

        File file = null;
        switch (dataset) {
            case "10k":
                file = new File("Datasets/10K/10K.shp");
                break;
            case "20k":
                file = new File("Datasets/20K/20K.shp");
                break;
            case "30k":
                file = new File("Datasets/30K/30K.shp");
                break;
            case "40k":
                file = new File("Datasets/40K/40K.shp");
                break;
            case "50k":
                file = new File("Datasets/50K/50K.shp");
                break;
            case "60k":
                file = new File("Datasets/60K/60K.shp");
                break;
            case "70k":
                file = new File("Datasets/70K/70K.shp");
                break;
            case "35k":
                file = new File("Datasets/cousub/cousub.shp");
                break;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("url", file.toURI().toURL());
        DataStore dataStore = DataStoreFinder.getDataStore(map);
        String typeName = dataStore.getTypeNames()[0];
        FeatureSource<SimpleFeatureType, SimpleFeature> source =
                dataStore.getFeatureSource(typeName);
        Filter filter = Filter.INCLUDE;
        dataStore.dispose();
        return source.getFeatures(filter);
    }

    private static ArrayList<S2CellUnion> initial_construct(FeatureCollection<SimpleFeatureType, SimpleFeature> collection, ArrayList<GeoArea> geoAreas, String dataset) {
        ArrayList<S2CellUnion> polygonCells = new ArrayList<>();
        int geo_index = 0;
        try (FeatureIterator<SimpleFeature> features = collection.features()) {
            while (features.hasNext()) {
                SimpleFeature feature = features.next();
                long extensive_attr;
                long internal_attr;

                extensive_attr = Long.parseLong((feature.getAttribute("ALAND").toString()));
                internal_attr = Long.parseLong(feature.getAttribute("AWATER").toString());


//                Geometry polygon = (Geometry) feature.getDefaultGeometry();
//                polygons.add(polygon);
//                Coordinate[] coor = polygon.getCoordinates();
//                GeoArea newArea = new GeoArea(geo_index , internal_attr , extensive_attr , coor);
//                geo_index ++;
//                geoAreas.add(newArea);

                Geometry polygon = (Geometry) feature.getDefaultGeometry();
                Coordinate[] coor = polygon.getCoordinates();

                List<S2Point> verticesLoop = new ArrayList<>();
                ArrayList<S2CellId> cellIds = new ArrayList<>();
                for (Coordinate coordinate : coor) {
                    S2Point point = S2LatLng.fromDegrees(coordinate.getY(), coordinate.getX()).toPoint();
                    S2Cell cell = new S2Cell(point);
                    cellIds.add(cell.id());
                }
                S2CellUnion cellUnion = new S2CellUnion();
                cellUnion.initFromCellIds(cellIds);
                polygonCells.add(cellUnion);


                GeoArea newArea = new GeoArea(geo_index, internal_attr, extensive_attr, verticesLoop);
                geo_index++;
                geoAreas.add(newArea);

            }
        }

        return polygonCells;

    }



    private static void setNeighborsCells(ArrayList<S2CellUnion> polygons, ArrayList<GeoArea> areas) {
//        long totalTime = 0;
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < polygons.size(); i++) {
            for (int j = i + 1; j < polygons.size(); j++) {
                if (polygons.get(i).intersects(polygons.get(j))) {
                    S2CellUnion intersection = new S2CellUnion();
                    intersection.getIntersection(polygons.get(i), polygons.get(j));

                    if (intersection.size() != 1) { // check if the intersection is a point
                    areas.get(i).add_neighbor(j);
                    areas.get(j).add_neighbor(i);
                    }
                }
            }
//            if ((i % 100 == 0) && (!polygons.isEmpty())) {
//                double result = (double) i / polygons.size();
//                long duration = (System.nanoTime() - startTime) / 100000000;
//                System.out.println(result * 100.00 + "%");
//                System.out.println("Time: " + duration + "ms");
//            }
        }
        long end_time = System.currentTimeMillis();
        System.out.println("Time: " + (end_time - startTime) + "ms");
        for (GeoArea area : areas) {
            if (area.get_neigh_area_index().size() == 0) {
                System.out.println("area has only one neighbor " + list_vals.get(area.get_geo_index()));
            }
        }
    }


}
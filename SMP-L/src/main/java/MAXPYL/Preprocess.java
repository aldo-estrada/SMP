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
        ArrayList<S2Polyline> polygons = initial_construct(collection, geoAreas, dataset);
        //System.out.println("construction finished");
        setNeighbors(polygons, geoAreas);
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

    private static ArrayList<S2Polyline> initial_construct(FeatureCollection<SimpleFeatureType, SimpleFeature> collection, ArrayList<GeoArea> geoAreas, String dataset) {
        ArrayList<S2Polyline> polylines = new ArrayList<>();
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
                for (Coordinate coordinate : coor) {
                    verticesLoop.add(S2LatLng.fromDegrees(coordinate.getY(), coordinate.getX()).toPoint()); // is the x lat or lng?
                }

                S2Polyline polylineS2 = new S2Polyline(verticesLoop); // testing
                polylines.add(polylineS2); // testing

                GeoArea newArea = new GeoArea(geo_index, internal_attr, extensive_attr, verticesLoop);
                geo_index++;
                geoAreas.add(newArea);


            }
        }

        return polylines;

    }

    private static void setNeighbors(ArrayList<S2Polyline> polylines, ArrayList<GeoArea> areas) {
//        for (int i = 0; i < polygons.size(); i++) {
//
//            for (int j = i + 1; j < polygons.size(); j++) {
//
//                if (polygons.get(i).intersects(polygons.get(j))) {
//
//                    Geometry intersection = polygons.get(i).intersection(polygons.get(j));
//
//                    if (intersection.getGeometryType() != "Point") {
//
//                        areas.get(i).add_neighbor(j);
//                        areas.get(j).add_neighbor(i);
//                        //System.out.println("neighbor added");
//
//                    } // end if
//                } // end if
//            } // end for
//        }
        long totalTime = 0;
        long startTime = System.nanoTime();
        for (int i = 0; i < polylines.size(); i++) {
            for (int j = i + 1; j < polylines.size(); j++) {
                if (polylines.get(i).intersects(polylines.get(j))) {
                    areas.get(i).add_neighbor(j);
                    areas.get(j).add_neighbor(i);
                }
            }

            long duration = (System.nanoTime() - startTime) / 1000000;
            totalTime = totalTime + duration;
            double percent = (double) i / polylines.size() * 100;
            System.out.println("Time: " + totalTime + "ms" + " Progress: " + percent + "%");

        }
        for (GeoArea area : areas) {
            if (area.get_neigh_area_index().size() == 0) {
                System.out.println("area has only one neighbor " + list_vals.get(area.get_geo_index()));
            }
        }
    }
}
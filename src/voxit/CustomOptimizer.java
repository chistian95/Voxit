package voxit;

import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static jme3tools.optimize.GeometryBatchFactory.mergeGeometries;

public class CustomOptimizer {    
    public static List<Geometry> makeBatches(Collection<Geometry> geometries) {
        ArrayList<Geometry> retVal = new ArrayList<>();
        HashMap<Material, List<Geometry>> matToGeom = new HashMap<>();

        for (Geometry geom : geometries) {
            List<Geometry> outList = matToGeom.get(geom.getMaterial());
            if (outList == null) {
                //trying to compare materials with the contentEquals method 
                for (Material mat : matToGeom.keySet()) {
                    if (geom.getMaterial().getName().equals(mat.getName())){
                        outList = matToGeom.get(mat);
                    }
                }
            }
            if (outList == null) {
                outList = new ArrayList<>();
                matToGeom.put(geom.getMaterial(), outList);
            }
            outList.add(geom);
        }

        int batchNum = 0;
        for (Map.Entry<Material, List<Geometry>> entry : matToGeom.entrySet()) {
            Material mat = entry.getKey();
            List<Geometry> geomsForMat = entry.getValue();
            Mesh mesh = new Mesh();
            mergeGeometries(geomsForMat, mesh);
            // lods
            mesh.updateCounts();
           
            Geometry out = new Geometry("batch[" + (batchNum++) + "]", mesh);
            out.setMaterial(mat);
            out.updateModelBound();
            retVal.add(out);
        }

        return retVal;
    }
}

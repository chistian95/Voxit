package voxit;

import com.jme3.scene.Mesh;
import java.util.Arrays;

public class MeshBuffer {
    public short[] indexes;
    public Mesh mesh;
    
    public MeshBuffer(short[] indexes, Mesh mesh) {
        this.indexes = indexes;
        this.mesh = mesh;
    }
    
    public boolean isIgual(short[] indexes) {
        return Arrays.equals(indexes, this.indexes);
    }
}

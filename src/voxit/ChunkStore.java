
package voxit;

import com.jme3.scene.Spatial;
import java.util.Objects;
import java.util.concurrent.Callable;

public class ChunkStore {
    public String idChunk;
    public Spatial spatBloques;
    private boolean generado;
    private boolean visible;
    
    public ChunkStore(int x, int y, int z, String idChunk, Voxit app) {
        this.generado = false;
        this.visible = false;
        this.idChunk = idChunk;        
        Chunk chunk = new Chunk(x,y,z, app);
        spatBloques = chunk.spatBloques;
        this.generado = true;
    }
    
    public boolean isVisible() {
        return visible;
    }
    public final void setVisible(boolean visible, Voxit vox) {
        if(!generado || visible == this.visible) {
            return;
        }
        
        this.visible = visible;
        final ChunkStore self = this;
        final Voxit app = vox;
        
        app.enqueue(new Callable<Spatial>() {
            @Override
            public Spatial call() throws Exception {  
                if(self.visible) {
                    app.getRootNode().attachChild(spatBloques);
                } else {
                    app.getRootNode().detachChild(spatBloques);
                }
                return spatBloques;
            } 
        });
    }
    
    public boolean isGenerado() {
        return generado;
    }
    
    public String getIdChunk() {
        return idChunk;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + Objects.hashCode(this.idChunk);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ChunkStore other = (ChunkStore) obj;
        if (!Objects.equals(this.idChunk, other.idChunk)) {
            return false;
        }
        return true;
    }    
}

package voxit;

import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

public class Chunk {
    public static final int ANCHO_CHUNK = 32;
    public static final float ESCALA_BLOQUES = 0.1f;
    
    private final int[] coords;
    private int[][][] infoChunk;
    private Geometry[][][] geomChunk;
    public Node spatBloques;
    private String idChunk;
    
    private final List<MeshBuffer> bufferMeshes = new ArrayList();
    
    Chunk(final int x, final int y, final int z, String idChunk, final Voxit app) {
        this.coords = new int[] {x,y,z};
        this.idChunk = idChunk;
        
        generarChunk(app);          
        spatBloques.setName(idChunk);
        spatBloques.setCullHint(Spatial.CullHint.Always);
        app.enqueue(new Callable<Spatial>() {
            @Override
            public Spatial call() throws Exception {  
                app.getRootNode().attachChild(spatBloques);
                return null;
            } 
        });
    }
    
    private void generarChunk(Voxit app) {        
        int x = coords[0];
        int y = coords[1];
        int z = coords[2];

        generarInfoChunk(app);
        generarGeomChunk(app.bloquesBase);
        optimizarChunk();
        spatBloques.setLocalTranslation(x*ANCHO_CHUNK*ESCALA_BLOQUES*2, y*ANCHO_CHUNK*ESCALA_BLOQUES*2, z*ANCHO_CHUNK*ESCALA_BLOQUES*2);        
    }
    
    private void generarInfoChunk(Voxit app) {
        int floor = 0;
        int ceiling = 20;
        double divisor = 60.0;
        int width = ANCHO_CHUNK;
        int alturaAgua = 5;
        
        int startX = coords[0] * width;
        int startY = coords[1] * width;
        int startZ = coords[2] * width;
        
        infoChunk = new int[width][width][width];
                
        for(int x=startX; x<startX+width; x++) {
            for(int z=startZ; z<startZ+width; z++) {
                double n = app.noise.eval(x / divisor, z / divisor);
                double y = (n + 1) * (ceiling - floor)  / (1 + 1) + floor;
                int xidx = (int) Math.floor(Math.abs((width + x % width) % width));
                int yidx = (int) Math.floor(Math.abs((width + y % width) % width));
                int zidx = (int) Math.floor(Math.abs((width + z % width) % width));

                if(y == floor || startY < y && y < startY + width) {
                    for(int ly = startY; ly < yidx; ly++) {
                        if(infoChunk[xidx][ly-startY][zidx] != 0) {
                            continue;
                        }
                        infoChunk[xidx][ly-startY][zidx] = 2;
                    }
                    if(infoChunk[xidx][yidx][zidx] == 0) {
                        infoChunk[xidx][yidx][zidx] = 3;
                    }
                    
                }

                for(int ly=startY; ly<=alturaAgua; ly++) {
                    if(ly > floor && infoChunk[x-startX][ly-startY][z-startZ] == 0) {
                        infoChunk[xidx][ly-startY][zidx] = 1;
                    }
                }
            }
        }
    }
    
    private void generarGeomChunk(Geometry[] bloquesBase) {
        float escala = ESCALA_BLOQUES;
        
        geomChunk = new Geometry[infoChunk.length][infoChunk[0].length][infoChunk[0][0].length];    
        
        for(int x=0,lenx=infoChunk.length; x<lenx; x++) {
            for(int y=0,leny=infoChunk[x].length; y<leny; y++) {
                for(int z=0,lenz=infoChunk[x][y].length; z<lenz; z++) {
                    int val = infoChunk[x][y][z];
                    
                    if(val == 0) {
                        continue;
                    }
                    
                    Geometry g = bloquesBase[val].clone();
                    g.move(x*escala*2, y*escala*2, z*escala*2);
                    geomChunk[x][y][z] = g;   
                }
            }
        }
    }
    
    private static final short[] GEOMETRY_INDICES_DATA = {
         2,  1,  0,  3,  2,  0, // back
         6,  5,  4,  7,  6,  4, // right
        10,  9,  8, 11, 10,  8, // front
        14, 13, 12, 15, 14, 12, // left
        18, 17, 16, 19, 18, 16, // top
        22, 21, 20, 23, 22, 20  // bottom
    };
    
    private void optimizarChunk() {
        List<Integer> solidos = new ArrayList<>();
        Set<Geometry> listaBloques = new HashSet<>();
        solidos.add(2);
        solidos.add(3);
        
        for(int x=0,lenx=infoChunk.length; x<lenx; x++) {
            for(int y=0,leny=infoChunk[x].length; y<leny; y++) {
                for(int z=0,lenz=infoChunk[x][y].length; z<lenz; z++) {
                    if(geomChunk[x][y][z] == null) {
                        continue;
                    }
                    Geometry g = geomChunk[x][y][z];
                    
                    int arriba,abajo,sur,norte,este,oeste;
                    
                    if(infoChunk[x][y][z] != 1) {
                        abajo = y-1>=0 && solidos.contains(infoChunk[x][y-1][z]) ? 0 : 1;
                        arriba = y+1<infoChunk[x].length && solidos.contains(infoChunk[x][y+1][z]) ? 0 : 1;
                        sur = x-1>=0 && solidos.contains(infoChunk[x-1][y][z]) ? 0 : 1;
                        norte = x+1<infoChunk.length && solidos.contains(infoChunk[x+1][y][z]) ? 0 : 1;
                        este = z+1<infoChunk[x][y].length && solidos.contains(infoChunk[x][y][z+1]) ? 0 : 1;
                        oeste = z-1>= 0 && solidos.contains(infoChunk[x][y][z-1]) ? 0 : 1;
                    } else {
                        abajo = y-1<0 || infoChunk[x][y-1][z] == 1 ? 0 : 1;
                        arriba = y+1>=infoChunk[x].length || infoChunk[x][y+1][z] == 1 ? 0 : 1;
                        sur = x-1<0 || infoChunk[x-1][y][z] == 1 ? 0 : 1;
                        norte = x+1>=infoChunk.length || infoChunk[x+1][y][z] == 1 ? 0 : 1;
                        este = z+1>=infoChunk[x][y].length || infoChunk[x][y][z+1] == 1 ? 0 : 1;
                        oeste = z-1< 0 || infoChunk[x][y][z-1] == 1 ? 0 : 1;
                    }
                    
                    if(abajo+arriba+norte+sur+este+oeste == 0) {
                        continue;
                    }
                    
                    short[] nuevoBuffer = GEOMETRY_INDICES_DATA.clone();
                                        
                    if(arriba == 0) {
                        int startI = 24;
                        for(int i=startI; i<startI+6; i++) {
                           nuevoBuffer[i] = -1;
                        }
                    }
                    if(abajo == 0) {                        
                        int startI = 30;
                        for(int i=startI; i<startI+6; i++) {
                           nuevoBuffer[i] = -1;
                        }
                    }
                    if(este == 0) {
                        int startI = 12;
                        for(int i=startI; i<startI+6; i++) {
                           nuevoBuffer[i] = -1;
                        }
                    }
                    if(oeste == 0) {
                        int startI = 0;
                        for(int i=startI; i<startI+6; i++) {
                           nuevoBuffer[i] = -1;
                        }
                    }
                    if(sur == 0) {
                        int startI = 18;
                        for(int i=startI; i<startI+6; i++) {
                           nuevoBuffer[i] = -1;
                        }
                    }
                    if(norte == 0) {
                        int startI = 6;
                        for(int i=startI; i<startI+6; i++) {
                           nuevoBuffer[i] = -1;
                        }
                    }
                    
                    int nuevoLength = 0;
                    for(short i : nuevoBuffer) {
                        if(i != -1) {
                            nuevoLength++;
                        }
                    }
                    short[] bufferFinal = new short[nuevoLength];
                    for(int i=0,j=0; i<nuevoBuffer.length; i++) {
                        if(nuevoBuffer[i] != -1) {
                            bufferFinal[j] = nuevoBuffer[i];
                            j++;
                        }
                    }
                    
                    Mesh meshBuffer = null;
                    for(MeshBuffer mb : bufferMeshes) {
                        if(mb.isIgual(bufferFinal)) {
                            meshBuffer = mb.mesh;
                            break;
                        }
                    }
                    
                    if(meshBuffer == null) {
                        Mesh m = g.getMesh().clone();                    
                        m.setBuffer(VertexBuffer.Type.Index, 3, BufferUtils.createShortBuffer(bufferFinal));
                        m.updateCounts();
                        m.updateBound();
                        m.setStatic();
                        bufferMeshes.add(new MeshBuffer(bufferFinal, m));
                        meshBuffer = m;
                    }
                    
                    g.setMesh(meshBuffer);   
                    
                    listaBloques.add(g);
                }
            }
        }
        
        spatBloques = new Node(idChunk);
        List<Geometry> listaBatch = CustomOptimizer.makeBatches(listaBloques);
        for(Geometry g : listaBatch) {
            spatBloques.attachChild(g);
        }
        spatBloques.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
    }
    
    public int[] getCoords() {
        return coords;
    }
}

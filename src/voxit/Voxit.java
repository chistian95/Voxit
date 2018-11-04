package voxit;

import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.scene.shape.Box;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.system.AppSettings;
import com.jme3.util.BufferUtils;
import java.util.ArrayList;
import java.util.List;
import jme3tools.optimize.GeometryBatchFactory;

public class Voxit extends SimpleApplication {
    public int[][][] mapa;
    public Geometry[][][] geom;
    public Spatial spatboxes;
    public Node boxes;
    
    private static final short[] GEOMETRY_INDICES_DATA = {
         2,  1,  0,  3,  2,  0, // back
         6,  5,  4,  7,  6,  4, // right
        10,  9,  8, 11, 10,  8, // front
        14, 13, 12, 15, 14, 12, // left
        18, 17, 16, 19, 18, 16, // top
        22, 21, 20, 23, 22, 20  // bottom
    };
    
    public static void main(String[] args) {
        Voxit app = new Voxit();
        
        AppSettings settings = new AppSettings(true);
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setTitle("Voxit");
        app.setShowSettings(false);
        app.setSettings(settings);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        flyCam.setMoveSpeed(10);
        
        AmbientLight al = new AmbientLight();
        al.setColor(new ColorRGBA(0.5f,0.5f,0.5f,1f));
        rootNode.addLight(al);
        
        DirectionalLight sol = new DirectionalLight();
        sol.setColor(ColorRGBA.White);
        //sol.setDirection(new Vector3f(-0.75f,-1,0.5f).normalizeLocal());
        sol.setDirection(new Vector3f(1f,-0.75f,1f).normalizeLocal());
        rootNode.addLight(sol);
        
        /*
        DirectionalLightShadowRenderer dlsr = new DirectionalLightShadowRenderer(assetManager, 10240, 4);
        dlsr.setLight(sol);
        viewPort.addProcessor(dlsr);
        */
        
        DirectionalLightShadowFilter dlsf = new DirectionalLightShadowFilter(assetManager, 10240, 4);
        dlsf.setLight(sol);
        dlsf.setEnabled(true);
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        fpp.addFilter(dlsf);
        viewPort.addProcessor(fpp);
        
        mapa = crearMapa();            
        pintarMapa();
    }
    
    private void pintarMapa() {
        float escala = 0.1f;
        boxes = new Node("boxes");
        Geometry[] bloques = new Geometry[4];
        Box b = new Box(escala, escala, escala);
        bloques[1] = new Geometry("bloqueAgua", b);
        bloques[1].setMaterial(assetManager.loadMaterial("Materials/Agua.j3m"));    
        bloques[1].setQueueBucket(Bucket.Transparent);
        bloques[1].setShadowMode(RenderQueue.ShadowMode.Off);
        bloques[2] = new Geometry("bloquePiedra", b);
        bloques[2].setMaterial(assetManager.loadMaterial("Materials/Piedra.j3m"));  
        bloques[3] = new Geometry("bloqueHierba", b);
        bloques[3].setMaterial(assetManager.loadMaterial("Materials/Hierba.j3m"));  
        
        geom = new Geometry[mapa.length][mapa[0].length][mapa[0][0].length];    
        
        for(int x=0,lenx=mapa.length; x<lenx; x++) {
            for(int y=0,leny=mapa[x].length; y<leny; y++) {
                for(int z=0,lenz=mapa[x][y].length; z<lenz; z++) {
                    int val = mapa[x][y][z];
                    
                    if(val == 0) {
                        continue;
                    }
                    
                    Geometry g = bloques[val].clone();
                    g.move(x*escala*2, y*escala*2, z*escala*2);
                    geom[x][y][z] = g;   
                    boxes.attachChild(g);
                }
            }
        }
        
        boxes.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        
        spatboxes = GeometryBatchFactory.optimize(boxes);
        rootNode.attachChild(spatboxes);
        updateMapa();
    }
    
    private void updateMapa() {
        List<Integer> solidos = new ArrayList<>();
        solidos.add(2);
        solidos.add(3);
        
        boxes.detachAllChildren();
        
        for(int x=0,lenx=mapa.length; x<lenx; x++) {
            for(int y=0,leny=mapa[x].length; y<leny; y++) {
                for(int z=0,lenz=mapa[x][y].length; z<lenz; z++) {
                    if(geom[x][y][z] == null) {
                        continue;
                    }
                    Geometry g = geom[x][y][z];
                    
                    int arriba,abajo,sur,norte,este,oeste;
                    
                    if(mapa[x][y][z] != 1) {
                        abajo = y-1>=0 && solidos.contains(mapa[x][y-1][z]) ? 0 : 1;
                        arriba = y+1<mapa[x].length && solidos.contains(mapa[x][y+1][z]) ? 0 : 1;
                        sur = x-1>=0 && solidos.contains(mapa[x-1][y][z]) ? 0 : 1;
                        norte = x+1<mapa.length && solidos.contains(mapa[x+1][y][z]) ? 0 : 1;
                        este = z+1<mapa[x][y].length && solidos.contains(mapa[x][y][z+1]) ? 0 : 1;
                        oeste = z-1>= 0 && solidos.contains(mapa[x][y][z-1]) ? 0 : 1;
                    } else {
                        abajo = y-1>=0 && mapa[x][y-1][z] == 1 ? 0 : 1;
                        arriba = y+1<mapa[x].length && mapa[x][y+1][z] == 1 ? 0 : 1;
                        sur = x-1>=0 && mapa[x-1][y][z] == 1 ? 0 : 1;
                        norte = x+1<mapa.length && mapa[x+1][y][z] == 1 ? 0 : 1;
                        este = z+1<mapa[x][y].length && mapa[x][y][z+1] == 1 ? 0 : 1;
                        oeste = z-1>= 0 && mapa[x][y][z-1] == 1 ? 0 : 1;
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
                    
                    Mesh m = g.getMesh().clone();                    
                    m.setBuffer(Type.Index, 3, BufferUtils.createShortBuffer(bufferFinal));
                    
                    g.setMesh(m);   
                    boxes.attachChild(g);
                }
            }
        }
        
        spatboxes = GeometryBatchFactory.optimize(boxes);
    }
    
    private int[][][] crearMapa() {
        OpenSimplexNoise noise = new OpenSimplexNoise(0);
        int floor = 0;
        int ceiling = 20;
        double divisor = 75.0;
        int[] position = new int[] {0,0,0};
        int width = 50;
        int alturaAgua = 3;

        int startX = position[0] * width;
        int startY = position[1] * width;
        int startZ = position[2] * width;
        
        mapa = new int[width][width][width];
                
        for(int x=startX; x<startX+width; x++) {
            for(int z=startZ; z<startZ+width; z++) {
                double n = noise.eval(x / divisor, z / divisor);
                double y = (n + 1) * (ceiling - floor)  / (1 + 1) + floor;
                int xidx = (int) Math.floor(Math.abs((width + x % width) % width));
                int yidx = (int) Math.floor(Math.abs((width + y % width) % width));
                int zidx = (int) Math.floor(Math.abs((width + z % width) % width));

                if(y == floor || startY < y && y < startY + width) {
                    for(int ly = floor; ly < yidx; ly++) {
                        if(mapa[xidx][ly][zidx] != 0) {
                            continue;
                        }
                        mapa[xidx][ly][zidx] = 2;
                    }
                    if(mapa[xidx][yidx][zidx] == 0) {
                        mapa[xidx][yidx][zidx] = 3;
                    }
                    
                }

                for(int ly=floor; ly<ceiling; ly++) {
                    if(ly > floor && ly <= alturaAgua && mapa[x][ly][z] == 0) {
                        mapa[xidx][ly][zidx] = 1;
                    }
                }
            }
        }
        
        return mapa;
    }

    @Override
    public void simpleUpdate(float tpf) {
    }

    @Override
    public void simpleRender(RenderManager rm) {
    }
}

package voxit;

import com.jme3.app.DetailedProfilerState;
import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.FogFilter;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.shadow.EdgeFilteringMode;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture;
import com.jme3.util.SkyFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Voxit extends SimpleApplication {    
    public static final int SEED = (int) (Math.random() * 1000);
    public static final int N_CHUNKS = 10;
    
    public Geometry[] bloquesBase;
    public OpenSimplexNoise noise;
    public Thread hiloChunks;
    public Set<ChunkStore> chunks = new HashSet();
    private int contUpdateChunk = 0;
    
    public static void main(String[] args) {
        Voxit app = new Voxit();
        
        AppSettings settings = new AppSettings(true);
        settings.setWidth(1280);
        settings.setHeight(720);
        settings.setTitle("Voxit");
        settings.setRenderer(AppSettings.LWJGL_OPENGL3);
        settings.setFrameRate(60);
        settings.setSamples(2);
        app.setShowSettings(false);
        app.setSettings(settings);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        stateManager.attach(new DetailedProfilerState());
        
        cam.setLocation(new Vector3f(1f, 2.75f, 1f));
        flyCam.setMoveSpeed(10);
        
        AmbientLight al = new AmbientLight();
        al.setColor(new ColorRGBA(0.5f,0.5f,0.5f,1f));
        rootNode.addLight(al);
        
        Vector3f dirSol = new Vector3f(0.5f,-0.75f,1f);
        DirectionalLight sol = new DirectionalLight();
        sol.setColor(ColorRGBA.White.clone().multLocal(1));
        sol.setDirection(dirSol.normalizeLocal());
        rootNode.addLight(sol);
                
        FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
        fpp.setNumSamples(1);
        
        DirectionalLightShadowFilter dlsf = new DirectionalLightShadowFilter(assetManager, 8000, 4);
        dlsf.setLight(sol);
        dlsf.setEdgeFilteringMode(EdgeFilteringMode.PCFPOISSON);
        dlsf.setEdgesThickness(-1);
        dlsf.setEnabled(true);       
        fpp.addFilter(dlsf);
        
        FogFilter fog = new FogFilter();
        fog.setFogColor(new ColorRGBA(0.8f, 0.8f, 0.8f, 1f));
        fog.setFogDistance(100);
        fog.setFogDensity(1f);
        fpp.addFilter(fog);
        
        viewPort.addProcessor(fpp);
        
        Texture lado, arriba, abajo;
        lado = assetManager.loadTexture("Textures/cielo_lado.png");
        arriba = assetManager.loadTexture("Textures/cielo_arriba.png");
        abajo = assetManager.loadTexture("Textures/cielo_abajo.png");
        rootNode.attachChild(SkyFactory.createSky(assetManager, lado, lado, lado ,lado, arriba, abajo));
        
        
        bloquesBase = new Geometry[4];
        float escala = 0.1f;
        Box b = new Box(escala, escala, escala);
        bloquesBase[1] = new Geometry("bloqueAgua", b);
        bloquesBase[1].setMaterial(assetManager.loadMaterial("Materials/Agua.j3m"));    
        bloquesBase[1].setQueueBucket(RenderQueue.Bucket.Transparent);
        bloquesBase[1].setShadowMode(RenderQueue.ShadowMode.Off);
        bloquesBase[2] = new Geometry("bloquePiedra", b);
        bloquesBase[2].setMaterial(assetManager.loadMaterial("Materials/Piedra.j3m"));  
        bloquesBase[3] = new Geometry("bloqueHierba", b);
        bloquesBase[3].setMaterial(assetManager.loadMaterial("Materials/Hierba.j3m")); 
        
        noise = new OpenSimplexNoise(SEED);      
        
        hiloChunks = new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    Vector3f posCam = cam.getLocation();
                    int x = (int) Math.floor(posCam.x / Chunk.ESCALA_BLOQUES / Chunk.ANCHO_CHUNK * 0.5);
                    int y = (int) Math.floor(posCam.y / Chunk.ESCALA_BLOQUES / Chunk.ANCHO_CHUNK * 0.5);
                    int z = (int) Math.floor(posCam.z / Chunk.ESCALA_BLOQUES / Chunk.ANCHO_CHUNK * 0.5);

                    cargarChunks(x,y,z);
                }    
            }
        });
        hiloChunks.start();
    }
    
    private void cargarChunks(int x, int y, int z) {
        y=0;
        
        for(int i=x-N_CHUNKS,leni=x+N_CHUNKS; i<=leni; i++) {
            for(int j=z-N_CHUNKS,lenj=z+N_CHUNKS; j<=lenj; j++) {
                String idChunk = "chunk;"+i+":"+y+":"+j;
                
                ChunkStore cs = buscarChunk(idChunk);                
                
                if(cs == null) {
                    cs = new ChunkStore(i,y,j, idChunk, this);
                    guardarChunk(cs);
                }
            }
        }        
    }
    
    private void ocultarChunks(int x, int y, int z) {
        synchronized(chunks) {
            Object[] chunkArray = (new ArrayList(chunks)).toArray();
            for(int i=0,len=chunkArray.length; i<len; i++) {
                ChunkStore cs = (ChunkStore) chunkArray[i];
                int absX = Math.abs(x - cs.coords[0]);
                int absZ = Math.abs(z - cs.coords[2]);

                if(absX > N_CHUNKS*2 || absZ > N_CHUNKS*2) {
                    chunks.remove(cs);
                } else if(absX > N_CHUNKS || absZ > N_CHUNKS) {
                    cs.setVisible(false, this);
                } else {
                    cs.setVisible(true, this);
                }
            }
        }        
    }
    
    private void guardarChunk(ChunkStore cs) {
        synchronized(chunks) {
            chunks.add(cs);
        }
    }
    
    private ChunkStore buscarChunk(String idChunk) {
        synchronized(chunks) {
            Object[] chunkArray = (new ArrayList(chunks)).toArray();
            for(int i=0,len=chunkArray.length; i<len; i++) {
                ChunkStore cs = (ChunkStore) chunkArray[i];
                if(cs.idChunk.equals(idChunk)) {
                    return cs;
                }
            }
            return null;
        }        
    }

    @Override
    public void simpleUpdate(float tpf) {
        contUpdateChunk++;
        if(contUpdateChunk >= 10) {
            contUpdateChunk = 0;
            
            Vector3f posCam = cam.getLocation();
            int x = (int) Math.floor(posCam.x / Chunk.ESCALA_BLOQUES / Chunk.ANCHO_CHUNK * 0.5);
            int y = (int) Math.floor(posCam.y / Chunk.ESCALA_BLOQUES / Chunk.ANCHO_CHUNK * 0.5);
            int z = (int) Math.floor(posCam.z / Chunk.ESCALA_BLOQUES / Chunk.ANCHO_CHUNK * 0.5);
            ocultarChunks(x,y,z);
        }
    }

    @Override
    public void simpleRender(RenderManager rm) {
    }

    @Override
    public void stop() {
        hiloChunks.stop();
        super.stop(); 
    }
    
    
}

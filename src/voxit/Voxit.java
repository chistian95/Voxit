package voxit;

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

public class Voxit extends SimpleApplication {    
    public static final int SEED = (int) (Math.random() * 1000);
    
    public Geometry[] bloquesBase;
    public OpenSimplexNoise noise;
    public boolean chunksHechos = false;
    
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
    }
    
    public void generarChunks() {
        final Voxit app = this;
        (new Thread() {
            @Override
            public void run() {
                int y = 0;
                for(int x=0; x<20; x++) {
                    for(int z=0; z<20; z++) {
                        new Chunk(x,y,z, app);
                    }
                } 
            }
        }).start();        
    }

    @Override
    public void simpleUpdate(float tpf) {
        if(!chunksHechos) {
            chunksHechos = true;
            generarChunks();
        }
    }

    @Override
    public void simpleRender(RenderManager rm) {
    }
}

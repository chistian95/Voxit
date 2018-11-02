var canvas = document.getElementById("renderCanvas");
var engine = new BABYLON.Engine(canvas, true);

var crearEscena = function() {
  var scene = new BABYLON.Scene(engine);
  scene.socket = io();

  var camara = new BABYLON.ArcRotateCamera("Camara", 3* Math.PI / 4, Math.PI / 4, 4, new BABYLON.Vector3(25, 50, 25), scene);
  camara.attachControl(canvas, true);

  var luz1 = new BABYLON.HemisphericLight("luz1", new BABYLON.Vector3(0,0.5,0), scene);

  var matAgua = new BABYLON.StandardMaterial("agua", scene);
  matAgua.specularColor = new BABYLON.Color3(0.1, 0.1, 0.1);
  matAgua.diffuseColor = new BABYLON.Color3(0.1, 0.45, 0.82);
  matAgua.freeze();

  var matGrass = new BABYLON.StandardMaterial("grass", scene);
  matGrass.specularColor = new BABYLON.Color3(0.1, 0.1, 0.1);
  matGrass.diffuseColor = new BABYLON.Color3(0.3, 0.7, 0.3);
  matGrass.freeze();

  var matPiedra = new BABYLON.StandardMaterial("piedra", scene);
  matPiedra.specularColor = new BABYLON.Color3(0.1, 0.1, 0.1);
  matPiedra.diffuseColor = new BABYLON.Color3(0.6, 0.6, 0.6);
  matPiedra.freeze();

  var materiales = {
    agua: matAgua,
    grass: matGrass,
    piedra: matPiedra,
  }

  var meshes = {};
  var mat_arr = ["aire", "grass", "piedra", "agua"];

  scene.socket.on('cargarMapa', function(mapa) {
    for(var x=0,lenX=mapa.length; x<lenX; x++) {
      for(var z=0,lenZ=mapa[x].length; z<lenZ; z++) {
        for(var y=0,lenY=mapa[x][z].length; y<lenY; y++) {
          var val = mapa[x][z][y];
          if(val > 0) {
            var material = mat_arr[val];

            var cubo;
            if(!meshes[material]) {
              cubo = BABYLON.MeshBuilder.CreateBox("c:"+x+";"+y+";"+z, scene);
              cubo.convertToUnIndexedMesh();
              cubo.material = materiales[material];

              meshes[material] = cubo;
            } else {
              cubo = meshes[material].createInstance("c:"+x+";"+y+";"+z);
            }

            cubo.position = new BABYLON.Vector3(x, y, z);
          }
        }
      }
    }
  });

  return scene;
}

var scene = crearEscena();

engine.runRenderLoop(function() {
  scene.render();
});

window.addEventListener("resize", function() {
  engine.resize();
});

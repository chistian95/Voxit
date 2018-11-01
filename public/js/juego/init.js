var canvas = document.getElementById("renderCanvas");
var engine = new BABYLON.Engine(canvas, true);

var crearEscena = function() {
  var scene = new BABYLON.Scene(engine);
  scene.socket = io();

  var camara = new BABYLON.ArcRotateCamera("Camara", 3* Math.PI / 4, Math.PI / 4, 4, new BABYLON.Vector3.Zero(), scene);
  camara.attachControl(canvas, true);


  var luz1 = new BABYLON.HemisphericLight("luz1", new BABYLON.Vector3(0,1,0), scene);
  var matBase = new BABYLON.StandardMaterial("basico", scene);
  matBase.specularColor = new BABYLON.Color3(0.1, 0.1, 0.1);
  matBase.diffuseColor = new BABYLON.Color3(0.3, 0.7, 0.3);

  for(var x=-5; x<5; x++) {
    for(var z=-5; z<5; z++) {
      var cubo = BABYLON.MeshBuilder.CreateBox("cubo", scene);
      cubo.position = new BABYLON.Vector3(x,0,z);
      cubo.material = matBase;
    }
  }

  return scene;
}

var scene = crearEscena();

engine.runRenderLoop(function() {
  scene.render();
});

window.addEventListener("resize", function() {
  engine.resize();
});

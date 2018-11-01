var canvas = document.getElementById("renderCanvas");
var engine = new BABYLON.Engine(canvas, true);

var crearEscena = function() {
  var scene = new BABYLON.Scene(engine);
  scene.socket = io();

  var camara = new BABYLON.ArcRotateCamera("Camara", 3* Math.PI / 4, Math.PI / 4, 4, new BABYLON.Vector3.Zero(), scene);
  camara.attachControl(canvas, true);


  var luz1 = new BABYLON.HemisphericLight("luz1", new BABYLON.Vector3(1,1,0), scene);
  luz1.position = new BABYLON.Vector3(0,3,0);

  for(var x=-5; x<5; x++) {
    for(var z=-5; z<5; z++) {
      var colores = [];
      var red = Math.random();
      var green = Math.random();
      var blue = Math.random();
      for(var i=0; i<6; i++) {
        colores[i] = new BABYLON.Color4(red, green, blue, 0.1);
      }

      var cubo = BABYLON.MeshBuilder.CreateBox("cubo", {faceColors: colores}, scene);
      cubo.position = new BABYLON.Vector3(x,0,z);
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

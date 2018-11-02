var express = require('express');
var app = express();
var server = require('http').Server(app);
var io = require('socket.io').listen(server);

app.use(express.static(__dirname + '/public'));

app.get('/', function(req, res) {
  res.sendFile(__dirname + '/index.hmtl');
});

io.sockets.on('connection', function(socket) {
  console.log(`Usuario conectado (${socket.id})`);

  socket.emit('cargarMapa', mapa);

  socket.on('disconnect', function() {
    console.log(`Usuario desconectado (${socket.id})`)
  })
});

server.listen(8081, function() {
  console.log(`Escuchando en ${server.address().port}`);
});

var noise = require('perlin').noise;
noise.seed('voxit');
var floor = 0;
var ceiling = 20;
var divisor = 75;
var position = [0, 0, 0];
var width = 50;
var alturaAgua = 3;

var startX = position[0] * width;
var startY = position[1] * width;
var startZ = position[2] * width;
var mapa = [];

for(var x=startX; x<startX+width; x++) {
  mapa[x] = [];
  for(var z=startZ; z<startZ+width; z++) {
    mapa[x][z] = [];
    for(var y=floor; y<ceiling; y++) {
      mapa[x][z][y] = 0;
    }

    var n = noise.simplex2(x / divisor , z / divisor);
    var y = (n + 1) * (ceiling - floor) / (1 + 1) + floor;
    var xidx = Math.round(Math.abs((width + x % width) % width));
    var yidx = Math.round(Math.abs((width + y % width) % width));
    var zidx = Math.round(Math.abs((width + z % width) % width));

    if(y === floor || startY < y && y < startY + width) {
      mapa[xidx][zidx][yidx] = 1;
    }

    for(var y=floor; y<ceiling; y++) {
      if(y > floor && y <= alturaAgua && mapa[x][z][y] === 0)
      mapa[x][z][y] = 2;
    }
  }
}

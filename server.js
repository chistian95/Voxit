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

  socket.on('disconnect', function() {
    console.log(`Usuario desconectado (${socket.id})`)
  })
});

server.listen(8081, function() {
  console.log(`Escuchando en ${server.address().port}`);
});

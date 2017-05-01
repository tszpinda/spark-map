var express = require('express')
var app = express()
var server = require('http').createServer(app);
var io = require('socket.io')(server);

app.use(express.static(__dirname + '/node_modules'))
app.set('view engine', 'ejs');

const sockets = [];

io.on('connection', function (socket) {
  sockets.push(socket);
});

const collection = {"type":"FeatureCollection", features: []}
/*
collection.features.push({
  type: "Feature",
  geometry: {
    "type": "Polygon",
    "coordinates": [
      [
        [0, 0], [20, 10], [10, 0], [0, 0]
      ]
    ]
  }
});
*/

function point(lat, lng) {
  lat = parseInt(lat);
  lng = parseInt(lng);
  return {
      type: "Point",
      coordinates: [lng, lat]
  };
}

app.get('/new', function(req, res){
  var q = req.query;
  if(q.latitude && q.longitude){
    const p = point(q.latitude, q.longitude);
    console.log(p);
    const f = {
      type: "Feature",
      geometry: p
    };
    collection.features.push(f);
    sockets.forEach(s => s.broadcast.emit('data-added', f));
    return res.json({status: 'ok'});
  }
  res.json({status: 'error'});
});

app.get('/geojson', function (req, res) {
  res.json(collection);
});

app.get('/', function (req, res) {
  res.render('index.ejs')
})


server.listen(3000, function () {
  console.log('http://localhost:3000')
})

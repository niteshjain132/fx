var express = require('express'),
  app = express(),
  http = require('http').Server(app),
  io = require('socket.io')(http),
  fetch = require('node-fetch');

app.use(express.static(__dirname + '/'));

var fields = ["currencyPair", "timestamp", "bidBig", "bidPips", "offerBig", "offerPips", "high", "low", "open"];
var cachedData = [];
var midRates = {};
var dayStats = {};
var connected = 0;

// TrueFX-style majors. open.er-api returns USD-based rates.
var pairs = [
  { symbol: "EUR/USD", invert: true, code: "EUR", decimals: 5 },
  { symbol: "USD/JPY", invert: false, code: "JPY", decimals: 3 },
  { symbol: "GBP/USD", invert: true, code: "GBP", decimals: 5 },
  { symbol: "USD/CHF", invert: false, code: "CHF", decimals: 5 },
  { symbol: "AUD/USD", invert: true, code: "AUD", decimals: 5 },
  { symbol: "USD/CAD", invert: false, code: "CAD", decimals: 5 },
  { symbol: "NZD/USD", invert: true, code: "NZD", decimals: 5 }
];

function splitPrice(price, decimals) {
  var fixed = Number(price).toFixed(decimals);
  var pipDigits = decimals <= 3 ? 1 : 2;
  var big = fixed.slice(0, fixed.length - pipDigits);
  var pips = fixed.slice(-pipDigits);
  // Drop trailing decimal point from big if pips took all fractional digits
  if (big.charAt(big.length - 1) === '.') {
    big = big.slice(0, -1);
  }
  return { big: big, pips: pips };
}

function buildRow(pair, mid, timestamp) {
  var spread = Math.pow(10, -pair.decimals) * (pair.decimals <= 3 ? 2 : 5);
  var jitter = (Math.random() - 0.5) * spread * 2;
  var bid = mid + jitter - spread / 2;
  var offer = mid + jitter + spread / 2;

  if (!dayStats[pair.symbol]) {
    dayStats[pair.symbol] = { high: offer, low: bid, open: mid };
  } else {
    dayStats[pair.symbol].high = Math.max(dayStats[pair.symbol].high, offer);
    dayStats[pair.symbol].low = Math.min(dayStats[pair.symbol].low, bid);
  }

  var bidParts = splitPrice(bid, pair.decimals);
  var offerParts = splitPrice(offer, pair.decimals);
  var stats = dayStats[pair.symbol];

  return {
    currencyPair: pair.symbol,
    timestamp: String(timestamp),
    bidBig: bidParts.big,
    bidPips: bidParts.pips,
    offerBig: offerParts.big,
    offerPips: offerParts.pips,
    high: stats.high.toFixed(pair.decimals),
    low: stats.low.toFixed(pair.decimals),
    open: stats.open.toFixed(pair.decimals)
  };
}

function emitTicks() {
  if (!Object.keys(midRates).length) return;
  var timestamp = Date.now();
  cachedData = pairs.map(function (pair) {
    return buildRow(pair, midRates[pair.symbol], timestamp);
  });
  io.sockets.emit('data', cachedData);
}

function refreshMids() {
  return fetch('https://open.er-api.com/v6/latest/USD')
    .then(function (response) {
      if (!response.ok) throw new Error('FX API HTTP ' + response.status);
      return response.json();
    })
    .then(function (payload) {
      if (!payload || !payload.rates) throw new Error('FX API missing rates');
      pairs.forEach(function (pair) {
        var usdRate = payload.rates[pair.code];
        if (!usdRate) return;
        midRates[pair.symbol] = pair.invert ? (1 / usdRate) : usdRate;
      });
      emitTicks();
    })
    .catch(function (err) {
      console.error('Failed to refresh FX mids:', err.message || err);
      // Keep emitting ticks from last known mids so the UI stays live.
      emitTicks();
    });
}

io.on('connection', function (socket) {
  connected++;
  if (cachedData.length) {
    socket.emit('data', cachedData);
  }
  socket.on('disconnect', function () {
    connected--;
  });
});

refreshMids();
setInterval(refreshMids, 60 * 1000);
setInterval(emitTicks, 2000);

http.listen(3000, function () {
  console.log('listening on: 3000');
});

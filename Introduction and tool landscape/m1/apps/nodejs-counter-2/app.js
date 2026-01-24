const express = require('express');
const { createClient } = require('redis');

const app  = express();
const PORT = 5000;

const redis = createClient();

redis.on('error', err => console.error('Redis error:', err));

(async () => {
  await redis.connect(); 
})();

app.get('/', async (req, res) => {
  const count = await redis.incr('nodejs-hits');

  const msg = `Hello! This Node app has been viewed ${count} times.\n`;
  res.type('text/plain').send(msg);
});

app.listen(PORT, '0.0.0.0', () => {
  console.log(`Server listening on http://0.0.0.0:${PORT}`);
});
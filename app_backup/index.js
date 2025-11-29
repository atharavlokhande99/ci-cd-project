// app/index.js
const express = require('express');
const app = express();

app.get('/', (req, res) => res.send('Hello from CI/CD demo! v' + (process.env.APP_VERSION || '0.0.1')));

if (require.main === module) {
  const port = process.env.PORT || 3000;
  app.listen(port, () => console.log('App listening on', port));
}

module.exports = app;


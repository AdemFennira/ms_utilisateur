// Fix index.js: parse args and run newman
const newman = require('newman');
const argv = require('minimist')(process.argv.slice(2));
const fs = require('fs');

const collectionPath = argv.collection || './collection.json';
const envPath = argv.env || './env.json';
const dataPath = argv.data || './dataset.json';

if (!fs.existsSync(collectionPath)) {
  console.error('Collection not found:', collectionPath);
  process.exit(1);
}

const opts = {
  collection: require(collectionPath),
  reporters: 'cli'
};

if (fs.existsSync(envPath)) {
  opts.environment = require(envPath);
}

if (fs.existsSync(dataPath)) {
  opts.iterationData = dataPath;
}

console.log('Running newman with', { collection: collectionPath, env: envPath, data: dataPath });

newman.run(opts, function (err, summary) {
  if (err) {
    console.error('Newman run failed:', err);
    process.exit(2);
  }
  const failures = summary.run && summary.run.failures && summary.run.failures.length;
  if (failures) {
    console.error('Failures:', failures);
    process.exit(3);
  }
  console.log('Newman run completed successfully');
  process.exit(0);
});

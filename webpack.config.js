const path = require('path');

module.exports = {
  entry: './src/main/resources/static/js/editor.js',
  output: {
    filename: 'bundle.js',
    path: path.resolve(__dirname, 'src/main/resources/static/js'),
  },
  mode: 'development',
  module: {
    rules: [
      {
        test: /\.m?js$/,
        resolve: {
          fullySpecified: false
        },
        use: 'babel-loader'
      }
    ]
  }
};
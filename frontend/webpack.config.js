let ExtractTextPlugin = require ('extract-text-webpack-plugin');


module.exports = {

    entry: './src/index.jsx',

    output: {
        path: __dirname + '/dist',
        publicPath: '/',
        filename: 'bundle.js'
    },
    devServer: {
      contentBase: './dist',
      port: 9000,
      proxy: [{
          '/api': {
              target: 'http://localhost:8080',
          }
      },
      {
        '/api/tail': {
            target: 'ws://localhost:8080',
            ws: true
        }
    }],
      historyApiFallback: true
    },

    module: {
        rules: [
            {
                test: /\.(js|jsx)$/,
                exclude: /node_modules/,
                loader: 'babel-loader'
            },
            {
                test: /\.css$/,
                use: [
                    'style-loader',
                    'css-loader'
                ]
            },
            {
                test: /\.(png|jpg|gif)$/,
                use: {
                    loader: 'url-loader'
                }
            },
            {
                test: /\.(woff(2)?|ttf|eot|svg)$/,
                use: {
                    loader: 'file-loader',
                    options: {
                        name: '[name].[ext]',
                        outputPath: 'fonts/'
                    }
                }
            }
        ]
    },
    resolve: {
        extensions: ['.js', '.jsx']
    }
};

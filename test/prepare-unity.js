const copyDirectory = require('./copy-directory');

copyDirectory('./unity/Assets/FlcTcpSocket', '../dist/unity/FlcTcpSocket', [
  'FlcTcpSocket.jar',
  'FlcTcpClient.cs',
  'FlcTcpClientAndroid.cs',
  'FlcTcpClientDefault.cs'
]);

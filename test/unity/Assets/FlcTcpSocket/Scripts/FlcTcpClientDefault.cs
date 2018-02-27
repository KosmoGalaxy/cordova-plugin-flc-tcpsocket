using System;
using System.Net.Sockets;
using System.Threading.Tasks;
using UnityEngine;

namespace FullLegitCode.TcpSocket
{
    public class FlcTcpClientDefault : FlcTcpClient
    {
        TcpClient _client;

        public FlcTcpClientDefault()
        {
            _client = new TcpClient
            {
                LingerState = new LingerOption(true, 0),
                NoDelay = true,
                SendTimeout = 5000
            };
        }

        public override async Task Open(string ip, int port)
        {
            try
            {
                await _client.ConnectAsync(ip, port);
                _isOpen = true;
                _ip = ip;
                _port = port;
                Debug.Log(string.Format("[FlcTcpClient] open. address={0}:{1}", ip, port));
            }
            catch (Exception e)
            {
                Debug.LogError(string.Format("[FlcTcpClient] open error. address={0}:{1} message={2}", ip, port, e.Message));
                throw e;
            }
        }

        public override async Task Send(byte[] data)
        {
            try
            {
                await _client.GetStream().WriteAsync(data, 0, data.Length);
            }
            catch (Exception e)
            {
                Debug.LogError(string.Format("[FlcTcpClient] send error. address={0}:{1} size={2} message={3}", ip, port, data.Length, e.Message));
                _TryClose();
                throw e;
            }
        }

        public override void Close()
        {
            if (!isClosed)
            {
                try
                {
                    _isClosed = true;
                    _client.Close();
                    Debug.Log("[FlcTcpClient] closed");
                }
                catch (Exception e)
                {
                    Debug.LogWarning(string.Format("[FlcTcpClient] close error. message={0}", e.Message));
                }
            }
        }

        void _TryClose()
        {
            try
            {
                Close();
            }
            catch (Exception e)
            {
                Debug.LogWarning(string.Format("[FlcTcpClient] close error. message={0}", e.Message));
            }
        }
    }
}

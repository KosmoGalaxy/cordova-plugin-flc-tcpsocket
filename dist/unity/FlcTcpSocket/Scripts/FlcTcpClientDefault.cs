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
                SendTimeout = 5000
            };
        }

        public override async Task Open(string ip, int port)
        {
            await _client.ConnectAsync(ip, port);
            _isOpen = _client.Connected;
        }

        public override async Task Send(byte[] data)
        {
            try
            {
                await _client.GetStream().WriteAsync(data, 0, data.Length);
            }
            catch (Exception e)
            {
                _TryClose();
                throw e;
            }
        }

        public override void Close()
        {
            if (!isClosed)
            {
                _isClosed = true;
                _client.Close();
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
                Debug.LogWarning("FlcTcpClient close error. message=" + e.Message);
            }
        }
    }
}

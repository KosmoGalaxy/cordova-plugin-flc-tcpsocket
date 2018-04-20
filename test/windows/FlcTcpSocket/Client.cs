using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Runtime.InteropServices.WindowsRuntime;
using System.Threading.Tasks;
using Windows.Foundation;

namespace FullLegitCode.TcpSocket
{
    public interface IClient
    {
        int Id { get; }
        string Ip { get; }
        event EventHandler<string> Closed;
    }


    class Client : IClient
    {
        static int _nextId = 1;


        public int Id { get; } = _nextId++;
        public string Ip { get; }
        public event EventHandler<string> Closed;

        public bool IsClosed { get; private set; }
        TcpClient TcpClient { get; }

        internal Client(TcpClient client)
        {
            TcpClient = client;
            IPEndPoint ipEndPoint = (IPEndPoint)TcpClient.Client.RemoteEndPoint;
            Ip = ipEndPoint.Address.ToString();
        }

        public void Close(string reason)
        {
            if (!IsClosed)
            {
                IsClosed = true;
                try
                {
                    TcpClient.Dispose();
                }
                catch (Exception e) { e.GetBaseException(); }
                Socket.Log(string.Format("client closed (id)={0} (ip)={1} (reason)={2}", Id, Ip, reason));
                Closed?.Invoke(this, reason);
            }
        }

        public IAsyncActionWithProgress<IClientReceivePayload> Listen()
        {
            byte[] buffer = new byte[1024];
            int nextOrderNo = 0;
            return AsyncInfo.Run<IClientReceivePayload>((token, progress) =>
            {
                return Task.Run(() =>
                {
                    try
                    {
                        while (!IsClosed)
                        {
                            int numBytes = TcpClient.GetStream().Read(buffer, 0, buffer.Length);
                            if (numBytes == 0)
                            {
                                break;
                            }
                            ClientReceivePayload payload = new ClientReceivePayload
                            {
                                Data = new List<byte>(buffer.Take(numBytes)),
                                OrderNo = nextOrderNo++
                            };
                            progress.Report(payload);
                        }
                    }
                    catch (Exception e)
                    {
                        Socket.Log(string.Format("client receive error (id)={0} (ip)={1} (message)={2}", Id, Ip, e.Message));
                        Close(e.Message);
                        return;
                    }
                    Close("disconnected");
                }, token);
            });
        }
    }
}

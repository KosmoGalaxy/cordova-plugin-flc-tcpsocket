using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Threading.Tasks;

namespace FullLegitCode.TcpSocket
{
    public interface IServer
    {
        int Id { get; }
        event EventHandler<Object> ClientOpen;
        event EventHandler<string> Closed;
    }


    class Server : IServer
    {
        static int _nextId = 1;


        public int Id { get; } = _nextId++;
        public event EventHandler<Object> ClientOpen;
        public event EventHandler<string> Closed;

        public bool IsClosed { get; private set; }
        TcpListener Listener { get; }
        Task ListeningTask { get; }
        Dictionary<int, Client> Clients { get; } = new Dictionary<int, Client>();

        internal Server(int port)
        {
            Listener = new TcpListener(new IPEndPoint(IPAddress.Any, port));
            ListeningTask = _Start();
        }

        public void Close(string reason)
        {
            if (!IsClosed)
            {
                IsClosed = true;
                _CloseClients(reason);
                try
                {
                    Listener.Stop();
                }
                catch (Exception e) { e.GetBaseException(); }
                Socket.Log(string.Format("server closed (id)={0}", Id));
                Closed?.Invoke(this, reason);
            }
        }

        public Client GetClient(int id)
        {
            return HasClient(id) ? Clients[id] : null;
        }

        public bool HasClient(int id)
        {
            return Clients.ContainsKey(id);
        }

        Task _Start()
        {
            return Task.Run(async () =>
            {
                Listener.Start();
                while (!IsClosed)
                {
                    TcpClient client = await Listener.AcceptTcpClientAsync();
                    _AddClient(client);
                }
            });
        }

        void _AddClient(TcpClient tcpClient)
        {
            Client client = new Client(tcpClient);
            Socket.Log(string.Format("client open (server id)={0} (id)={1} (ip)={2}", Id, client.Id, client.Ip));
            Clients.Add(client.Id, client);
            ClientOpen?.Invoke(this, client);
            client.Closed += (c, r) => Clients.Remove(client.Id);
        }

        void _CloseClients(string reason)
        {
            KeyValuePair<int, Client>[] clients = Clients.ToArray();
            foreach (KeyValuePair<int, Client> kvp in clients)
            {
                kvp.Value.Close(reason);
            }
        }
    }
}

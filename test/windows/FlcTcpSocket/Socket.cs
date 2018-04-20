using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Threading.Tasks;
using Windows.Foundation;

namespace FullLegitCode.TcpSocket
{
    public sealed class Socket
    {
        const string TAG = "[FlcTcpSocket] ";


        static Dictionary<int, Server> Servers { get; } = new Dictionary<int, Server>();

        public static IAsyncOperation<Object> OpenServer(int port)
        {
            return Task.Run(() =>
            {
                Server server = new Server(port);
                Log(string.Format("server open (id)={0} (port)={1}", server.Id, port));
                Servers.Add(server.Id, server);
                return (Object)server;
            })
            .AsAsyncOperation();
        }

        public static IAsyncActionWithProgress<IClientReceivePayload> ClientListen(int id)
        {
            Client client = _GetClient(id);
            if (client == null)
            {
                throw new Exception(string.Format("client (id)={0} not found", id));
            }
            return client.Listen();
        }

        static Client _GetClient(int id)
        {
            foreach (KeyValuePair<int, Server> kvp in Servers)
            {
                Client client = kvp.Value.GetClient(id);
                if (client != null)
                {
                    return client;
                }
            }
            return null;
        }

        internal static void Log(string message)
        {
            Debug.WriteLine(TAG + message);
        }
    }
}

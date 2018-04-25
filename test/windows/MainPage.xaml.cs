using FullLegitCode.TcpSocket;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices.WindowsRuntime;
using System.Text;
using Windows.Foundation;
using Windows.Foundation.Collections;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Controls.Primitives;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Navigation;

// The Blank Page item template is documented at https://go.microsoft.com/fwlink/?LinkId=402352&clcid=0x409

namespace FlcTcpSocketTest
{
    /// <summary>
    /// An empty page that can be used on its own or navigated to within a Frame.
    /// </summary>
    public sealed partial class MainPage : Page
    {
        public MainPage()
        {
            this.InitializeComponent();
            //Test();
            ConnectClient();
        }

        async void Test()
        {
            IServer server = (IServer)await Socket.OpenServer(3070);
            Debug.WriteLine("[FlcTcpSocketTest] server open (id)=" + server.Id);
            server.ClientOpen += (s, c) =>
            {
                IClient client = (IClient)c;
                Debug.WriteLine(string.Format("[FlcTcpSocketTest] client open (id)={0} (ip)={1}", client.Id, client.Ip));
                client.Closed += (c1, reason) =>
                {
                    Debug.WriteLine(string.Format("[FlcTcpSocketTest] client closed (id)={0} (ip)={1} (reason)={2}", client.Id, client.Ip, reason));
                };
                Socket.ClientListen(client.Id).Progress = (info, status) =>
                {
                    Debug.WriteLine(string.Format(
                        "[FlcTcpSocketTest] client receive (id)={0} (ip)={1} (data)={2}",
                        client.Id,
                        client.Ip,
                        Encoding.UTF8.GetString(status.Data.ToArray())
                    ));
                };
            };
            ConnectClient();
        }

        async void ConnectClient()
        {
            System.Net.Sockets.TcpClient client = new System.Net.Sockets.TcpClient();
            await client.ConnectAsync("192.168.1.142", 3070);
            Debug.WriteLine("connected");
        }
    }
}

using System.Threading.Tasks;

namespace FullLegitCode.TcpSocket
{
    public abstract class FlcTcpClient
    {
        public static FlcTcpClient Create()
        {
#if UNITY_ANDROID && !UNITY_EDITOR
            return new FlcTcpClientAndroid();
#else
            return new FlcTcpClientDefault();
#endif
        }


        protected string _ip;
        public string ip { get { return _ip; } }

        protected int _port;
        public int port { get { return _port; } }

        protected bool _isOpen = false;
        public bool isOpen { get { return _isOpen; } }

        protected bool _isClosed = false;
        public bool isClosed { get { return _isClosed; } }

        public abstract Task Open(string ip, int port);
        public abstract Task Send(byte[] data);
        public abstract void Close();
    }
}

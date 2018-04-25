using System;

namespace FullLegitCode.TcpSocket
{
    public interface IClientReceivePayload
    {
        Byte[] Data { get; }
        int OrderNo { get; }
    }


    class ClientReceivePayload : IClientReceivePayload
    {
        public Byte[] Data { get; set; }
        public int OrderNo { get; set; }
    }
}

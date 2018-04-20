using System.Collections.Generic;

namespace FullLegitCode.TcpSocket
{
    public interface IClientReceivePayload
    {
        IList<byte> Data { get; }
        int OrderNo { get; }
    }


    class ClientReceivePayload : IClientReceivePayload
    {
        public IList<byte> Data { get; set; }
        public int OrderNo { get; set; }
    }
}

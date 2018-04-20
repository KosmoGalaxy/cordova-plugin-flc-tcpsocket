namespace FullLegitCode.TcpSocket
{
    public interface IClientOpenEventArgs
    {
        int Id { get; set; }
        string Ip { get; set; }
    }


    public sealed class ClientOpenEventArgs : IClientOpenEventArgs
    {
        public int Id { get; set; }
        public string Ip { get; set; }
    }
}

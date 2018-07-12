using System;
using System.Threading;
using System.Threading.Tasks;
using UnityEngine;

namespace FullLegitCode.TcpSocket
{
    public class FlcTcpClientAndroid : FlcTcpClient
    {
        AndroidJavaObject _javaObject;

        public FlcTcpClientAndroid()
        {
            _javaObject = new AndroidJavaObject("pl.fulllegitcode.tcpsocket.FlcTcpClient");
        }

        public override async Task Open(string ip, int port)
        {
            TaskCompletionSource<bool> tcs = new TaskCompletionSource<bool>();
            Thread thread = new Thread(() =>
            {
                try
                {
                    if (_javaObject != null)
                    {
                        AndroidJNI.AttachCurrentThread();
                        _javaObject.Call("openSync", ip, port);
                        AndroidJNI.DetachCurrentThread();
                        tcs.SetResult(true);
                    }
                    else
                    {
                        throw new Exception("client already closed");
                    }
                }
                catch (Exception e)
                {
                    tcs.SetException(e);
                }
            });
            thread.Start();
            try
            {
                await tcs.Task;
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
            TaskCompletionSource<bool> tcs = new TaskCompletionSource<bool>();
            Thread thread = new Thread(() =>
            {
                try
                {
                    if (_javaObject != null)
                    {
                        AndroidJNI.AttachCurrentThread();
                        _javaObject.Call("sendSync", data);
                        AndroidJNI.DetachCurrentThread();
                        tcs.SetResult(true);
                    }
                    else
                    {
                        throw new Exception("client already closed");
                    }
                }
                catch (Exception e)
                {
                    tcs.SetException(e);
                }
            });
            thread.Start();
            try
            {
                await tcs.Task;
            }
            catch (Exception e)
            {
                Debug.LogError(string.Format("[FlcTcpClient] send error. address={0}:{1} size={2} message={3}", ip, port, data.Length, e.Message));
                _CheckClose();
                throw e;
            }
        }

        public override void Close()
        {
            if (!isClosed)
            {
                lock (_javaObject)
                {
                    _isClosed = true;
                    try
                    {
                        _javaObject.Call("close");
                    }
                    catch (Exception e) { e.ToString(); }
                    try
                    {
                        _javaObject.Dispose();
                    }
                    catch (Exception e) { e.ToString(); }
                    _javaObject = null;
                    Debug.Log("[FlcTcpClient] closed");
                }
            }
        }

        void _CheckClose()
        {
            try
            {
                if (_javaObject.Call<bool>("isClosed"))
                {
                    Close();
                }
            }
            catch (Exception e) { e.ToString(); }
        }
    }
}

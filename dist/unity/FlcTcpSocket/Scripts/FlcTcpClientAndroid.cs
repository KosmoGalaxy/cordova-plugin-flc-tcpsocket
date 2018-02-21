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
                    AndroidJNI.AttachCurrentThread();
                    _javaObject.Call("openSync", ip, port);
                    AndroidJNI.DetachCurrentThread();
                    _isOpen = true;
                    tcs.SetResult(true);
                }
                catch (Exception e)
                {
                    tcs.SetException(e);
                }
            });
            thread.Start();
            await tcs.Task;
        }

        public override async Task Send(byte[] data)
        {
            TaskCompletionSource<bool> tcs = new TaskCompletionSource<bool>();
            Thread thread = new Thread(() =>
            {
                try
                {
                    AndroidJNI.AttachCurrentThread();
                    _javaObject.Call("sendSync", data);
                    AndroidJNI.DetachCurrentThread();
                    tcs.SetResult(true);
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
                _isClosed = true;
                throw e;
            }
        }

        public override void Close()
        {
            if (!isClosed)
            {
                _isClosed = true;
                _javaObject.Call("close");
            }
        }
    }
}

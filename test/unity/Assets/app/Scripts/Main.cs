using FullLegitCode.TcpSocket;
using System;
using System.Text;
using System.Threading.Tasks;
using UnityEngine;

public class Main : MonoBehaviour
{
    bool _keepRunning = true;
    FlcTcpClient _client;

    void Start()
    {
        _OpenClient();
        _LogTime();
    }

    void OnDestroy()
    {
        _keepRunning = false;
        if (!_client.isClosed)
        {
            _client.Close();
        }
    }

    async void _OpenClient()
    {
        try
        {
            _client = FlcTcpClient.Create();
            await _client.Open("192.168.2.123", 3070);
            Debug.Log("--- client open " + _client.isOpen);
            if (_client.isOpen)
            {
                _Send();
            }
        }
        catch (Exception e)
        {
            Debug.LogError("--- client open error. message=" + e.Message);
        }
    }

    async void _Send()
    {
        try
        {
            while (_keepRunning && !_client.isClosed)
            {
                await _client.Send(Encoding.UTF8.GetBytes("syreni śpiew karyny"));
                Debug.Log("--- client send success");
                await Task.Delay(1000);
            }
        }
        catch (Exception e)
        {
            Debug.LogError("--- client send error. message=" + e.Message + " isClosed=" + _client.isClosed);
        }
    }

    async void _LogTime()
    {
        while (_keepRunning)
        {
            Debug.Log("- " + (Time.time * 1000).ToString("0") + " isOpen=" + _client.isOpen + " isClosed=" + _client.isClosed);
            await Task.Delay(1000);
        }
    }
}

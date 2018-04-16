//
//  FlcTcpSocket.swift
//  cordova-plugin-flc-tcpsocket-iostest
//
//  Created by wojcieszki on 13/04/2018.
//  Copyright © 2018 wojcieszki. All rights reserved.
//

import Foundation

class FlcTcpSocket {
  
  
  static func getMyIp() -> String? {
    let ips: [AnyHashable: String] = UtilObjectiveC.getIPAddresses()
    var myIp: String
    if String(ips["hotspot"]!) != "" {
      myIp = String(ips["hotspot"]!)
    } else {
      myIp = String(ips["wireless"]!)
    }
    return myIp
  }
  
  static func openServer(port: Int32, onClient: @escaping (_ client: TCPClient) -> ()) {
    let address: String = FlcTcpSocket.getMyIp()!
    print("Setting up server at \(address):\(port)")
    let server: TCPServer = TCPServer(address: address, port: port)
    switch server.listen() {
    case .success:
      while true {
        if let client = server.accept() {
          onClient(client)
        } else {
          print("accept error")
        }
      }
    case .failure(let error):
      print(error)
    }
  }
  
  static func connectToServer(address: String, port: Int32) {
    print("Connecting to \(address):\(port)")
    let client = TCPClient(address: address, port: port)
    switch client.connect(timeout: 5) {
    case .success:
      switch client.send(string: "WOLOLO test message" ) {
      case .success:
        guard let data = client.read(1024*10) else { return }
        
        if let response = String(bytes: data, encoding: .utf8) {
          print(response)
        }
      case .failure(let error):
        print(error)
      }
    case .failure(let error):
      print(error)
    }
  }
  
}

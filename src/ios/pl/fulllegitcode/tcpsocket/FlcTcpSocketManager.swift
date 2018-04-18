//
//  FlcTcpSocket.swift
//  cordova-plugin-flc-tcpsocket-iostest
//
//  Created by wojcieszki on 13/04/2018.
//  Copyright © 2018 wojcieszki. All rights reserved.
//

import Foundation

public enum FlcTcpSocketManagerError: Error {
  case serverNotFound
  case clientNotFound
}

let TCP_BUFFER_SIZE = 1024

class FlcTcpSocketManager {
  
  var servers: [Int32: TCPServer]
  
  init() {
    servers = [Int32: TCPServer]()
  }
  
  func getMyIp() -> String? {
    let ips: [AnyHashable: String] = FlcTcpUtilObjectiveC.getIPAddresses()
    var myIp: String
    if String(ips["hotspot"]!) != "" {
      myIp = String(ips["hotspot"]!)
    } else {
      myIp = String(ips["wireless"]!)
    }
    return myIp
  }
  
  func openServer(port: Int32, onClient: @escaping (_ client: TCPClient) -> (), onError: @escaping (_ message: String) -> ()) -> TCPServer {
    let address: String = getMyIp()!
    print("Setting up TCP Socket server at \(address):\(port)")
    let server: TCPServer = TCPServer(address: address, port: port)
    servers[server.getId()] = server
    DispatchQueue.global().async {
      switch server.listen() {
      case .success:
        while true {
          if let client = server.accept() {
            onClient(client)
          } else {
            onError("TCP Socket Server accept client error")
          }
        }
      case .failure(let error):
        onError("TCPServer listen error " + error.localizedDescription)
      }
    }
    return server
  }
  
  func clientReceive(id: Int32, onReceive: @escaping (_ bytes: [UInt8]) -> ()) throws {
    guard let client: TCPClient = getClientById(id) else {
      throw FlcTcpSocketManagerError.clientNotFound
    }
    
    DispatchQueue.global().async {
      while true {
        if let bytes: [UInt8] = client.recv(TCP_BUFFER_SIZE) {
          onReceive(bytes)
        }
      }
    }
  }
  
  func closeServer(_ id: Int32) throws {
    guard let server: TCPServer = getServerById(id) else {
      throw FlcTcpSocketManagerError.serverNotFound
    }
    
    server.close()
    servers[id] = nil
  }
  
  func closeClient(_ id: Int32) throws {
    guard let server = getServerByClientId(id) else {
      throw FlcTcpSocketManagerError.clientNotFound
    }
    
    server.closeClient(id)
  }
  
  func getServerById(_ id: Int32) -> TCPServer? {
    return servers[id]
  }
  
  func getClientById(_ id: Int32) -> TCPClient? {
    for server in servers.values {
      for (clientId, client) in server.clients {
        if clientId == id {
          return client
        }
      }
    }
    return nil
  }
  
  func getServerByClientId(_ id: Int32) -> TCPServer? {
    for server in servers.values {
      for clientId in server.clients.keys {
        if clientId == id {
          return server
        }
      }
    }
    return nil
  }
  
}

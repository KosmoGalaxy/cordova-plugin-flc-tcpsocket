//
//  FlcTcpSocketPlugin.swift
//  HelloCordova
//
//  Created by wojcieszki on 11/04/2018.
//

import Foundation

@objc(FlcTcpSocketPlugin) class FlcTcpSocketPlugin : CDVPlugin {
  
  var isDebug: Bool = false
  var manager: FlcTcpSocketManager
  
  override init() {
    manager = FlcTcpSocketManager()
    super.init()
  }
  
  override func pluginInitialize() {
    manager = FlcTcpSocketManager()
    super.pluginInitialize()
  }
  
  @objc(setDebug:) func setDebug(command: CDVInvokedUrlCommand) {
    isDebug = command.argument(at: 0) as? Bool ?? false
    self.commandDelegate!.run(inBackground: {
      let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
      self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
    })
  }
  
  @objc(openServer:) func openServer(command: CDVInvokedUrlCommand) {
    let port: Int32 = command.argument(at: 0) as! Int32
    self.commandDelegate!.run(inBackground: {
      let server: TCPServer = self.manager.openServer(port: port, onClient: { client in
        let payload: [String: Any] = ["event": "client", "id": client.getId()]
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: payload)
        pluginResult!.setKeepCallbackAs(true)
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
      }, onError: { message in
        let payload: [String: String] = ["event": "error", "message": message]
        let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: payload)
        pluginResult!.setKeepCallbackAs(true)
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
      })
      let payload: [String: Any] = ["event": "open", "id": server.getId()]
      let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: payload)
      pluginResult!.setKeepCallbackAs(true)
      self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
    })
  }
  
  @objc(closeServer:) func closeServer(command: CDVInvokedUrlCommand) {
    let serverId: Int32 = command.argument(at: 0) as! Int32
    self.commandDelegate!.run(inBackground: {
      let pluginResult: CDVPluginResult
      do {
        try self.manager.closeServer(serverId)
        pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
      } catch FlcTcpSocketManagerError.clientNotFound {
        pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "server not found")
      } catch {
        pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "unknown error")
      }
      self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
    })
  }
  
  @objc(clientReceive:) func clientReceive(command: CDVInvokedUrlCommand) {
    let clientId: Int32 = command.argument(at: 0) as! Int32
    self.commandDelegate!.run(inBackground: {
      let pluginResult: CDVPluginResult
      do {
        try self.manager.clientReceive(id: clientId) { bytes in
          let pluginResultReceive = CDVPluginResult(status: CDVCommandStatus_OK, messageAsArrayBuffer: Data(bytes: bytes))
          pluginResultReceive!.setKeepCallbackAs(true)
          self.commandDelegate.send(pluginResultReceive, callbackId: command.callbackId)
          
        }
      } catch FlcTcpSocketManagerError.clientNotFound {
        pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "client not found")
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
      } catch {
        pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "unknown error")
        self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
      }
    })
  }
  
  @objc(closeClient:) func closeClient(command: CDVInvokedUrlCommand) {
    let clientId: Int32 = command.argument(at: 0) as! Int32
    self.commandDelegate!.run(inBackground: {
      let pluginResult: CDVPluginResult
      do {
        try self.manager.closeClient(clientId)
        pluginResult = CDVPluginResult(status: CDVCommandStatus_OK)
      } catch FlcTcpSocketManagerError.clientNotFound {
        pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "client not found")
      } catch {
        pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "unknown error")
      }
      self.commandDelegate.send(pluginResult, callbackId: command.callbackId)
    })
  }
  
}

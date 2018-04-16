//
//  ViewController.swift
//  cordova-plugin-flc-tcpsocket-iostest
//
//  Created by wojcieszki on 13/04/2018.
//  Copyright © 2018 wojcieszki. All rights reserved.
//

import UIKit

let PORT: Int32 = 1337
let IP: String = "192.168.1.144"

class ViewController: UIViewController {

  //MARK: Outlets
  @IBOutlet weak var currentIpLabel: UILabel!
  @IBOutlet weak var serverPortTextField: UITextField!
  @IBOutlet weak var connectToIpTextField: UITextField!
  @IBOutlet weak var connectToPortTextField: UITextField!
  @IBOutlet weak var consoleTextView: UITextView!
  
  override func viewDidLoad() {
    super.viewDidLoad()
    
    currentIpLabel.text = FlcTcpSocket.getMyIp()
    serverPortTextField.text = String(PORT)
    connectToIpTextField.text = IP
    connectToPortTextField.text = String(PORT)
  }

  override func didReceiveMemoryWarning() {
    super.didReceiveMemoryWarning()
  }
  
  func printToConsole(text: String) {
    consoleTextView.text = self.consoleTextView.text + text + "\n"
    let range: NSRange = NSMakeRange(consoleTextView.text.count, 0)
    consoleTextView.scrollRangeToVisible(range)
  }

  //MARK: Actions
  @IBAction func startServer(_ sender: UIButton) {
    let port: Int32 = Int32(serverPortTextField.text!)!
    self.printToConsole(text: "Opening server at port \(port)")
    DispatchQueue.global().async {
      FlcTcpSocket.openServer(port: port) { client in
        DispatchQueue.main.async {
          self.printToConsole(text: "New client from:\(client.address)[\(client.port)]")
          DispatchQueue.global().async {
            while let data = client.recv(1024) {
              DispatchQueue.main.async {
                let msg: String = String(bytes: data, encoding: String.Encoding.utf8)!
                self.printToConsole(text: "Message from client:\(client.address)[\(client.port)] - \(msg)")
                let msgBack: String = "Thanks for sending \(msg)"
                _ = client.send(data: Array(msgBack.utf8))
              }
            }
            client.close()
          }
        }
      }
    }
  }
  
  @IBAction func connectToServer(_ sender: UIButton) {
    let ip: String = connectToIpTextField.text!
    let port: Int32 = Int32(connectToPortTextField.text!)!
    FlcTcpSocket.connectToServer(address: ip, port: port)
  }
}


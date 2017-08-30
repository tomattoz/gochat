//
//  Office356Logger.swift
//  Chat
//
//  Created by Pham Hoa on 8/21/17.
//  Copyright © 2017 ys1382. All rights reserved.
//

import Foundation
import ADAL
import JWTDecode
import NXOAuth2Client

class OfficeAuthentication: NSObject, URLSessionDelegate {
    
    //-- SharedIntance
    static let shared = OfficeAuthentication()
    
    //-- Init
    private override init() {
        var error: ADAuthenticationError?
        self.applicationContext = ADAuthenticationContext.init(authority: kAuthority, validateAuthority: true, error: &error)
    }
    
    // Constants
    
    let kClientID = "044e6315-8adc-4dce-9e8e-d9c4d8fef806"
    let kAuthority = "https://login.microsoftonline.com/common/oauth2/v2.0/authorize"
    let kResource = "https://graph.windows.net"
    let kRedirectUri = "https://login.microsoftonline.com/common/oauth2/v2.0/authorize"
    let kGraphURI = "https://graph.microsoft.com/v1.0/me"
    let kContactGraphURI = "https://graph.microsoft.com/v1.0/me/contacts"
    let kScopes: [String] = ["https://graph.microsoft.com/user.read", "https://graph.microsoft.com/contacts.read"]
    
    // Properties
    
    var accessToken: String?
    var applicationContext: ADAuthenticationContext?

    func login(_ completedHandler: @escaping ((String?)->())) {
        if applicationContext == nil {
            return
        }
        
        
        applicationContext!.acquireToken(withResource: kResource, clientId: kClientID, redirectUri: URL.init(string: kRedirectUri)) { (result) in
            if result != nil {
                self.accessToken = result!.accessToken
                if let idToken = result?.tokenCacheItem?.userInformation?.rawIdToken {
                    if let jwt = try? decode(jwt: idToken) {
                        print(jwt.body)
//                        let email = jwt.claim(name: "preferred_username").string
//                        let id = jwt.claim(name: "tid").string
                        
                    } else {
                        
                    }
                }
            }
        }
        
    }
    
    func signout() {

    }
    
    func getContacts(_ completedHandler: @escaping(([Contact]?)->())) {
        if let url = URL(string: "\(kContactGraphURI)?$top=50") {
            getContacts(url: url, completedHandler)
        } else {
            completedHandler(nil)
        }
    }
    
    private func getContacts(url: URL,_ completedHandler: @escaping(([Contact]?)->())) {
        struct Holder {
            static var contactsList = [Contact]()
        }
        
        if self.accessToken == nil {
            completedHandler(nil)
            return
        }
        
        let sessionConfig = URLSessionConfiguration.default
        
        // Specify the Graph API endpoint
        var request = URLRequest(url: url)
        
        // Set the Authorization header for the request. We use Bearer tokens, so we specify Bearer + the token we got from the result
        request.setValue("Bearer \(self.accessToken!)", forHTTPHeaderField: "Authorization")
        
        //        let parameters = [String: AnyObject]()
        //        let parameterString = parameters.stringFromHttpParameters()
        
        let urlSession = URLSession(configuration: sessionConfig, delegate: self, delegateQueue: OperationQueue.main)
        
        urlSession.dataTask(with: request) { data, response, error in
            let result = try? JSONSerialization.jsonObject(with: data!, options: [])
            if result != nil, let dict = result as? [String: AnyObject] {
                print(dict)
                
                if let contacts = dict["value"] as? [[String: AnyObject]] {
                    
                    for contactDict in contacts {
                        let contactBuilder = Contact.Builder()
                        
                        if let arrEmails = contactDict["emailAddresses"] as? [[String: AnyObject]], let email = arrEmails.first?["address"] as? String {
                            contactBuilder.setId(email).setName(email)

                            if let contact = try? contactBuilder.build() {
                                Holder.contactsList.append(contact)
                            }
                        }
                    }
                }
                
                // recursive
                if let nextLink = dict["@odata.nextLink"] as? String, let url = URL(string: nextLink) {
                    self.getContacts(url: url, completedHandler)
                } else {
                    completedHandler(Holder.contactsList)
                }
            }
        }.resume()
    }
    
    private func getUserInfo(from idToken: String, completedHanlder: @escaping ((String?, String?)->())) {
        if let jwt = try? decode(jwt: idToken) {
            print(jwt.body)
            let email = jwt.claim(name: "preferred_username").string
            let id = jwt.claim(name: "tid").string
            
            completedHanlder(id, email)
        } else {
            completedHanlder(nil, nil)
        }
    }
}

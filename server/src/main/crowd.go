package main

import (
  "fmt"
  "github.com/boltdb/bolt"
  "github.com/gorilla/websocket"
  "math/rand"
  "sync"
  "encoding/json"
  "net/http"
)

type Crowd struct {
  clients             map[string]*Client
  presenceSubscribers map[string][]string // set of subscribers to each client
  clientsMtx          sync.Mutex
  queue               chan Wire
  db                  *bolt.DB
}

func (crowd *Crowd) Init(db *bolt.DB) {
  crowd.queue = make(chan Wire, 5)
  crowd.clients = make(map[string]*Client)
  crowd.presenceSubscribers = make(map[string][]string)
  crowd.db = db

  // loop to send messages from queue
  go func() {
	for {
	  message := <-crowd.queue
	  to := message.GetTo()
	  if to == "" {
		fmt.Println("Send " + message.GetWhich().String() + " to whom?")
		return
	  }

	  client, ok := crowd.clients[to]
	  if ok == false {
		fmt.Println("Can't find " + to)
		return
	  }

	  which := message.Which
	  if which != Wire_CONTACTS { // don't forward sessionId
		message.SessionId = ""
	  }
	  fmt.Printf("Send %s from %s to %s\n", message.GetWhich().String(), message.From, message.To)
	  client.Send(&message)
	}
  }()
}

func (crowd *Crowd) messageArrived(conn *websocket.Conn, wire *Wire, sessionId string) (string, bool) {
  if wire.GetWhich() == Wire_LOGIN {
	sessionId := crowd.receivedLogin(conn, wire.GetLogin())
	return sessionId, true
  }
  sessionId = wire.GetSessionId()
  if sessionId != "" {
	fmt.Println("\nSessionId is " + sessionId)
	crowd.updatePresence(sessionId, true)
  }

  client, ok := crowd.clients[sessionId]
  if !ok {
	if client == nil && sessionId != "" {
	  fmt.Println("no client for " + sessionId)
	  return sessionId, false
	} else {
	  fmt.Println("Session Id is empty, which = " + wire.GetWhich().String())
	}
  }

  switch wire.GetWhich() {
  case Wire_CONTACTS:
	client.receivedContacts(wire)
  case Wire_STORE:
	client.receivedStore(wire)
  case Wire_LOAD:
	client.receivedLoad(wire)
  case Wire_PUBLIC_KEY:
	fallthrough
  case Wire_PUBLIC_KEY_RESPONSE:
	fallthrough
  case Wire_HANDSHAKE:
	fallthrough
  case Wire_PAYLOAD:
	if client == nil {
	  fmt.Printf("client is nil %d\n", len(crowd.clients))
	}
	if wire == nil {
	  fmt.Println("wire is nil")
	}
	forward(client, wire)
  default:
	fmt.Println("No handler for " + wire.GetWhich().String())
  }
  return sessionId, true
}

func (crowd *Crowd) receivedLogin(conn *websocket.Conn, id string) string {
  fmt.Println("Received Login: " + id)
  byt := []byte(id);
  var dat map[string]interface{}
  if err := json.Unmarshal(byt, &dat); err != nil {
	fmt.Println("Json wrong")
  }
  mId := dat["name"].(string)
  mToken := dat["token"].(string)

  defer crowd.clientsMtx.Unlock()
  defer crowd.clientsMtx.Lock()

  sessionId := createSessionId()

  var client *Client
  if c, ok := crowd.clients[mId]; ok {
	client = c
  } else {
	client = &Client{
	  id:       mId,
	  sessions: make(map[string]*websocket.Conn),
	  online:   false,
	}
  }
  client.sessions[sessionId] = conn
  crowd.clients[mId] = client
  if mToken == "normal" {
	loginSuccess(client, sessionId, crowd)
  } else if verifyToken(mToken) {
	loginSuccess(client, sessionId, crowd)
  } else {
	crowd.clients[sessionId] = client
	client.loginFail(sessionId)
  }
  return sessionId
}

func loginSuccess(client *Client, sessionId string, crowd *Crowd) {
  fmt.Printf("New client id = %s, session = %s, len = %d\n", client.id, sessionId, len(client.sessions))
  client.Load(crowd.db)
  crowd.clients[sessionId] = client
  client.sendContacts(sessionId)
  crowd.updatePresence(sessionId, true)
}

func verifyToken(token string) bool {
  client := &http.Client{
  }
  req, err := http.NewRequest("GET", "https://graph.microsoft.com/v1.0/me", nil)
  req.Header.Add("Authorization", "Bearer "+token)
  req.Header.Add("Content-Type", "application/json")
  resp, err := client.Do(req)
  if err != nil {
	fmt.Println("Get Api MS", err)
	return false
  }
  defer resp.Body.Close()
  statusCode := resp.StatusCode
  fmt.Println("verify token:\nStatusCode: ", statusCode)
  if statusCode == 200 {
	return true
  } else {
	return false
  }
}

// todo: need a real GUID generator
func createSessionId() string {
  alphanum := "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
  var bytes = make([]byte, 10)
  rand.Read(bytes)
  for i, b := range bytes {
	bytes[i] = alphanum[b%byte(len(alphanum))]
  }
  return string(bytes)
}

func (crowd *Crowd) updatePresence(sessionId string, online bool) {
  client, ok := crowd.clients[sessionId]
  if !ok {
	fmt.Println("\t can't find " + sessionId)
	return
  }

  crowd.clients[sessionId] = client
  if online == client.online {
	return
  } else {
	fmt.Printf("Update Presence sessionId = %s online=%t\n", sessionId, online)
  }
  client.updatePresence(sessionId, online)

  // inform subscribers
  from := client.id
  contact := &Contact{
	Id:     from,
	Online: online,
  }

  for _, subscriber := range crowd.presenceSubscribers[from] {
	fmt.Println("\t subscriber = " + subscriber)
	update := &Wire{
	  Which:    Wire_PRESENCE,
	  Contacts: []*Contact{contact},
	  To:       subscriber,
	}
	fmt.Printf("\t contacts length = %d\n", len(update.GetContacts()))
	crowd.queue <- *update
  }
  client.subscribeToContacts()
}

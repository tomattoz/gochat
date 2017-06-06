import Foundation

class EventBus {

    enum Event: String {
        case connected
        case disconnected
        case authenticated
        case contacts
        case presence
        case text
    }

    static func post(about: Haber.Which) {
        self.post(Event(rawValue: about.toString())!)
    }

    static func post(_ key: Event) {
        NotificationCenter.default.post(name:Notification.Name(rawValue:key.rawValue), object: nil, userInfo:nil)
    }

    static func post(forKey key: String) {
        NotificationCenter.default.post(name:Notification.Name(rawValue:key), object: nil, userInfo:nil)
    }

    static func addListener(about:Event, didReceive:@escaping (Notification)->Void) {
        NotificationCenter.default.addObserver(
            forName: NSNotification.Name(rawValue: about.rawValue),
            object: nil,
            queue: nil,
            using: didReceive)
    }
}

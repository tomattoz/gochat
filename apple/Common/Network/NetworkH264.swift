
import Foundation

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// NetworkH264Serializer
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

class NetworkH264Serializer : IODataProtocol {
    
    private var next: IODataProtocol?
    
    init(_ next: IODataProtocol?) {
        self.next = next
    }
    
    func process(_ packets: [Int: NSData]) {
        let s = PacketSerializer()
        
        s.push(data: packets[IOPart.Timestamp.rawValue]!)
        s.push(data: packets[H264Part.SPS.rawValue]!)
        s.push(data: packets[H264Part.PPS.rawValue]!)
        s.push(data: packets[H264Part.Data.rawValue]!)
        
        next?.process([VideoPart.NetworkPacket.rawValue: s.data])
    }
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// NetworkH264Deserializer
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

class NetworkH264Deserializer : IODataProtocol {
    
    private var next: IODataProtocol?
    
    init(_ next: IODataProtocol?) {
        self.next = next
    }

    func process(_ data: [Int: NSData]) {

        let d = PacketDeserializer(data[VideoPart.NetworkPacket.rawValue]!)
        var result = [Int: NSData]()
        
        result[IOPart.Timestamp.rawValue] = d.popData()
        result[H264Part.SPS.rawValue] = d.popData()
        result[H264Part.PPS.rawValue] = d.popData()
        result[H264Part.Data.rawValue] = d.popData()
        
        next?.process(result)
    }
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Video format
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

extension VideoFormat {
    
    func toNetwork() throws -> NSData {
        return try JSONSerialization.data(withJSONObject: data,
                                          options: JSONSerialization.defaultWritingOptions) as NSData
    }

    static func fromNetwork(_ data: NSData) throws -> VideoFormat {
        let json = try JSONSerialization.jsonObject(with: data as Data,
                                                    options: JSONSerialization.ReadingOptions()) as! [String: Any]
        return VideoFormat(json)
    }
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// NetworkOutputVideo
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

class NetworkOutputVideo : IODataProtocol {
    
    let id: IOID
    
    init(_ id: IOID) {
        self.id = id
    }
    
    func process(_ data: [Int: NSData]) {
        Backend.shared.sendVideo(id, data[VideoPart.NetworkPacket.rawValue]!)
    }
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// NetworkOutputVideoSession
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

class NetworkOutputVideoSession : VideoSessionProtocol {
    
    let id: IOID
    let format: VideoFormat
    
    init(_ id: IOID, _ format: VideoFormat) {
        self.id = id
        self.format = format
    }
    
    func start() throws {
        Backend.shared.sendVideoSession(id, try format.toNetwork(), true)
    }
    
    func update(_ format: VideoFormat) throws {
        // TODO: send update format
    }
    
    func stop() {
        Backend.shared.sendVideoSession(id, nil, false)
    }
}


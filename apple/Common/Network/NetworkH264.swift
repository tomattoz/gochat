
import Foundation

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// NetworkH264Serializer
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

class NetworkH264Serializer : IODataProtocol {
    
    private var next: IODataProtocol?
    
    init(_ next: IODataProtocol?) {
        self.next = next
    }
    
    func process(_ data: [Int: NSData]) {
        let result = NSMutableData()
        
        _process(data, H264Part.Time, result)
        _process(data, H264Part.SPS, result)
        _process(data, H264Part.PPS, result)
        _process(data, H264Part.Data, result)

        next?.process([H264Part.NetworkPacket.rawValue: result])
    }
    
    func _process(_ data: [Int: NSData], _ part: H264Part, _ result: NSMutableData) {
        var size: UInt32 = UInt32(data.keys.contains(part.rawValue)
            ? data[part.rawValue]!.length
            : 0)
        
        result.append(&size, length: MemoryLayout<UInt32>.size)
        
        if (size != 0) {
            result.append(data[part.rawValue]! as Data)
        }
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
        
        var shift = 0
        let packet = data[H264Part.NetworkPacket.rawValue]!
        var result = [Int: NSData]()

        if let part = _process(data: packet, shift: &shift) {
            result[H264Part.Time.rawValue] = part
        }

        if let part = _process(data: packet, shift: &shift) {
            result[H264Part.SPS.rawValue] = part
        }

        if let part = _process(data: packet, shift: &shift) {
            result[H264Part.PPS.rawValue] = part
        }

        if let part = _process(data: packet, shift: &shift) {
            result[H264Part.Data.rawValue] = part
        }
        
        next?.process(result)
    }
    
    func _process(data: NSData, shift: inout Int) -> NSData? {
        
        var size: UInt32 = 0
        
        data.getBytes(&size, range: NSRange(location: shift, length: MemoryLayout<UInt32>.size))
        shift += MemoryLayout<UInt32>.size
        
        if (size == 0) {
            return nil
        }

        let result = NSData(bytes: data.bytes.advanced(by: shift), length: Int(size))
        shift += Int(size)
        return result
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
    
    let to: String
    let sid: String
    
    init(_ to: String, _ sid: String) {
        self.to = to
        self.sid = sid
    }
    
    func process(_ data: [Int: NSData]) {
        Backend.shared.sendVideo(to, sid, data[H264Part.NetworkPacket.rawValue]!)
    }
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// NetworkOutputVideoSession
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

class NetworkOutputVideoSession : VideoSessionProtocol {
    
    let to: String
    let sid: String
    let format: VideoFormat
    
    init(_ to: String, _ sid: String, _ format: VideoFormat) {
        self.to = to
        self.sid = sid
        self.format = format
    }
    
    func start() throws {
        Backend.shared.sendVideoSession(to, sid, try format.toNetwork(), true)
    }
    
    func update(_ format: VideoFormat) throws {
        // TODO: send update format
    }
    
    func stop() {
        Backend.shared.sendVideoSession(to, sid, nil, false)
    }
}


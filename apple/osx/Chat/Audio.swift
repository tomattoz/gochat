import Foundation
import AVFoundation

struct IOBus {
    static let input = 0
    static let output = 1
}

class Audio {
    let engineI = AVAudioEngine()
    let engineO = AVAudioEngine()
    let player = AVAudioPlayerNode()

    init() {
        let format = engineI.inputNode!.inputFormat(forBus: IOBus.input)
        let node = AVAudioMixerNode()
        
        engineI.attach(node)
        engineO.attach(player)
        
        node.installTap(onBus: IOBus.input,
                        bufferSize: 4096,
                        format: format,
                        block:
        { (buffer: AVAudioPCMBuffer, time: AVAudioTime) in
            
            let buffer_serialized = buffer.serialize()
            let buffer_deserialized = AVAudioPCMBuffer.deserialize(buffer_serialized, format)

            logIO("audio \(AVAudioTime.seconds(forHostTime: time.hostTime))")
            
            self.player.scheduleBuffer(buffer_deserialized, completionHandler: nil)
        })
        
        engineI.connect(engineI.inputNode!, to: node, format: format)
        engineI.prepare()
        
        engineO.connect(player, to: engineO.outputNode, format: format)
        engineO.prepare()
    }
    
    func start() {
        try! engineI.start()
        try! engineO.start()

        player.play()
    }
    
    func stop() {
        engineI.stop()
        engineO.stop()
    }
}

extension AVAudioPCMBuffer {

    func serialize() -> NSData {
        let channelCount = 1  // given PCMBuffer channel count is 1
        let channels = UnsafeBufferPointer(start: self.floatChannelData, count: channelCount)
        let length = Int(self.frameCapacity * self.format.streamDescription.pointee.mBytesPerFrame)

        return NSData(bytes: channels[0], length:length)
    }

    static func deserialize(_ data: NSData, _ format: AVAudioFormat) -> AVAudioPCMBuffer {
        assert(UInt32(data.length) % format.streamDescription.pointee.mBytesPerFrame == 0)

        let frameCapacity = AVAudioFrameCount(UInt32(data.length) / format.streamDescription.pointee.mBytesPerFrame)
        let result = AVAudioPCMBuffer(pcmFormat: format, frameCapacity: frameCapacity)
        let channels = UnsafeBufferPointer(start: result.floatChannelData, count: Int(result.frameCapacity))

        data.getBytes(UnsafeMutableRawPointer(channels[0]) , length: data.length)
        result.frameLength = frameCapacity

        return result
    }
}

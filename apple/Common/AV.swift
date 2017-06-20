
import AVFoundation

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Assertions
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

func assert_audio_capture_queue() {
    assert(DispatchQueue.OnQueue(AV.shared.audioCaptureQueue))
}

func assert_video_capture_queue() {
    assert(DispatchQueue.OnQueue(AV.shared.videoCaptureQueue))
}

func assert_av_output_queue() {
    assert(DispatchQueue.OnQueue(AV.shared.avOutputQueue))
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// AV
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

class AV {
    
    static let shared = AV()
    static let defaultAudioFormatID = kAudioFormatMPEG4AAC
    static let defaultAudioInterval = 0.1
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IO
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    let audioCaptureQueue = DispatchQueue.CreateCheckable("chat.AudioCaptureQueue")
    let videoCaptureQueue = DispatchQueue.CreateCheckable("chat.VideoCaptureQueue")
    let avOutputQueue = DispatchQueue.CreateCheckable("chat.AVOutputQueue")

    private(set) var activeInput: IOSessionProtocol?
    private(set) var activeOutput = [String: IOSessionProtocol]()
    private(set) var activeIOSync = [String: IOSync]()
    
    init() {
        defaultVideoDimension = defaultVideoInputDevice?.activeFormat.dimensions
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Input
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    var defaultVideoInputDevice: AVCaptureDevice? {
        return AVCaptureDevice.chatVideoDevice()
    }
    
    var defaultVideoDimension: CMVideoDimensions?

    var defaultVideoInputFormat: AVCaptureDeviceFormat? {
        get {
            guard let dimensions = defaultVideoDimension else { return nil }
            return defaultVideoInputDevice?.inputFormat(width: dimensions.width)
        }
    }

    var defaultAudioInputFormat: AudioStreamBasicDescription? {
        guard let inputFormat = AVAudioEngine().inputNode?.inputFormat(forBus: AudioBus.input) else { return nil }

        return AudioStreamBasicDescription.CreateVBR(AV.defaultAudioFormatID,
                                                           inputFormat.sampleRate/*8000*/,
                                                           1/*inputFormat.channelCount*/)
    }

    private func _defaultVideoInput(_ id: IOID,
                                    _ rotated: Bool,
                                    _ session: inout AVCaptureSession.Accessor?,
                                    _ x: inout [VideoSessionProtocol]) {
        guard let device = AVCaptureDevice.chatVideoDevice() else { return }
        guard var outFormat = defaultVideoOutputFormat else { return }
        guard let inpFormat = defaultVideoInputFormat else { return }
        
        if rotated {
            outFormat.rotate()
        }
        
        let sessionEncoder =
            VideoEncoderSessionH264(
                inpFormat.dimensions,
                outFormat,
                VideoEncoderH264(
                    NetworkH264Serializer(
                        NetworkOutputVideo(id))))
        let sessionNetwork = NetworkOutputVideoSession(id, outFormat)

        let videoInput =
            VideoInput(
                device,
                AV.shared.videoCaptureQueue,
                inpFormat,
                sessionEncoder)
        
        session = videoInput.sessionAccessor
        x.append(VideoSessionBroadcast([sessionEncoder, videoInput, sessionNetwork]))
    }

    private func _defaultAudioInput(_ id: IOID, _ x: inout [IOSessionProtocol]) {
        
        guard let format = defaultAudioInputFormat else { return }
        
        let input =
            AudioInput(
                format,
                AV.defaultAudioInterval,
                NetworkAudioSerializer(
                    NetworkOutputAudio(id)))

        let sessionNetwork =
            NetworkOutputAudioSession(id, input.format)

        x.append(IOSessionBroadcast([input, sessionNetwork]))
    }

    func startInput(_ x: IOSessionProtocol?) throws {
        activeInput?.stop()
        activeInput = x
        try activeInput?.start()
    }
    
    func defaultVideoInput(_ id: IOID,
                           _ rotated: Bool,
                           _ session: inout AVCaptureSession.Accessor?) -> VideoSessionProtocol? {
        var x = [VideoSessionProtocol]()
        
        _defaultVideoInput(id, rotated, &session, &x)
        return create(x)
    }

    func defaultVideoInput(_ id: IOID,
                           _ preview: AVCaptureVideoPreviewLayer) -> VideoSessionProtocol? {
        var x = [VideoSessionProtocol]()
        var y: AVCaptureSession.Accessor?
        
        _defaultVideoInput(id, false, &y, &x)
        
        if y != nil {
            x.append(VideoPreview(preview, y!))
        }
        
        return create(x)
    }

    func defaultAudioInput(_ id: IOID) -> IOSessionProtocol? {
        var x = [IOSessionProtocol]()
        _defaultAudioInput(id, &x)
        return create(x)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Output
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    var defaultVideoOutputFormat: VideoFormat? {
        get {
            guard let dimensions = defaultVideoInputFormat?.dimensions else { return nil }
            return VideoFormat(dimensions)
        }
    }

    func defaultIOSync(_ gid: String) -> IOSync {
        let result = activeIOSync[gid]
        
        if result != nil {
            return result!
        }
        
        activeIOSync[gid] = IOSync()
        
        return activeIOSync[gid]!
    }

    private func cleanupSync() {
        _ = activeIOSync.keys.map({ self.cleanupSync($0) })
    }

    private func cleanupSync(_ gid: String) {
        guard let sync = activeIOSync[gid] else { return }
        guard sync.active == false else { return }
        
        activeIOSync.removeValue(forKey: gid)
    }

    func startOutput(_ id: IOID, _ kind: IOKind, _ session: IOSessionProtocol) throws {
        try session.start()
        activeOutput[id.from + String(describing: kind)] = session
    }
    
    func stopOutput(_ id: IOID, _ kind: IOKind) {
        activeOutput[id.from + String(describing: kind)]?.stop()
        activeOutput.removeValue(forKey: id.from)
        cleanupSync(id.gid)
    }
    
    func stopAllOutput() {
        _ = activeOutput.values.map({ $0.stop() })
        activeOutput.removeAll()
        cleanupSync()
    }
    
    func defaultNetworkVideoOutput(_ id: IOID,
                                   _ output: VideoOutputProtocol,
                                   _ session: inout IOSessionProtocol?) -> IODataProtocol {
        
        let time =
            VideoTimeSerializer(IOPart.Timestamp.rawValue)
        
        let sync =
            defaultIOSync(id.gid)
        
        let syncBus =
            IOSyncBus(
                id.sid,
                IOKind.Video,
                sync)
        
        let result =
            IODataDispatcher(
                avOutputQueue,
                NetworkH264Deserializer(
                    IOTimebaseReset(
                        sync,
                        time,
                        syncBus)))
        
        sync.add(
            IOKind.Video,
            time,
            VideoDecoderH264Data(
                VideoDecoderH264(
                output)))

        session = create([syncBus])
        
        return result
    }

    func startDefaultNetworkVideoOutput(_ id: IOID,
                                        _ output: VideoOutputProtocol,
                                        _ session: IOSessionProtocol?) throws -> IODataProtocol {
        var session2: IOSessionProtocol?
        let result = AV.shared.defaultNetworkVideoOutput(id, output, &session2)
        
        if session2 != nil {
            try startOutput(id, IOKind.Video, create([session, session2])!)
        }
        
        return result
    }
    
    func defaultNetworkAudioOutput(_ id: IOID,
                                   _ format: AudioFormat,
                                   _ session: inout IOSessionProtocol?) -> IODataProtocol {
        let time =
            AudioTimeSerializer(AudioPart.NetworkPacket.rawValue, MemoryLayout<UInt32>.size)
        
        let output =
            AudioOutput(
                factory(format),
                avOutputQueue)
        
        let decoder =
            AudioDecoder(
                factory(format),
                output.format,
                output)
        
        let sync =
            defaultIOSync(
                id.gid)
        
        let syncBus =
            IOSyncBus(
                id.sid,
                IOKind.Audio,
                sync)
        
        let result =
            IODataDispatcher(
                avOutputQueue,
                IOTimebaseReset(
                    sync,
                    time,
                    syncBus))
        
        sync.add(
            IOKind.Audio,
            time,
            NetworkAudioDeserializer(
                decoder))
        
        session = create([output, decoder, syncBus])
        
        return result
    }
    
    func setupDefaultNetworkAudioOutput(_ platformSession: IOSessionProtocol?) {        
        let audioSessionStart = { (_ id: IOID, format: AudioFormat) throws -> IODataProtocol in
            var session: IOSessionProtocol? = nil
            let result = AV.shared.defaultNetworkAudioOutput(id, format, &session)
            
            if platformSession != nil && session != nil {
                let shared = session!
                let broadcast = create([platformSession!, shared])
                
                session = broadcast
            }
            
            if session == nil {
                session = platformSession
            }
            
            if session != nil {
                self.stopAllOutput()
                try self.startOutput(id, IOKind.Audio, session!)
            }
            
            return result
        }
        
        let audioSessionStop = { (_ id: IOID) in
            self.stopOutput(id, IOKind.Audio)
        }
        
        Backend.shared.audioSessionStart = { (_ id: IOID, format: AudioFormat) in
            var result: IODataProtocol?
            
            self.avOutputQueue.sync {
                do {
                    result = try audioSessionStart(id, format)
                }
                catch {
                    logIOError(error)
                }
            }
            
            return result
        }
        
        Backend.shared.audioSessionStop = { (_ id: IOID) in
            self.avOutputQueue.sync {
                audioSessionStop(id)
            }
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Playback
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    var defaultAudioInputUncompressedFormat: AudioStreamBasicDescription? {
        guard let inputFormat = AVAudioEngine().inputNode?.inputFormat(forBus: AudioBus.input) else { return nil }
        var format = inputFormat.streamDescription.pointee
        
        format.mChannelsPerFrame = 1
        return format
    }

    func startAudioUncompressedPlayback() throws {
        guard let format = defaultAudioInputUncompressedFormat else { return }
        
        let input =
            AudioInput(
                format,
                AV.defaultAudioInterval)
        
        let output =
            AudioOutput(
                input.format,
                AV.shared.audioCaptureQueue)
        
        input.output =
            NetworkAudioSerializer(
                NetworkAudioDeserializer(
                    output))
        
        try audioCaptureQueue.sync {
            try AV.shared.startInput(create([output, input]))
        }
    }

    func startAudioCompressedPlayback() throws {
        guard let format = defaultAudioInputFormat else { return }

        let input =
            AudioInput(
                format,
                AV.defaultAudioInterval)
        
        let output =
            AudioOutput(
                input.format,
                AV.shared.audioCaptureQueue)
        
        let decoder =
            AudioDecoder(
                input.format,
                output.format,
                output)
        
        input.output =
            NetworkAudioSerializer(
                NetworkAudioDeserializer(
                    decoder))
        
        try audioCaptureQueue.sync {
            try AV.shared.startInput(create([input, output, decoder]))
        }
    }
}

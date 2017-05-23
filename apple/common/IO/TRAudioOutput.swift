//
//  Output.swift
//  Chat
//
//  Created by Ivan Khvorostinin on 23/05/2017.
//  Copyright © 2017 ys1382. All rights reserved.
//

import AudioToolbox

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// TRAudioOutputProtocol
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

protocol TRAudioOutputProtocol {

    func start(_ format: UnsafePointer<AudioStreamBasicDescription>,
               _ maxPacketSize: UInt32,
               _ interval: Double)

    func process(_ buffer: AudioQueueBufferRef,
                 _ packetDesc: UnsafePointer<AudioStreamPacketDescription>,
                 _ packetNum: UInt32,
                 _ timeStamp: UnsafePointer<AudioTimeStamp>)

    func stop()
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// TRAudioOutputChain
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

class TRAudioOutputChain : TRAudioOutputProtocol {
    
    private let chain: [TRAudioOutputProtocol]
    
    init(_ chain: [TRAudioOutputProtocol]) {
        self.chain = chain
    }
    
    func start(_ format: UnsafePointer<AudioStreamBasicDescription>,
               _ maxPacketSize: UInt32,
               _ interval: Double) {
        _ = chain.map({ $0.start(format, maxPacketSize, interval) })
    }
    
    func process(_ buffer: AudioQueueBufferRef,
                 _ packetDesc: UnsafePointer<AudioStreamPacketDescription>,
                 _ packetNum: UInt32,
                 _ timeStamp: UnsafePointer<AudioTimeStamp>) {
        _ = chain.map({ $0.process(buffer, packetDesc, packetNum, timeStamp) })
    }
    
    func stop() {
        _ = chain.map({ $0.stop() })
    }

}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// TRAudioOutput
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

class TRAudioOutput : TRAudioOutputProtocol {
    
    private var queue: AudioQueueRef?
    private var buffer: AudioQueueBufferRef?
    private var packetsToRead: UInt32 = 0
    
    func start(_ format: UnsafePointer<AudioStreamBasicDescription>,
               _ maxPacketSize: UInt32,
               _ interval: Double) {
        
        do {
            // create queue

            try checkStatus(AudioQueueNewOutput(format,
                                                callback,
                                                Unmanaged.passUnretained(self).toOpaque(),
                                                CFRunLoopGetCurrent(),
                                                CFRunLoopMode.commonModes.rawValue,
                                                0,
                                                &queue), "AudioQueueNew failed")
            
            // we need to calculate how many packets we read at a time, and how big a buffer we need
            // we base this on the size of the packets in the file and an approximate duration for each buffer
            // first check to see what the max size of a packet is - if it is bigger
            // than our allocation default size, that needs to become larger
            
            // adjust buffer size to represent about a half second of audio based on this format
            var bufferByteSize: UInt32 = 0
            _calculateBytesForTime (format.pointee, maxPacketSize, interval, &bufferByteSize, &packetsToRead)
            
            let isFormatVBR = format.pointee.mBytesPerPacket == 0 || format.pointee.mFramesPerPacket == 0
            
            try checkStatus(AudioQueueAllocateBufferWithPacketDescriptions(queue!,
                                                                           bufferByteSize,
                                                                           isFormatVBR ? packetsToRead : 0,
                                                                           &buffer), "AudioQueueAllocateBuffer failed")
            
            // set the volume of the queue
            try checkStatus(AudioQueueSetParameter(queue!,
                                                   kAudioQueueParam_Volume,
                                                   1.0), "Set queue volume failed");
            
            // start queue
            
            try checkStatus(AudioQueueStart(queue!,
                                            nil), "AudioQueueStart failed")
        }
        catch {
            logIOError(error.localizedDescription)
        }
    }
    
    func stop() {
        
        do {
            
            try checkStatus(AudioQueueStop(queue!,
                                           true), "AudioQueueStop failed")
            
            try checkStatus(AudioQueueDispose(queue!,
                                              true), "AudioQueueDispose failed")
            
            queue = nil
        }
        catch {
            logIOError(error.localizedDescription)
        }
    }
    
    func process(_ buffer: AudioQueueBufferRef,
                 _ packetDesc: UnsafePointer<AudioStreamPacketDescription>,
                 _ packetNum: UInt32,
                 _ timeStamp: UnsafePointer<AudioTimeStamp>) {
        
        do {
            memcpy(self.buffer!.pointee.mAudioData, buffer.pointee.mAudioData, Int(buffer.pointee.mAudioDataByteSize))
            self.buffer!.pointee.mAudioDataByteSize = buffer.pointee.mAudioDataByteSize

            try checkStatus(AudioQueueEnqueueBuffer(queue!,
                                                    self.buffer!,
                                                    packetNum,
                                                    packetDesc), "AudioQueueEnqueueBuffer failed")
        }
        catch {
            logIOError(error.localizedDescription)
        }

    }
    
    private func _calculateBytesForTime(_ inDesc: AudioStreamBasicDescription,
                                        _ inMaxPacketSize: UInt32,
                                        _ inSeconds: Double,
                                        _ outBufferSize: inout UInt32,
                                        _ outNumPackets: inout UInt32)
    {
        // we only use time here as a guideline
        // we're really trying to get somewhere between 16K and 64K buffers, but not allocate too much if we don't need it
        let maxBufferSize: UInt32 = 0x10000; // limit size to 64K
        let minBufferSize: UInt32 = 0x4000; // limit size to 16K

        if (inDesc.mFramesPerPacket != 0) {
            let numPacketsForTime = inDesc.mSampleRate / Double(inDesc.mFramesPerPacket) * inSeconds
            outBufferSize = UInt32(numPacketsForTime * Double(inMaxPacketSize))
        }
        else {
            // if frames per packet is zero, then the codec has no predictable packet == time
            // so we can't tailor this (we don't know how many Packets represent a time period
            // we'll just return a default buffer size
            outBufferSize = maxBufferSize > inMaxPacketSize ? maxBufferSize : inMaxPacketSize;
        }
        
        // we're going to limit our size to our default
        if (outBufferSize > maxBufferSize && outBufferSize > inMaxPacketSize) {
            outBufferSize = maxBufferSize
        }
        else if (outBufferSize < minBufferSize) {
            // also make sure we're not too small - we don't want to go the disk for too small chunks
            outBufferSize = minBufferSize;
        }
        
        outNumPackets = outBufferSize / inMaxPacketSize;
    }

    private let callback: AudioQueueOutputCallback = {
        (inUserData: UnsafeMutableRawPointer?,
        inAQ: AudioQueueRef,
        inBuffer: AudioQueueBufferRef) in
        
    }
}
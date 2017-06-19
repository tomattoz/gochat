
import Foundation
import CoreMedia

func logIOSync(_ message: String) {
//    print("Sync: \(message)")
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// IOSyncBus
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

class IOSyncBus : IODataProtocol {
    
    private let kind: IOKind
    private let sync: IOSync
    
    init(_ kind: IOKind, _ sync: IOSync) {
        self.kind = kind
        self.sync = sync
    }
    
    func process(_ data: [Int : NSData]) {
        sync.process(kind, data)
    }
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// IOSync
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

class IOSync : IOSessionProtocol {
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Internal Structs
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private struct _TimerItem {
        
        let id: Int
        let kind: IOKind
        let data: [Int : NSData]
        
        init(_ id: Int, _ kind: IOKind, _ data: [Int : NSData]) {
            self.id = id
            self.kind = kind
            self.data = data
        }
    }
    
    private struct _QueueItem {
        var timer: Timer
        let data: _TimerItem

        init(_ timer: Timer, _ data: _TimerItem) {
            self.timer = timer
            self.data = data
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Fields
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private var started = false
    
    private var id: Int = 0
    private var nextID: Int { get { id += 1; return id } }

    private var output = [IOKind: IODataProtocol?]()
    private var timing = [IOKind: IOTimeProtocol]()
    
    private let gap: IOSyncGap
    
    private var thread: ChatThread?
    private var queue = [Int: _QueueItem]()
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Interface
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    init(_ gap: IOSyncGap) {
        self.gap = gap
    }
    
    init() {
        self.gap = IOSyncGap()
    }
    
    func add(_ kind: IOKind, _ time: IOTimeProtocol, _ output: IODataProtocol?) {
        self.output[kind] = output
        self.timing[kind] = time
    }
    
    func start() throws {
        guard started == false else { return }

        thread = ChatThread(IOSync.self)
        thread!.start()
        started = true
    }
    
    func stop() {
        thread!.cancel()
        thread = nil
        started = false
    }
    
    func process(_ kind: IOKind, _ data: [Int : NSData]) {
        output[kind]!?.process(data)
//        _enqueue(kind, data)
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Sheduling
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    private func _enqueue(_ kind: IOKind, _ data: [Int : NSData]) {
        thread!.sync({
            let id = self.nextID
            let remoteTime = self.timing[kind]!.time(data)
            let timerItem = _TimerItem(id, kind, data)
            
            let shedule: IOSyncGap.Shedule =  { (_ time: Double) in
                self._shedule(timerItem, time)
                logIOSync("Sheduling \(kind) data with id \(id)")
            }
            
            let reshedule: IOSyncGap.Shedule =  { (_ shift: Double) in
                self._reshedule(shift)
                logIOSync("Resheduling with shift \(shift)")
            }
            
            let zombie: FuncVV = {
                logIOSync("Belated \(kind) data with id \(id) lost")
            }
            
            self.gap.process(remoteTime, shedule: shedule, reshedule: reshedule, zombie: zombie)
        })
    }
    
    private func _timer(_ at: Double, _ id: Int) -> Timer {
        return Timer(fireAt: Date(timeIntervalSince1970: at),
                     interval: 0,
                     target: self,
                     selector: #selector(_output(timer:)),
                     userInfo: id,
                     repeats: false)
    }
    
    private func _shedule(_ data: _TimerItem, _ at: Double) {
        let timer = _timer(at, data.id)
        queue[id] = _QueueItem(timer, data)
        sheduleTimer(timer)
    }
    
    private func _reshedule(_ shift: Double) {
        for var i in queue {
            i.value.timer.invalidate()
            i.value.timer = _timer(i.value.timer.fireDate.addingTimeInterval(shift).timeIntervalSince1970,
                                   i.value.data.id)
            sheduleTimer(i.value.timer)
        }
    }
    
    @objc private func _output(timer: Timer) {
        let id = timer.userInfo as! Int
        
        _output(queue[id]!.data)
        queue.removeValue(forKey: id)
    }
    
    private func _output(_ x: _TimerItem) {
      
        AV.shared.avOutputQueue.async {
            self.output[x.kind]!?.process(x.data)
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Test support
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    fileprivate func sheduleTimer(_ timer: Timer) {
        thread!.runLoop.add(timer, forMode: .defaultRunLoopMode)
    }
}

class IOSyncGap {
    
    // Shedule argument - local time to shedule
    // Reshedule argument - time shift in seconds for sheduled data
    typealias Shedule = (Double) -> Void
    
    private var localZero: Double?
    private var remoteZero: Double?
    private var remoteLast = 0.0
    private var shedule = [Int64: Double]() // local nanoseconds : remote seconds

    private var gap = 0.0
    private var gapMax: Int = 30
    private var gapLog = [Double]()

    var packets: Int = 0

    var localTime: Double {
        get {
            return Date().timeIntervalSince1970
        }
    }
    
    func process(_ remoteTime: Double,
                 shedule: @escaping Shedule,
                 reshedule: @escaping Shedule,
                 zombie: @escaping FuncVV) {
        let localTime = self.localTime
        var callback: FuncVV?

        packets += 1

        // init
        
        if localZero == nil {
            localZero = localTime
            remoteZero = remoteTime
        }

        // calc gap
        
        let gap = (localTime - localZero!) - (remoteTime - remoteZero!)
        let gapPrev = self.gap
        let sheduleTime = localTime + self.gap - gap

        _updateGap(gap)

        // sheduling
        
        let shedule_: Shedule = { (time: Double) in
            shedule(time)
            self.shedule[seconds2nano(sheduleTime)] = remoteTime
        }
        
        // belated
        
        if _belated(remoteTime, localTime) {
            callback = { zombie() }
        }
        
        // reshedule + shedule

        else if micro(gap) > micro(self.gap) {
            callback = { reshedule(self.gap - gapPrev); shedule_(sheduleTime) }
        }

        // shedule
        
        else {
            callback = { shedule_(sheduleTime) }
        }

        if packets > 10 {
            callback!()
        }
    }
    
    private func _belated(_ remoteTime: Double, _ localTime: Double) -> Bool {
        
        let localNano = seconds2nano(localTime)

        for i in shedule.keys {
            if i > localNano {
                continue
            }

            if remoteLast < shedule[i]! {
                remoteLast = shedule[i]!
            }
            
            shedule.removeValue(forKey: i)
        }
        
        return remoteTime < remoteLast
    }
    
    private func _calc() -> Double {
        let gapSorted = gapLog.sorted()
        let diffAverage = (gapSorted.last! - gapSorted.first!) / Double(gapSorted.count)
        var index: Int = gapSorted.count - 1
        
        while index >= gapSorted.count * 10 / 100 && index > 1 {
            if gapSorted[index] - gapSorted[index - 1] < diffAverage {
                break
            }
            
            index -= 1
        }
        
        return gapSorted[index]
    }
    
    private func _updateGap(_ gap: Double) {
        gapLog.append(gap)
        
        if gapLog.count > gapMax {
            gapLog.removeFirst()
        }

        self.gap = _calc()
        
        print("gap \(self.gap)")
    }
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// IOSyncTest
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

class IOSyncTest : IOSync {
    
    let gap = IOSyncTestGap()
    let time = IOSyncTestTime()
    
    let zeroLocal = 0.0
    let zeroRemote = 1000.0
    var timeLocal: Double = 0
    var timeShedule: Double = 0
    
    var success = true
    
    override init() {
        super.init(gap)
        
        add(.Audio, time, nil)
        add(.Video, time, nil)
    }

    func test() -> Bool {
        try! start()
        return success
    }
    
    override func start() throws {
        try super.start()
        
        _test(.Video, 0.05, 0.2) // 0.07
        _test(.Video, 0.10, 0.2) // 0.12
        _test(.Audio, 0.10, 0.3) // 0.13
        _test(.Video, 0.15, 0.2) // 0.17
        _test(.Audio, 0.20, 0.2) // 0.22
        _test(.Video, 0.21, 0.2) // 0.23
        _test(.Audio, 0.30, 0.2) // 0.32
        _test(.Video, 0.30, 0.2) // 0.32
        _test(.Video, 0.25, 0.9) // 0.34
        _test(.Video, 0.35, 0.2) // 0.37
        _test(.Video, 0.39, 0.2) // 0.41
        _test(.Audio, 0.40, 0.2) // 0.42
        _test(.Video, 0.45, 0.2) // 0.47
        _test(.Audio, 0.50, 0.2) // 0.52
    }
    
    override func stop() {
        super.stop()
    }
    
    override func sheduleTimer(_ timer: Timer) {
        timeShedule = timer.fireDate.timeIntervalSince1970
    }
    
    private func _test(_ kind: IOKind, _ captureTime: Double, _ delay: Double) {
        print("ct \(captureTime)")
        gap.localTime = zeroLocal + captureTime + delay
        process(kind, time.createPacket(zeroRemote + captureTime))
    }
}

class IOSyncTestGap : IOSyncGap {
    
    private var _localTime: Double = 0
    
    override var localTime: Double {
        get {
            return _localTime
        }
        set {
            _localTime = newValue
        }
    }
}

class IOSyncTestTime : IOTimeProtocol {

    static let kTime: Int = 0
    
    func createPacket(_ time: Double) -> [Int : NSData] {
        var result = [Int : NSData]()
        self.time(&result, time)
        return result
    }
    
    func time(_ data: [Int : NSData]) -> Double {
        var result: Double = 0
        memcpy(&result, data[IOSyncTestTime.kTime]!.bytes, MemoryLayout<Double>.size)
        return result
    }
    
    func time(_ data: inout [Int : NSData], _ time_: Double) {
        var time = time_
        data[IOSyncTestTime.kTime] = NSData(bytes: &time, length: MemoryLayout<Double>.size)
    }
}

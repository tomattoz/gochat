
import Foundation

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Typedefs
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

#if os(iOS)
    import UIKit
    typealias AppleView = UIView
    typealias AppleColor = UIColor
    typealias AppleApplicationDelegate = UIResponder
#else
    import Cocoa
    typealias AppleView = NSView
    typealias AppleColor = NSColor
    typealias AppleApplicationDelegate = NSObject
#endif

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Lambdas
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

typealias FuncVV = () -> Void

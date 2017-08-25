import UIKit
import AVFoundation
import MSAL

@UIApplicationMain
class AppDelegate: Application, UIApplicationDelegate, UISplitViewControllerDelegate {

    var window: UIWindow?

    func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplicationLaunchOptionsKey: Any]?) -> Bool {

        // enable for Video capture
        
        UIDevice.current.beginGeneratingDeviceOrientationNotifications()

        // UI
        
        let splitViewController = self.window!.rootViewController as! UISplitViewController
        let last = splitViewController.viewControllers.count-1
        let navigationController = splitViewController.viewControllers[last] as! UINavigationController
        navigationController.topViewController!.navigationItem.leftBarButtonItem = splitViewController.displayModeButtonItem
        splitViewController.delegate = self
        
        // Audio
        
        do {
            try AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayAndRecord)
            try AVAudioSession.sharedInstance().setActive(true)
        }
        catch {
            logIOError(error)
        }

        // done

        return true
    }
    
    // @brief Handles inbound URLs. Checks if the URL matches the redirect URI for a pending AppAuth
    // authorization request and if so, will look for the code in the response.
    
    func application(_ application: UIApplication, open url: URL, sourceApplication: String?, annotation: Any) -> Bool {
        
        print("Received callback!")
        
        MSALPublicClientApplication.handleMSALResponse(url)
        
        return true
    }

    func splitViewController(_ splitViewController: UISplitViewController,
                             collapseSecondary secondaryViewController:UIViewController,
                             onto primaryViewController:UIViewController) -> Bool {
        guard let secondaryAsNavController = secondaryViewController as? UINavigationController else {
            return false
        }
        guard let _ = secondaryAsNavController.topViewController as? DetailViewController else {
            return false
        }
        if Model.shared.watching == nil {
            // Return true to indicate that we have handled the collapse by doing nothing
            // the secondary controller will be discarded.
            return true
        }
        return false
    }
}

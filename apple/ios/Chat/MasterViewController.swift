import UIKit

class MasterViewController: UITableViewController {

    var detailViewController: DetailViewController? = nil
    private var contacts = [Contact]()

    override func viewDidLoad() {
        super.viewDidLoad()

        self.navigationItem.leftBarButtonItem = self.editButtonItem

        let addButton = UIBarButtonItem(barButtonSystemItem: .add, target: self, action: #selector(askContact(_:)))
        self.navigationItem.rightBarButtonItems!.append(addButton)

        if let split = self.splitViewController {
            let controllers = split.viewControllers
            self.detailViewController = (controllers[controllers.count-1] as! UINavigationController).topViewController as? DetailViewController
        }

        EventBus.addListener(about: .contacts) { notification in
            self.contacts = Array(Model.shared.roster.values)
            self.tableView.reloadData()
        }

        EventBus.addListener(about: .presence) { notification in
            self.tableView.reloadData()
        }

        EventBus.addListener(about: .text) { notification in
            self.tableView.reloadData()
        }        
    }

    override func viewWillAppear(_ animated: Bool) {
        self.clearsSelectionOnViewWillAppear = self.splitViewController!.isCollapsed
        super.viewWillAppear(animated)
    }

    override func prepare(for segue: UIStoryboardSegue, sender: Any?) {
        if let controller = (segue.destination as? UINavigationController)?.topViewController as? DetailViewController,
            let indexPath = self.tableView.indexPathForSelectedRow {
            self.tableView.reloadData()
            Model.shared.watching = self.contacts[indexPath.row].id
            controller.navigationItem.leftBarButtonItem = self.splitViewController?.displayModeButtonItem
            controller.navigationItem.leftItemsSupplementBackButton = true
        }
    }

    func askContact(_ sender: Any) {
        SplitViewController.askString(title:"Add a contact", cancellable: true) { username in
            self.addContact(username)
        }
    }

    // table

    private func addContact(_ username: String) {
        let contactBuilder = Contact.Builder()
        contactBuilder.setId(username)
        contactBuilder.setName(username)
        if let contact = try? contactBuilder.build() {
            self.contacts.insert(contact, at: 0)
            self.updateNames()
        }
    }

    private func updateNames() {
        self.contacts.sort(by: {
            $0.name > $1.name
        })
        Model.shared.setContacts(self.contacts.map({ return $0.id }))
        self.tableView.reloadData()
    }

    override func numberOfSections(in tableView: UITableView) -> Int {
        return 1
    }

    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return contacts.count
    }

    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let cell = tableView.dequeueReusableCell(withIdentifier: "Cell", for: indexPath)
        let contact = self.contacts[indexPath.row]
        cell.textLabel?.text = self.cellTextFor(contact.id)
        cell.textLabel?.textColor = Model.shared.roster[contact.id]?.online == true ? .blue : .gray
        return cell
    }

    func cellTextFor(_ id: String) -> String {
        let unreads = Model.shared.unreads[id] ?? 0
        let showUnreads = unreads > 0 ? " (\(unreads))" : ""
        let name = Model.shared.roster[id]?.name ?? ""
        return name + showUnreads
    }

    override func tableView(_ tableView: UITableView,
                            commit editingStyle: UITableViewCellEditingStyle,
                            forRowAt indexPath: IndexPath) {
        if editingStyle == .delete {
            self.contacts.remove(at: indexPath.row)
            tableView.deleteRows(at: [indexPath], with: .fade)
            self.updateNames()
        }
    }
}

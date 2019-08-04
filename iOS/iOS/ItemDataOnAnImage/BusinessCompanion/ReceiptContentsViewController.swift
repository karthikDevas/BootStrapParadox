

import UIKit
import Vision

class ReceiptContentsViewController: UITableViewController, RecognizedTextDataSource {

    var image: UIImage?

    static let tableCellIdentifier = "receiptContentCell"

    // Use this height value to differentiate between big labels and small labels in a receipt.
    static let textHeightThreshold: CGFloat = 0.025
    
    typealias ReceiptContentField = (name: String, value: String)

    // The information to fetch from a scanned receipt.
    struct ReceiptContents {

        var name: String?
        var items = [ReceiptContentField]()
    }
    
    var contents = ReceiptContents()
}

// MARK: UITableViewDataSource
extension ReceiptContentsViewController {
    override func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
        return contents.items.count
    }
    
    override func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
        let field = contents.items[indexPath.row]
        let cell = tableView.dequeueReusableCell(withIdentifier: ReceiptContentsViewController.tableCellIdentifier, for: indexPath)
        cell.textLabel?.text = field.name
        cell.detailTextLabel?.text = field.value
//        print("\(field.name)\t\(field.value)")
        return cell
    }
}
    
    // MARK: RecognizedTextDataSource
extension ReceiptContentsViewController {
        
    func addRecognizedText(recognizedText: [VNRecognizedTextObservation])
    {
        var textWithOriginY: [CGFloat: String] = [:]
        
        let maximumCandidates = 1
        
        var previousOriginY = CGPoint.zero.y
        
        for observation in recognizedText
        {
            guard let candidate = observation.topCandidates(maximumCandidates).first else { continue }
            let text = candidate.string

            let yOrigin = observation.boundingBox.origin.y
            let diff = yOrigin - previousOriginY
            
            if  abs(diff) < 0.01
            {
                textWithOriginY[previousOriginY]?.append(" " + text)
            }
            else
            {
                if textWithOriginY[yOrigin] != nil
                {
                    textWithOriginY[yOrigin]?.append(" " + text)
                }
                else
                {
                    textWithOriginY[yOrigin] = text
                }
                
                previousOriginY = observation.boundingBox.origin.y
            }
        }
        
        let sortedKeys = Array(textWithOriginY.keys).sorted(by: >)
        
        sortedKeys.forEach { (key) in
            print(textWithOriginY[key] ?? "")
        }

//        let possibleKeywordsForItemAmount = ["amount", "item total"]
//
//        let itemsStarting = textWithOriginY.first { (keyValuePair) -> Bool in
//            keyValuePair.value.conta
//        }
    }
}

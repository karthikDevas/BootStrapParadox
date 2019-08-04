//
//  ImagePreviewController.swift
//  BusinessCompanion
//
//  Created by Karthikaeyan A R on 03/08/19.
//  Copyright © 2019 Apple. All rights reserved.
//

import UIKit
import Vision

extension NSRegularExpression {
    convenience init(_ pattern: String) {
        do {
            try self.init(pattern: pattern)
        } catch {
            preconditionFailure("Illegal regular expression: \(pattern).")
        }
    }
    
    func matches(_ string: String) -> Bool {
        let range = NSRange(location: 0, length: string.utf16.count)
        return firstMatch(in: string, options: [], range: range) != nil
    }
}

extension Equatable
{
    func isAny(of candidates: Self...) -> Bool
    {
        return candidates.contains(self)
    }
}

extension CGRect {
    init(p1: CGPoint, p2: CGPoint) {
        self.init(x: min(p1.x, p2.x),
                  y: min(p1.y, p2.y),
                  width: abs(p1.x - p2.x),
                  height: abs(p1.y - p2.y))
    }
}


class ImagePreviewController: UIViewController {
    
    private var imageView = UIImageView()
    private var image: UIImage
    private var observations: [VNRecognizedTextObservation]
    
    var textRecognitionRequest = VNRecognizeTextRequest()
    
    var textRecognition = VNRecognizeTextRequest()
    
    init(_ image:  UIImage, observations: [VNRecognizedTextObservation])
    {
        self.image = image
        self.observations = observations
        
        super.init(nibName: nil, bundle: nil)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()

        view.backgroundColor = .white
        
        imageView.translatesAutoresizingMaskIntoConstraints = false
        imageView.image = image
        imageView.contentMode = .scaleAspectFit
        view.addSubview(imageView)
        NSLayoutConstraint.activate([imageView.centerXAnchor.constraint(equalTo: view.centerXAnchor),
                                     imageView.centerYAnchor.constraint(equalTo: view.centerYAnchor),
                                     imageView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
                                     imageView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
                                     imageView.widthAnchor.constraint(equalTo: view.widthAnchor),
                                     imageView.heightAnchor.constraint(equalTo: view.heightAnchor)])
        
        textRecognition = VNRecognizeTextRequest(completionHandler: { (request, error) in
            
            if let results = request.results, !results.isEmpty {
                if let requestResults = request.results as? [VNRecognizedTextObservation] {
                    DispatchQueue.main.async {
                        self.observations = requestResults
                        self.processText()
                    }
                }
            }
        })

        textRecognition.recognitionLevel = .accurate
        textRecognition.usesLanguageCorrection = true
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        
        view.layoutIfNeeded()
        
        navigationItem.rightBarButtonItem = UIBarButtonItem(title: "Tap to scan", style: .done, target: self, action: #selector(startProcessing))
    }
    
    @objc private func startProcessing()
    {
        guard let cgImage = imageView.image?.cgImage else {
            print("Failed to get cgimage from input image")
            return
        }

        let handler = VNImageRequestHandler(cgImage: cgImage, options: [:])

        do {
            try handler.perform([textRecognition])
        } catch {
            print(error)
        }
    }
    
    func processText()
    {
        let maximumCandidates = 1
        
        ///Origin Y Co-ordinates and full texts in a line
        var recognisedTexts: [CGFloat: String] = [:]
        
        var previousRightTopMargin: CGPoint? = nil
        var previousBottomRightMargin: CGPoint? = nil
        var previousYOrigin: CGFloat = 0
        
        for observation in observations {
            guard let candidate = observation.topCandidates(maximumCandidates).first else { continue }
            
            let text = candidate.string
            let boundingBox = observation.boundingBox
            
            if let pRT = previousRightTopMargin, let pBT = previousBottomRightMargin
            {
                let projectionBox = CGRect(origin: CGPoint(x: 0, y: boundingBox.height), size: CGSize(width: boundingBox.origin.x, height: boundingBox.height))
                
                let previousTrailingBoundry = CGRect(p1: pBT, p2: pRT)
                
                if  previousTrailingBoundry.intersects(projectionBox)
                {
                    recognisedTexts[previousYOrigin]?.append(" " + text)
                }
                else
                {
                    previousRightTopMargin = CGPoint(x: boundingBox.width, y: boundingBox.height)
                    previousBottomRightMargin = CGPoint(x: boundingBox.width, y: boundingBox.origin.y)
                    
                    previousYOrigin = boundingBox.origin.y
                    recognisedTexts[previousYOrigin] = text
                }
            }
            else
            {
                previousRightTopMargin = CGPoint(x: boundingBox.width, y: boundingBox.height)
                previousBottomRightMargin = CGPoint(x: boundingBox.width, y: boundingBox.origin.y)
                
                previousYOrigin = boundingBox.origin.y
                recognisedTexts[previousYOrigin] = text
             }
        }
        
        let sortedKeys = Array(recognisedTexts.keys).sorted(by: >)
        
        let panAndGSTRegex = NSRegularExpression("[A-Z]{5}[0-9]{4}[A-Z]{1}")
        
        sortedKeys.forEach { (key) in
            if let value = recognisedTexts[key] {
                
                let types: NSTextCheckingResult.CheckingType = [.date, .phoneNumber, .address]
                let detector = try! NSDataDetector(types: types.rawValue)
                let matches = detector.matches(in: value, options: .init(), range: NSRange(location: 0, length: value.count))
                
                if !panAndGSTRegex.matches(value), matches.isEmpty {
                    
                    var canShow = true
                    
                    for restrictedWord in ["bill", "invoice", "reference"] {
                        if value.lowercased().contains(restrictedWord) {
                            canShow = false
                            break
                        }
                    }
                    
                    if canShow {
                        
                        var amountValues = value.split(separator: " ").compactMap{ return Double($0) }
                        let textValues = value.split(separator: " ").filter{ return Double($0) == nil}
                        
                        var message = ""
                        
                        if !textValues.isEmpty, amountValues.count > 2 {
                            message = "Item Details"
                            message += "\n Item Name:"
                            message += textValues.joined(separator: " ")

                            message += "\n Item Amount:"
                            let max = amountValues.max()!
                            message += "\(max)"
                            
                            if let amountIndex = amountValues.firstIndex(where: {$0 == max}) {
                                amountValues.remove(at: amountIndex)
                            }
                            
                            if amountValues.count > 1 {
                                message += "\n Rate or Quantity:"
                                message += amountValues.map{ "\($0)" }.joined(separator: ",")
                            }
                            else if !amountValues.isEmpty {
                                message += "\n Quantity:"
                                message += "\(amountValues.map{ "\($0)" }.joined(separator: ","))"
                            }
                            
                            let alert = UIAlertController(title: "", message: message, preferredStyle: .alert)
                            let cancelAction = UIAlertAction(title: "Ok", style: .cancel) { (_) in
                                self.dismiss(animated: true)
                            }
                            
                            alert.addAction(cancelAction)
                            present(alert, animated: true, completion: nil)
                        }
                    }
                }
            }
        }
    }
}

extension UIImage {
    func cropped(boundingBox: CGRect) -> UIImage? {
        
        guard let cgImage = self.cgImage?.cropping(to: boundingBox) else {
            return nil
        }

        return UIImage(cgImage: cgImage)
    }
}

/*
 func logText(for observations: [VNRecognizedTextObservation])
 {
     var textWithOriginY: [CGFloat: String] = [:]
     var previousOriginY = CGPoint.zero.y
     
     for observation in observations
     {
         guard let candidate = observation.topCandidates(1).first else { continue }
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
         print("\(key) - " + (textWithOriginY[key] ?? ""))
     }
 }
 */
 
//            let text = candidate.string
//
//            let regex = NSRegularExpression("[0-9][.]+[0-9]")
//
//            if regex.matches(text)
//            {
//                isFirstOccurenceOfDecimalFound = true
//
//                let rect = observation.boundingBox
//                let x: CGFloat = 0
//                let w = image.size.width
//                let h = rect.height * image.size.height
//                let y = image.size.height * (1-rect.origin.y) - h
//                let convRect = CGRect(x: x, y: y, width: w, height: h)
//
//                let croppedImage = imageView.image!.cropped(boundingBox: convRect)
//
//                guard let cgImage = croppedImage?.cgImage else {
//                    print("Failed to get cgimage from input image")
//                    return
//                }
//
//                let downscaledImageRect = CGRect(x: convRect.origin.x, y: convRect.origin.y / 3, width: convRect.size.width / 3, height: convRect.size.height / 3)
//
//                let layer = CAShapeLayer()
//                layer.path = UIBezierPath(roundedRect: downscaledImageRect, cornerRadius: 0).cgPath
//                layer.fillColor = UIColor.red.cgColor
//                view.layer.addSublayer(layer)
//
//                let handler = VNImageRequestHandler(cgImage: cgImage, options: [:])
//                do {
//                    try handler.perform([textRecognitionRequest])
//                } catch {
//                    print(error)
//                }
//
//                if isFirstOccurenceOfDecimalFound
//                {
//                    break
//                }
//            }

//        textRecognitionRequest = VNRecognizeTextRequest(completionHandler: { (request, error) in
//
//            if let results = request.results, !results.isEmpty {
//                if let requestResults = request.results as? [VNRecognizedTextObservation] {
//                    DispatchQueue.main.async { [weak self] in
//                        self?.logText(for: requestResults)
//                    }
//                }
//            }
//        })
//
//        textRecognitionRequest.recognitionLevel = .accurate
//        textRecognitionRequest.usesLanguageCorrection = true

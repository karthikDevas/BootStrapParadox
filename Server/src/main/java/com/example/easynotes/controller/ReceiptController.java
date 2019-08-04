package com.example.easynotes.controller;

import com.example.easynotes.exception.ResourceNotFoundException;
import com.example.easynotes.model.Receipt;
import com.example.easynotes.repository.ReceiptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ReceiptController {

    @Autowired
    ReceiptRepository receiptRepository;

    @GetMapping("/receipts")
    public List<Receipt> getAllReceipts() {
        return receiptRepository.findAll();
    }

    @PostMapping("/receipts")
    public Receipt createReceipt(@Valid @RequestBody Receipt receipt) {
        return receiptRepository.save(receipt);
    }

    @GetMapping("/receipts/{id}")
    public Receipt getReceiptByID(@PathVariable(value = "id") Long id)
    {
        return receiptRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Receipt", "id", id));
    }

    @PutMapping("/receipts/{id}")
    public Receipt updateReceipt(@PathVariable(value = "id") Long id,
                           @Valid @RequestBody Receipt receiptDetails)
    {

        Receipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt", "id", id));

        receipt.setProduct_name(receiptDetails.getProduct_name());
        receipt.setItem_id(receiptDetails.getItem_id());
        receipt.setGst_id(receiptDetails.getGst_id());
        receipt.setOrder_id(receiptDetails.getOrder_id());
        receipt.setPrice(receiptDetails.getPrice());
        receipt.setQuantity(receiptDetails.getQuantity());
        receipt.setShipping_charge(receiptDetails.getShipping_charge());

        return receiptRepository.save(receipt);
    }

    @DeleteMapping("/receipts/{id}")
    public ResponseEntity<?> deleteReceipt(@PathVariable(value = "id") Long id) {
        Receipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receipt", "id", id));

        receiptRepository.delete(receipt);

        return ResponseEntity.ok().build();
    }
}

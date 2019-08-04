package com.example.easynotes.controller;

import com.example.easynotes.exception.ResourceNotFoundException;
import com.example.easynotes.model.Item;
import com.example.easynotes.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ItemController {

    @Autowired
    ItemRepository itemRepository;

    @GetMapping("/items")
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    @PostMapping("/items")
    public Item createItem(@Valid @RequestBody Item item) {
        return itemRepository.save(item);
    }

    @GetMapping("/items/{product_sku}")
    public Item getItemById(@PathVariable(value = "product_sku") Long product_sku)
    {
        return itemRepository.findById(product_sku).orElseThrow(() -> new ResourceNotFoundException("Item", "product_sku", product_sku));
    }

    @PutMapping("/items/{product_sku}")
    public Item updateItem(@PathVariable(value = "product_sku") Long product_sku,
                           @Valid @RequestBody Item itemDetails)
    {

        Item item = itemRepository.findById(product_sku)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "product_sku", product_sku));

        item.setProduct_name(itemDetails.getProduct_name());
        item.setPrice(itemDetails.getPrice());

        Item updatedItem = itemRepository.save(item);
        return updatedItem;
    }

    @DeleteMapping("/items/{product_sku}")
    public ResponseEntity<?> deleteItem(@PathVariable(value = "product_sku") Long product_sku) {
        Item item = itemRepository.findById(product_sku)
                .orElseThrow(() -> new ResourceNotFoundException("Item", "product_sku", product_sku));

        itemRepository.delete(item);

        return ResponseEntity.ok().build();
    }
}

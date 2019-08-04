package com.example.easynotes.controller;

import com.example.easynotes.exception.ResourceNotFoundException;
import com.example.easynotes.model.Stores;
import com.example.easynotes.repository.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
public class StoreController {

    @Autowired
    StoreRepository storeRepository;

    @GetMapping("/stores")
    public List<Stores> getAllStores() {
        return storeRepository.findAll();
    }

    @PostMapping("/stores")
    public Stores createStores(@Valid @RequestBody Stores stores) {
        return storeRepository.save(stores);
    }

    @GetMapping("/stores/{id}")
    public Stores getStoreByID(@PathVariable(value = "id") Long id)
    {
        return storeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Stores", "id", id));
    }

    @PutMapping("/stores/{id}")
    public Stores updateStores(@PathVariable(value = "id") Long id,
                               @Valid @RequestBody Stores storesDetails)
    {

        Stores stores = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stores", "id", id));

        stores.setStore_name(stores.getStore_name());
        stores.setDoor_no(stores.getDoor_no());
        stores.setStreet(stores.getStreet());
        stores.setCity(stores.getCity());
        stores.setState(stores.getState());
        stores.setGstin(stores.getGstin());
        stores.setRating(stores.getRating());

        Stores updatedStores = storeRepository.save(stores);
        return updatedStores;
    }

    @DeleteMapping("/stores/{id}")
    public ResponseEntity<?> deleteStore(@PathVariable(value = "id") Long id) {
        Stores stores = storeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stores", "id", id));

        storeRepository.delete(stores);

        return ResponseEntity.ok().build();
    }
}

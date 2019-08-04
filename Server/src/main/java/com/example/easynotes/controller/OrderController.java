package com.example.easynotes.controller;

import com.example.easynotes.exception.ResourceNotFoundException;
import com.example.easynotes.model.MasterModel;
import com.example.easynotes.model.Orders;
import com.example.easynotes.model.Item;
import com.example.easynotes.repository.ItemRepository;
import com.example.easynotes.repository.OrderRepository;
import jdk.nashorn.api.scripting.JSObject;
import jdk.nashorn.internal.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import org.json.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class OrderController {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    ItemRepository itemRepository;

    @GetMapping("/orders")
        public List<Orders> getAllItems()
    {
        return orderRepository.findAll();
    }

    @PostMapping("/orders")
    public Orders createItem(@Valid @RequestBody Orders orders)
    {
        String storeData = orders.getStore();
        orders.setOrder_status(0);
        JSONObject jo = new JSONObject(storeData);
        JSONArray jsonArray = jo.getJSONArray("Items");
        for(int i=0; i<jsonArray.length(); i++)
        {
            JSONObject jsonObject =(JSONObject) jsonArray.get(i);
            String itemName = String.valueOf(jsonObject.get("name"));
            if(!SpellChecker.match_words(SpellChecker.itemDictionary, itemName))
            {
                MasterModel.doItemCatelogueAction(itemName, jsonObject.getInt("rate"));
            }
        }

        orders.setOrder_status(1);
        String name = jo.get("name");
        if(!SpellChecker.match_words(SpellChecker.storesDictionary, name))
        {
            MasterModel.doStoreUpdateAction(jo.get("name"), jo.get("address"), jo.get("address"), jo.get("address"), jo.get("address"), jo.getInt("rating"), jo.get("tin_no"));
        }

        Orders updatedOrder = orderRepository.save(orders);
        return updatedOrder;
    }

    @GetMapping("/orders/{id}")
    public Orders getOrderById(@PathVariable(value = "id") Long id)
    {
        return orderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Orders", "id", id));
    }

    @PutMapping("/orders/{id}")
    public Orders updateOrder(@PathVariable(value = "id") Long id,
                           @Valid @RequestBody Orders orderDetails)
    {

        Orders orders = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orders", "id", id));

        orders.setOrder_status(1);
        String storeData = orderDetails.getStore();
        orders.setStore(storeData);
        Orders updatedOrder = orderRepository.save(orders);
        //{"name":"CHOLA BHATURA   ","qty":1.0,"rate":78.6,"amount":78.6}
        JSONObject jo = new JSONObject(storeData);
        jo = jo.getJSONObject("store");
        JSONArray jsonArray = jo.getJSONArray("items");
        for(int i=0; i<jsonArray.length(); i++)
        {
            JSONObject jsonObject =(JSONObject) jsonArray.get(i);
            String itemName = String.valueOf(jsonObject.getJSONObject("name"));
            SpellChecker.match_words(itemName);
        }

        return updatedOrder;
    }

    @DeleteMapping("/orders/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable(value = "id") Long id) {
        Orders orders = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orders", "id", id));

        orderRepository.delete(orders);

        return ResponseEntity.ok().build();
    }
}

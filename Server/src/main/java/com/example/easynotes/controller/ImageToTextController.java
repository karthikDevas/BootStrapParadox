/*package com.example.easynotes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.OutputStream;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ImageToTextController
{
    private static String FOLDER = "~/Home/web/setup/spring_mysql/";

    @PutMapping("/upload/{order_id}")
    public String singleFileUpload(@RequestParam("file") MultipartFile multipartFile, RedirectAttributes redirectAttributes)
    {
        if (multipartFile.isEmpty())
        {
            redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
            return "redirect:uploadStatus";
        }

        try
        {
            File tempFile = File.createTempFile(FOLDER, multipartFile.getOriginalFilename());
            tempFile.deleteOnExit();
            multipartFile.transferTo(tempFile);
            redirectAttributes.addFlashAttribute("message", "You successfully uploaded '");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return "";
    }

}*/
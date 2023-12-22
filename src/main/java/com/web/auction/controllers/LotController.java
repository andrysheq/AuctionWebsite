package com.web.auction.controllers;

import com.web.auction.data.LotRepository;
import com.web.auction.models.Lot;
import com.web.auction.models.LotForm;
import com.web.auction.models.LotProps;
import com.web.auction.models.User;
import com.web.auction.security.RegistrationForm;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/lots")
public class LotController {

    private LotRepository lotRepo;

    private LotProps props;

    public LotController(LotRepository lotRepo,LotProps props) {
        this.lotRepo = lotRepo;
        this.props = props;
    }

    @GetMapping("/current")
    public String orderForm(@AuthenticationPrincipal User user,
                            @ModelAttribute Lot lot) {
        if (lot.getNameOfLot() == null) {
            lot.setNameOfLot(user.getFullName());
        }
        if (lot.getStreet() == null) {
            lot.setStreet(user.getStreet());
        }
        if (lot.getCity() == null) {
            lot.setCity(user.getCity());
        }


        return "createLot";
    }

    @PostMapping
    public String processLot(@ModelAttribute("form") @Valid LotForm form,
                             BindingResult result,
                             @RequestParam("photo") MultipartFile photo,
                             Model model,@Valid Lot lot, Errors errors,
                               SessionStatus sessionStatus,
                               @AuthenticationPrincipal User user) {

        if (errors.hasErrors()) {
            return "createLot";
        }

        try {
            form.setPhoto(photo); // сохранить фото в объекте RegistrationForm
        }catch (Exception e){
            return "redirect:/lots/current?error";
        }

        Lot lotToSave = form.toLot();

        lotToSave.setUser(user);
        lotToSave.setCity(user.getCity());
        lotToSave.setStreet(user.getStreet());

        lotRepo.save(lotToSave);

        sessionStatus.setComplete();

        return "redirect:/";
    }

    @GetMapping
    public String lotsForUser(
            @AuthenticationPrincipal User user, Model model) {


        List<Lot> lots = lotRepo.findLotByUser(user);
        Map<Lot, String> lotPhotoMap = new HashMap<>();

        for (Lot lot : lots) {
            byte[] photoBytes = lot.getPhotoOfLot();
            String photoBase64 = Base64.getEncoder().encodeToString(photoBytes);
            lotPhotoMap.put(lot, photoBase64);
        }

        model.addAttribute("lots", lots);
        model.addAttribute("lotPhotoMap", lotPhotoMap);

        return "lotList";
    }

}
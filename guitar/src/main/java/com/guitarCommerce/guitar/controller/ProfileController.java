package com.guitarCommerce.guitar.controller;

import com.guitarCommerce.guitar.entity.User;
import com.guitarCommerce.guitar.service.UserService;

import java.security.Principal;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public ProfileController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String showProfile(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        model.addAttribute("user", user);
        return "profile";
    }



// =====================================================================
//              Aggiornamenti Profilo Utente



// -------------------- USERNAME ----------------- fatto
@GetMapping("/update-username")
public String updateUsername(@RequestParam String username, Model model, Principal principal) {
    // Recupera l'utente corrente
    User currentUser = userService.findByUsername(principal.getName());

    // Verifica se esiste un altro utente con lo stesso username
    User existingUser = userService.findByUsername(username);

    if (existingUser != null) {
        if (existingUser.getId() == currentUser.getId()) {
            // Se l'utente sta cercando di impostare il proprio username attuale
            model.addAttribute("errorUsername", "È il tuo username attuale.");
        } else {
            // Se esiste un altro utente con lo stesso username
            model.addAttribute("errorUsername", "Username già in uso.");
        }
    } else {
        // Aggiorna il username dell'utente corrente
        currentUser.setUsername(username);
        userService.updateUser(currentUser);

        // Aggiungi un messaggio di successo
        model.addAttribute("successUsername", "Username aggiornato con successo.");
    }

    // Aggiungi l'utente aggiornato al modello
    model.addAttribute("user", currentUser);

    return "profile";
}

// -------------------- PASSWORD -----------------  testare
@PostMapping("/change-password")
public String changePassword(@RequestParam String currentPassword,
                             @RequestParam String newPassword,
                             @RequestParam String confirmPassword,
                             Principal principal,
                             RedirectAttributes redirectAttributes,
                             Model model) {
    User currentUser = userService.findByUsername(principal.getName());

    if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
        // La password attuale non è corretta
        redirectAttributes.addFlashAttribute("errorPassword", "Password attuale errata");
    } else if (!newPassword.equals(confirmPassword)) {
        // Le nuove password non coincidono
        redirectAttributes.addFlashAttribute("errorPassword", "Le nuove password non coincidono");
    } else {
        // Aggiorna la password dell'utente
        currentUser.setPassword(passwordEncoder.encode(newPassword));
        userService.updateUser(currentUser);
        redirectAttributes.addFlashAttribute("successPassword", "Password aggiornata con successo");
    }

    // Reindirizza alla pagina del profilo per evitare di inviare nuovamente il modulo
    return "redirect:/profile";
}


// -------------------- MAIL -----------------   fatto

@PostMapping("/change-Email")
public String changeEmail(@RequestParam String currentMail,  // mail attuale per controllo
                          @RequestParam String newMail,   // mail aggiornata
                          Principal principal,  // user attule
                          Model model) {  // model
    User currentUser = userService.findByUsername(principal.getName()); 

    if (!currentUser.getEmail().equals(currentMail)) {
        // non aggiorno e aggiungo un messaggio d'errore
        model.addAttribute("errorEmail", "L'email attuale non corrisponde.");
    } else {

        // aggiorno e aggiungo un messaggio di successo
        currentUser.setEmail(newMail);
        userService.updateUser(currentUser);
        model.addAttribute("successEmail", "Email aggiornata.");
    }

    // aggiungo il modello a prescindere
    model.addAttribute("user", currentUser); 
    return "profile";
}
    
    

    


// -------------------- TELEFONO -----------------   da finire

// @PostMapping("/update-phone")
// public String updatePhone(@ModelAttribute("user") User user, Model model) {
//     User currentUser = userService.getCurrentUser();

//     // Aggiorna solo il campo telefono
//     currentUser.setPhone(user.getPhone());
//     userService.updateUser(currentUser);

//     // Aggiungi un messaggio di successo appropriato
//     model.addAttribute("successPhone", "Numero di telefono aggiornato con successo.");
//     model.addAttribute("user", currentUser);

//     return "profile";
// }


@PostMapping("/update-phone")
public String updatePhone(@ModelAttribute("user") User user, Model model) {
    User currentUser = userService.getCurrentUser();

    // Verifica che il numero di telefono non sia vuoto
    if (user.getPhone() == null || user.getPhone().trim().isEmpty()) {
        model.addAttribute("errorPhone", "Il numero di telefono non può essere vuoto.");
    }
    // Verifica che il numero di telefono abbia un formato valido
    else if (!isValidPhoneNumber(user.getPhone())) {
        model.addAttribute("errorPhone", "Il numero di telefono non è valido.");
    }
    // Verifica che il numero di telefono non sia già in uso da un altro utente
    else if (userService.isPhoneNumberInUse(user.getPhone(), currentUser.getId())) {
        model.addAttribute("errorPhone", "Il numero di telefono è già in uso.");
    }
    else {
        // Aggiorna il numero di telefono dell'utente corrente
        currentUser.setPhone(user.getPhone());
        userService.updateUser(currentUser);

        // Aggiungi un messaggio di successo
        model.addAttribute("successPhone", "Numero di telefono aggiornato con successo.");
    }

    // Aggiungi l'utente aggiornato al modello
    model.addAttribute("user", currentUser);

    return "profile";
}

// Metodo di utilità per validare il formato del numero di telefono
private boolean isValidPhoneNumber(String phoneNumber) {
    // Esempio di espressione regolare per un numero di telefono valido
    // Questo può variare a seconda del formato desiderato
    String phoneRegex = "^[+]?[0-9]{10,15}$";
    return phoneNumber.matches(phoneRegex);
}



// -------------------- INDIRIZZO -----------------




// -------------------- INDIRIZZO -----------------


    @PostMapping("/update-address")
    public String updateAddress(@ModelAttribute User user, Principal principal) {
        User currentUser = userService.findByUsername(principal.getName());
        currentUser.setStreet(user.getStreet());
        currentUser.setCity(user.getCity());
        currentUser.setPostalCode(user.getPostalCode());
        currentUser.setCountry(user.getCountry());
        userService.updateUser(currentUser);
        return "redirect:/profile";
    }



    
}
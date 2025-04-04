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

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/profile")
public class ProfileController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public ProfileController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

// =====================================================================
//              Dati del Profilo Utente

    @GetMapping
    public String showProfile(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        model.addAttribute("user", user);
        return "profile";
    }



// =====================================================================
//              Aggiornamenti Profilo Utente



// -------------------- USERNAME ----------------- fatto - controllato 
@PostMapping("/change-username")
public String updateUsername(@RequestParam String username, Model model, Principal principal) {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    logger.info("Username dal Principal: {}", principal.getName());
    logger.info("Nuovo username richiesto: {}", username);

    
    
    try {
        // Recupera l'utente corrente
        User currentUser = userService.findByUsername(principal.getName());

        // nel caso non trovasse un utente, non dovrebbe succedere...
        if (currentUser == null) {
            model.addAttribute("error", "Errore: Utente non trovato. Riprova o contatta l'assistenza.");
            model.addAttribute("errorUsername", "Errore: Utente non trovato. Riprova o contatta l'assistenza.");
            return "profile";
        }

        // Verifica se esiste un altro utente con lo stesso username
        User existingUser = userService.findByUsername(username);

        if (existingUser != null) {
            if (existingUser.getId() == currentUser.getId()) {
                model.addAttribute("error", "È il tuo username attuale.");
                model.addAttribute("errorUsername", "È il tuo username attuale.");
            } else {
                model.addAttribute("error", "Username già in uso.");
                model.addAttribute("errorUsername", "Username già in uso.");
            }
        } else {
            // Aggiorna il username dell'utente corrente
            currentUser.setUsername(username);
            userService.updateUser(currentUser);

            // --------- Aggiorna il contesto di autenticazione -------------
            // Recupera l'autenticazione corrente dal contesto di sicurezza.
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            // Crea un nuovo oggetto UserDetails con il nuovo nome utente (username), la password esistente e i ruoli/autorizzazioni correnti (auth.getAuthorities()).
            UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                username, currentUser.getPassword(), auth.getAuthorities());
            // Crea una nuova autenticazione con i dettagli aggiornati.
            Authentication newAuth = new UsernamePasswordAuthenticationToken(userDetails, auth.getCredentials(), auth.getAuthorities());
            // mposta il nuovo oggetto di autenticazione nel contesto di sicurezza.
            SecurityContextHolder.getContext().setAuthentication(newAuth);

            logger.info("Contesto di sicurezza aggiornato con nuovo username: {}", username);
            // Aggiungi un messaggio di successo
            model.addAttribute("success", "Username aggiornato con successo.");
            model.addAttribute("successUsername", "Username aggiornato con successo.");
        }

        // Aggiungi l'utente aggiornato al modello
        model.addAttribute("user", currentUser);

    } catch (RuntimeException e) {
        logger.error("Errore durante l'aggiornamento: {}", e.getMessage());
        model.addAttribute("error", "Errore: Utente non trovato. Riprova o contatta l'assistenza.");
        model.addAttribute("errorUsername", "Errore: Utente non trovato. Riprova o contatta l'assistenza.");
    }

    return "profile";
}

// -------------------- PASSWORD -----------------  fatto
@PostMapping("/change-password")
public String changePassword(@RequestParam String currentPassword,
                             @RequestParam String newPassword,
                             @RequestParam String confirmPassword,
                             Principal principal,
                             RedirectAttributes redirectAttributes,
                             Model model) {
    try{
        // recupero l'utente orrente
    User currentUser = userService.findByUsername(principal.getName());

    // verifico se la password attuale è corretta
    if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
        // La password attuale non è corretta
        redirectAttributes.addFlashAttribute("error", "Password attuale errata");
        redirectAttributes.addFlashAttribute("errorPassword", "Password attuale errata");

        // verifico se le due nuove password coincidono
    } else if (!newPassword.equals(confirmPassword)) {
        // Le nuove password non coincidono
        redirectAttributes.addFlashAttribute("error", "Le nuove password non coincidono");
        redirectAttributes.addFlashAttribute("errorPassword", "Le nuove password non coincidono");

    //se va tutto bene fin qui
    } else {
        // cripto e aggiorno la nuova password
        String encodedNewPassword = passwordEncoder.encode(newPassword);
        currentUser.setPassword(encodedNewPassword);
        userService.updateUser(currentUser);

         // Aggiorno il contesto di autenticazione con la nuova password
         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
         UserDetails userDetails = new org.springframework.security.core.userdetails.User(
             currentUser.getUsername(), encodedNewPassword, auth.getAuthorities());
         Authentication newAuth = new UsernamePasswordAuthenticationToken(userDetails, newPassword, auth.getAuthorities());
         SecurityContextHolder.getContext().setAuthentication(newAuth);

         // messaggio di successo
         redirectAttributes.addFlashAttribute("success", "Password aggiornata con successo");
        redirectAttributes.addFlashAttribute("successPassword", "Password aggiornata con successo");
    }

    // Reindirizza alla pagina del profilo
    return "redirect:/profile";

} catch (RuntimeException e) {
    redirectAttributes.addFlashAttribute("error", "Errore: Utente non trovato. Riprova o contatta l'assistenza.");
    redirectAttributes.addFlashAttribute("errorPassword", "Errore: Utente non trovato. Riprova o contatta l'assistenza.");
    return "redirect:/profile";
}
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
        model.addAttribute("error", "L'email attuale non corrisponde.");
        model.addAttribute("errorEmail", "L'email attuale non corrisponde.");
    } else {

        // aggiorno e aggiungo un messaggio di successo
        currentUser.setEmail(newMail);
        userService.updateUser(currentUser);
        model.addAttribute("success", "Email aggiornata.");
        model.addAttribute("successEmail", "Email aggiornata.");
    }

    // aggiungo il modello a prescindere
    model.addAttribute("user", currentUser); 
    return "profile";
}
    
    

    


// -------------------- TELEFONO -----------------   da finire



@PostMapping("/update-phone")
public String updatePhone(@ModelAttribute("user") User user, Model model) {
    try {
        // Recupera l'utente corrente autenticato
        User currentUser = userService.getCurrentUser();

        // Verifica che il numero di telefono non sia vuoto
        if (user.getPhone() == null || user.getPhone().trim().isEmpty()) {
            model.addAttribute("error", "Il numero di telefono non può essere vuoto.");
            model.addAttribute("errorPhone", "Il numero di telefono non può essere vuoto.");
        }
        // Verifica che il numero di telefono abbia un formato valido (chiamata al metodo del service)
        else if (!userService.isValidPhoneNumber(user.getPhone())) {
            model.addAttribute("error", "Il numero di telefono non è valido.");
            model.addAttribute("errorPhone", "Il numero di telefono non è valido.");
        }
        // Verifica che il numero di telefono non sia già in uso da un altro utente
        else if (userService.isPhoneNumberInUse(user.getPhone(), currentUser.getId())) {
            model.addAttribute("error", "Il numero di telefono è già in uso.");
            model.addAttribute("errorPhone", "Il numero di telefono è già in uso.");
        }
        else {
            // Aggiorna il numero di telefono dell'utente corrente
            currentUser.setPhone(user.getPhone());
            userService.updateUser(currentUser);

            // Aggiungi un messaggio di successo
            model.addAttribute("success", "Numero di telefono aggiornato con successo.");
            model.addAttribute("successPhone", "Numero di telefono aggiornato con successo.");
        }

        // Aggiungi l'utente aggiornato al modello
        model.addAttribute("user", currentUser);

        // Ritorna alla pagina del profilo senza reindirizzamento
        return "profile";

    } catch (RuntimeException e) {
        model.addAttribute("error", "Errore: Impossibile aggiornare il numero di telefono. Riprova o contatta l'assistenza.");
        model.addAttribute("errorPhone", "Errore: Impossibile aggiornare il numero di telefono. Riprova o contatta l'assistenza.");
        model.addAttribute("user", userService.getCurrentUser()); // Assicurati che "user" sia comunque presente nel modello
        return "profile";
    }
}








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
package com.guitarCommerce.guitar.service;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;

import com.guitarCommerce.guitar.entity.User;
import com.guitarCommerce.guitar.repository.UserRepository;

import org.slf4j.Logger;

// ======================================= ok

@Service
public class UserService {

    // dipendenze
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ========================================================================
    // logger
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    // ------------------------------------------
    // autenticazione
    private void updateAuthenticationContext(String username, String password, Authentication auth) {
        // Questo crea un nuovo oggetto UserDetails utilizzando il nome utente, 
        //la password e le autorità (ruoli) dell'utente corrente. 
        //L'oggetto User è una implementazione di UserDetails fornita da Spring Security.
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                username, password, auth.getAuthorities());
        Authentication newAuth = new UsernamePasswordAuthenticationToken(userDetails, auth.getCredentials(),
                auth.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }

    // ---------------------------------------------------
    // validazione numero di telefono
    private String validatePhoneNumber(String phone, Integer currentUserId, Model model) {
        if (phone == null || phone.trim().isEmpty()) {
            model.addAttribute("errorPhone", "Il numero di telefono non può essere vuoto.");
            return "Il numero di telefono non può essere vuoto.";
        }
        if (!isValidPhoneNumber(phone)) {
            model.addAttribute("errorPhone", "Il numero di telefono non è valido.");
            return "Il numero di telefono non è valido.";
        }
        if (isPhoneNumberInUse(phone, currentUserId)) {
            model.addAttribute("errorPhone", "Il numero di telefono è già in uso.");
            return "Il numero di telefono è già in uso.";
        }
        return null; // Nessun errore
    }


    public boolean isPhoneNumberInUse(String phone, Integer currentUserId) {
        Optional<User> existingUser = userRepository.findByPhone(phone);
        return existingUser.isPresent() && existingUser.get().getId() != currentUserId;
    }

    public boolean isValidPhoneNumber(String phoneNumber) {
        // espressione regolare
        String phoneRegex = "^\\+?[0-9]{1,3}?\\s?\\(?([0-9]{1,3}?)\\)?[\\s.-]?[0-9]{1,4}[\\s.-]?[0-9]{1,4}[\\s.-]?[0-9]{1,9}$";
        return phoneNumber != null && phoneNumber.matches(phoneRegex);
    }

    // ========================================================================
    // Mostra il profilo utente
    public void showProfile(Model model) {
        User user = getCurrentUser();
        model.addAttribute("user", user);
    }

    // ========================================================================
    // -------------------- AGGIORNAMENTI ------------------------


    // Aggiorno l'utente
    @Transactional
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    // Aggiorno il ruolo di un utente
    @Transactional
    public void updateUserRole(Integer id, String role, Model model) {
        User user = getUserById(id);
        if (user == null) {
            model.addAttribute("error", "Utente non trovato.");
            return;
        }
        user.setRole(role);
        userRepository.save(user);
        model.addAttribute("success", "Ruolo aggiornato con successo.");
        model.addAttribute("user", user);
    }

 // Aggiorna l'username
@Transactional
public void updateUsername(String username, Principal principal, Model model) {
    try {
        User currentUser = getCurrentUser();  // recupero l'utente corrente
        // se è nullo, improbabile
        if (currentUser == null) {
            model.addAttribute("error", "Errore: Utente non trovato. Riprova o contatta l'assistenza.");
            model.addAttribute("errorUsername", "Errore: Utente non trovato. Riprova o contatta l'assistenza.");
            return;
        }
        // cerco un utente con il nuovo username
        User existingUser = userRepository.findByUsername(username).orElse(null);
        // se lo trovo
        if (existingUser != null) {
            // se è il suo -.-
            if (existingUser.getId() == currentUser.getId()) {
                model.addAttribute("error", "È il tuo username attuale.");
                model.addAttribute("errorUsername", "È il tuo username attuale.");
            } else { // se di un altro
                model.addAttribute("error", "Username già in uso.");
                model.addAttribute("errorUsername", "Username già in uso.");
            }
        } else {
            currentUser.setUsername(username);
            updateUser(currentUser); // aggiorno l'user con il nuovo username

            // ---  Aggiorno il contesto di autenticazione con il nuovo username
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            updateAuthenticationContext(username, currentUser.getPassword(), auth);

            logger.info("Contesto di sicurezza aggiornato con nuovo username: {}", username);
            model.addAttribute("success", "Username aggiornato con successo.");
            model.addAttribute("successUsername", "Username aggiornato con successo.");
        }

        // Assicurati che il modello contenga tutte le informazioni dell'utente
        model.addAttribute("user", currentUser);
    } catch (RuntimeException e) {
        // errore di qualche tipo
        logger.error("Errore durante l'aggiornamento: {}", e.getMessage());
        model.addAttribute("error", "Errore: Utente non trovato. Riprova o contatta l'assistenza.");
        model.addAttribute("errorUsername", "Errore: Utente non trovato. Riprova o contatta l'assistenza.");
    }
}

    // ------------------------------
    // Aggiorna la password
    @Transactional
    public void changePassword(String currentPassword, String newPassword, String confirmPassword,
            Principal principal, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = getCurrentUser();  // recupero l'utente corrente
            // confronto con la password dell'utente
            if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "Password attuale errata");
                redirectAttributes.addFlashAttribute("errorPassword", "Password attuale errata");
            // confronto con la password di conferma
            } else if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Le nuove password non coincidono");
                redirectAttributes.addFlashAttribute("errorPassword", "Le nuove password non coincidono");
            // Se entrambe le verifiche sono superate, codifica la nuova password utilizzando passwordEncoder e aggiorna la password dell'utente corrente
            } else {
                String encodedNewPassword = passwordEncoder.encode(newPassword);
                currentUser.setPassword(encodedNewPassword);
                updateUser(currentUser); // aggiorno l'utente con la nuova password

                // Aggiorno il contesto di autenticazione con la nuova password
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                updateAuthenticationContext(currentUser.getUsername(), encodedNewPassword, auth);

                redirectAttributes.addFlashAttribute("success", "Password aggiornata con successo");
                redirectAttributes.addFlashAttribute("successPassword", "Password aggiornata con successo");
            }
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Errore: Utente non trovato. Riprova o contatta l'assistenza.");
            redirectAttributes.addFlashAttribute("errorPassword", "Errore: Utente non trovato. Riprova o contatta l'assistenza.");
        }
    }

    // -----------------------------
    // Aggiorna l'email
    @Transactional
    public void changeEmail(String currentMail, String newMail, Principal principal, Model model) {
        User currentUser = getCurrentUser();  // recupero l'utente corrente

        // Verifica se l'email attuale fornita (currentMail) corrisponde all'email memorizzata per l'utente corrente
        if (!currentUser.getEmail().equals(currentMail)) {
            model.addAttribute("error", "L'email attuale non corrisponde.");
            model.addAttribute("errorEmail", "L'email attuale non corrisponde.");
        // Se l'email attuale corrisponde, aggiorna l'email dell'utente corrente con la nuova email (newMail)
        } else {
            currentUser.setEmail(newMail);
            updateUser(currentUser);
            model.addAttribute("success", "Email aggiornata.");
            model.addAttribute("successEmail", "Email aggiornata.");
        }

        model.addAttribute("user", currentUser);
    }

    // -----------------------------
    // Aggiorna il numero di telefono
    @Transactional
    public void updatePhone(User user, Model model) {
        try {
            User currentUser = getCurrentUser(); // recupero l'utente corrente
            // Valida il nuovo numero di telefono chiamando il metodo validatePhoneNumber()
            String error = validatePhoneNumber(user.getPhone(), currentUser.getId(), model);
    
            // Se non ci sono errori di validazione (error è null), aggiorna il numero di telefono dell'utente corrente con il nuovo numero di telefono.
            if (error == null) {
                currentUser.setPhone(user.getPhone());
                updateUser(currentUser);
                model.addAttribute("success", "Numero di telefono aggiornato con successo.");
                model.addAttribute("successPhone", "Numero di telefono aggiornato con successo.");
            }
    
            // Assicurati che il modello contenga tutte le informazioni dell'utente
            model.addAttribute("user", currentUser);
        } catch (RuntimeException e) {
            model.addAttribute("error", "Errore: Impossibile aggiornare il numero di telefono. Riprova o contatta l'assistenza.");
            model.addAttribute("errorPhone", "Errore: Impossibile aggiornare il numero di telefono. Riprova o contatta l'assistenza.");
            model.addAttribute("user", getCurrentUser());
        }
    }

    // -----------------------------
   // Aggiorna l'indirizzo
   @Transactional
   public void updateAddress(User user, Principal principal) {
       User currentUser = findByUsername(principal.getName());
   
       // Aggiorna solo i campi dell'indirizzo, mantenendo gli altri campi invariati
       currentUser.setStreet(user.getStreet());
       currentUser.setCity(user.getCity());
       currentUser.setPostalCode(user.getPostalCode());
       currentUser.setCountry(user.getCountry());
   
       // Aggiorna anche nome e cognome se presenti nel modulo
       if (user.getFirstName() != null) {
           currentUser.setFirstName(user.getFirstName());
       }
       if (user.getLastName() != null) {
           currentUser.setLastName(user.getLastName());
       }
       if (user.getPhone() != null) {
        currentUser.setPhone(user.getPhone());
    }
   
       // Salva l'utente aggiornato
       updateUser(currentUser);
   }



    

    // =========================================================================
    // ------- cerco utenti ------

    // trovo tutti gli utenti
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // trova un utente per username
    public User findByUsername(String username) {
        // Chiama il metodo findByUsername() del userRepository per cercare un utente con il dato username. 
        //Il risultato è un Optional<User>, che è un contenitore che può contenere un valore (l'utente trovato) o essere vuoto.
        Optional<User> optionalUser = userRepository.findByUsername(username);
        // se c'è un user
        if (optionalUser.isPresent()) {
            // restituisco l'utente con get per estrarlo da optional
            return optionalUser.get();
        } else {
            throw new RuntimeException("User non trovato con username: " + username);
        }
    }

   
    
    // trova un utente per id
    public User getUserById(Integer id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isPresent()) {
            return optionalUser.get();
        } else {
            throw new RuntimeException("User non trovato con id: " + id);
        }
    }

    // trova un utente per id e lo restituisce
    public void getUserDetails(Integer id, Model model) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            model.addAttribute("error", "Utente non trovato.");
        }
        model.addAttribute("user", user);
    }

    // ----------------------------------------------------------
    
    // recupero l'utente corrente
    public User getCurrentUser() {
        // Ottiene l'oggetto principal dall'autenticazione corrente nel contesto di sicurezza di Spring. 
        //Il principal rappresenta l'utente autenticato (UserDetails)
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                      // Se il principal è un'istanza di UserDetails ---       estrae il nome utente   --- converte il principal in una stringa
        String username = (principal instanceof UserDetails) ? ((UserDetails) principal).getUsername() : principal.toString();
        return findByUsername(username);
    }

    


  // =========================================================================
    // ------- Creo utente ------
    @Transactional
    public User createUser(User user) {
        // controlliamo che non ci sia già un utente con lo stesso username
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username già in uso");
        }
        return userRepository.save(user);
    }


// =========================================================================
    // ------- Cancello utente ------
    @Transactional
    public void deleteUser(Integer id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }




}